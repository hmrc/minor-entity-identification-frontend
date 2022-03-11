/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.minorentityidentificationfrontend.controllers.uaControllers

import play.api.http.Status.OK
import play.api.libs.json.{JsString, Json}
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.core.config.{EnableFullTrustJourney, EnableFullUAJourney, FeatureSwitching}
import uk.gov.hmrc.minorentityidentificationfrontend.models.KnownFactsMatchingResult._
import uk.gov.hmrc.minorentityidentificationfrontend.stubs.{AuthStub, StorageStub, ValidateUnincorporatedAssociationDetailsConnectorStub}
import uk.gov.hmrc.minorentityidentificationfrontend.utils.AuditEnabledSpecHelper
import uk.gov.hmrc.minorentityidentificationfrontend.utils.WiremockHelper.{stubAudit, verifyAudit}
import uk.gov.hmrc.minorentityidentificationfrontend.views.CheckYourAnswersCommonViewTests
import uk.gov.hmrc.minorentityidentificationfrontend.views.uaViews.UaCheckYourAnswersSpecificViewTests

class CheckYourAnswersControllerISpec extends AuditEnabledSpecHelper
  with AuthStub
  with StorageStub
  with ValidateUnincorporatedAssociationDetailsConnectorStub
  with CheckYourAnswersCommonViewTests
  with UaCheckYourAnswersSpecificViewTests
  with FeatureSwitching {

  "GET /identify-your-trust/<testJourneyId>/check-your-answers-business" when {

    "the EnableFullTrustJourney is enabled" when {

      "the applicant has a Ctutr and a registered office postcode" should {
        enable(EnableFullUAJourney)
        lazy val result: WSResponse = {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveUtr(testJourneyId)(OK, testCtutrJson)
          stubRetrievePostcode(testJourneyId)(OK, testOfficePostcode)
          stubRetrieveCHRN(testJourneyId)(NOT_FOUND)

          get(s"/identify-your-unincorporated-association/$testJourneyId/check-your-answers-business")
        }

        "return OK" in {
          result.status mustBe OK
        }

        "return a view which" should {
          testCheckYourAnswersCommonView(result)
          testUaWithCtutrAndOfficePostcodeSummaryListView(result, testJourneyId)
        }

      }

      "the applicant has a no Ctutr and a no charity hmrc reference number" should {
        enable(EnableFullUAJourney)
        lazy val result: WSResponse = {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveUtr(testJourneyId)(NOT_FOUND)
          stubRetrieveCHRN(testJourneyId)(NOT_FOUND)
          stubRetrievePostcode(testJourneyId)(NOT_FOUND)

          get(s"/identify-your-unincorporated-association/$testJourneyId/check-your-answers-business")
        }

        "return OK" in {
          result.status mustBe OK
        }

        "return a view which" should {
          testCheckYourAnswersCommonView(result)
          testUaWithNoCtutrAndNoCHRNSummaryListView(result, testJourneyId)
        }

      }

      "the applicant has a registered office postcode but they don't have Ctutr" should {
        enable(EnableFullUAJourney)
        lazy val result: WSResponse = {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveUtr(testJourneyId)(NOT_FOUND)
          stubRetrievePostcode(testJourneyId)(OK, testOfficePostcode)

          get(s"/identify-your-unincorporated-association/$testJourneyId/check-your-answers-business")
        }

        "return INTERNAL_SERVER_ERROR" in {
          result.status mustBe INTERNAL_SERVER_ERROR
        }

      }

      "the user is UNAUTHORISED" should {
        "redirect to sign in page" in {
          enable(EnableFullUAJourney)
          stubAuthFailure()

          lazy val result: WSResponse = get(s"/identify-your-unincorporated-association/$testJourneyId/check-your-answers-business")

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri("/bas-gateway/sign-in" +
              s"?continue_url=%2Fidentify-your-unincorporated-association%2F$testJourneyId%2Fcheck-your-answers-business" +
              "&origin=minor-entity-identification-frontend"
            )
          }
        }
      }

      "the user does not have an internal ID" should {
        "return an INTERNAL_SERVER_ERROR status" in {
          enable(EnableFullUAJourney)
          stubAuth(OK, successfulAuthResponse(internalId = None))

          lazy val result: WSResponse = get(s"/identify-your-unincorporated-association/$testJourneyId/check-your-answers-business")

          result.status mustBe INTERNAL_SERVER_ERROR

        }
      }

      "the EnableFullUAJourney is disabled" should {
        "throw an internal server exception" in {
          disable(EnableFullUAJourney)
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

          val result = get(s"/identify-your-unincorporated-association/$testJourneyId/check-your-answers-business")

          result.status mustBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }

  "POST /identify-your-unincorporated-association/check-your-answers-business" should {

    "redirect to the provided continueUrl when the identifiers are matched" in {
      enable(EnableFullUAJourney)

      await(insertJourneyConfig(
        journeyId = testJourneyId,
        internalId = testInternalId,
        testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
      ))

      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      stubRetrieveUtr(testJourneyId)(OK, testCtutrJson)
      stubRetrievePostcode(testJourneyId)(OK, testOfficePostcode)
      stubRetrieveCHRN(testJourneyId)(NOT_FOUND)

      stubValidateUnincorporatedAssociationDetails(testCtutr, testOfficePostcode)(OK, Json.obj("matched" -> true))

      stubStoreIdentifiersMatch(testJourneyId, SuccessfulMatchKey)(OK)

      stubAudit()

      val result = post(s"/identify-your-unincorporated-association/$testJourneyId/check-your-answers-business")()

      result must have {
        httpStatus(SEE_OTHER)
        redirectUri(expectedValue = s"$testContinueUrl?journeyId=$testJourneyId")
      }

      verifyStoreIdentifiersMatch(testJourneyId, expBody = JsString(SuccessfulMatchKey))
      verifyAudit()
    }

    "redirect to the cannot confirm business error page when the identifiers are not matched" in {

      enable(EnableFullUAJourney)

      await(insertJourneyConfig(
        journeyId = testJourneyId,
        internalId = testInternalId,
        testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
      ))

      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      stubRetrieveUtr(testJourneyId)(OK, testCtutrJson)
      stubRetrievePostcode(testJourneyId)(OK, testOfficePostcode)
      stubRetrieveCHRN(testJourneyId)(NOT_FOUND)

      stubValidateUnincorporatedAssociationDetails(testCtutr, testOfficePostcode)(OK, Json.obj("matched" -> false))

      stubStoreIdentifiersMatch(testJourneyId, DetailsMismatchKey)(OK)

      stubAudit()

      val result = post(s"/identify-your-unincorporated-association/$testJourneyId/check-your-answers-business")()

      result must have {
        httpStatus(SEE_OTHER)
        redirectUri(expectedValue = errorControllers.routes.CannotConfirmBusinessController.show(testJourneyId).url)
      }

      verifyStoreIdentifiersMatch(testJourneyId, expBody = JsString(DetailsMismatchKey))
      verifyAudit()
    }

    "redirect to the cannot confirm business error page when the unincorporated association's details cannot be found" in {

      enable(EnableFullUAJourney)

      await(insertJourneyConfig(
        journeyId = testJourneyId,
        internalId = testInternalId,
        testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
      ))

      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      stubRetrieveUtr(testJourneyId)(OK, testCtutrJson)
      stubRetrievePostcode(testJourneyId)(OK, testOfficePostcode)
      stubRetrieveCHRN(testJourneyId)(NOT_FOUND)

      stubValidateUnincorporatedAssociationDetails(testCtutr, testOfficePostcode)(BAD_REQUEST, Json.obj("code" -> "NOT_FOUND",
        "reason" -> "The back end has indicated that CT UTR cannot be returned"
      ))

      stubStoreIdentifiersMatch(testJourneyId, DetailsNotFoundKey)(OK)

      stubAudit()

      val result = post(s"/identify-your-unincorporated-association/$testJourneyId/check-your-answers-business")()

      result must have {
        httpStatus(SEE_OTHER)
        redirectUri(expectedValue = errorControllers.routes.CannotConfirmBusinessController.show(testJourneyId).url)
      }

      verifyStoreIdentifiersMatch(testJourneyId, expBody = JsString(DetailsNotFoundKey))
      verifyAudit()
    }

    "redirect to the provided continueUrl" when {

      "the user provides only a charity reference number" in {

        enable(EnableFullUAJourney)

        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
        ))

        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRetrieveUtr(testJourneyId)(NOT_FOUND)
        stubRetrievePostcode(testJourneyId)(NOT_FOUND)
        stubRetrieveCHRN(testJourneyId)(OK, testCHRN)

        stubStoreIdentifiersMatch(testJourneyId, UnMatchableKey)(OK)

        stubAudit()

        val result = post(s"/identify-your-unincorporated-association/$testJourneyId/check-your-answers-business")()

        result must have {
          httpStatus(SEE_OTHER)
          redirectUri(expectedValue = s"$testContinueUrl?journeyId=$testJourneyId")
        }

        verifyStoreIdentifiersMatch(testJourneyId, expBody = JsString(UnMatchableKey))
        verifyAudit()
      }

      "there is no Ctutr and no chrn" in {

        enable(EnableFullUAJourney)

        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
        ))

        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRetrieveUtr(testJourneyId)(NOT_FOUND)
        stubRetrievePostcode(testJourneyId)(NOT_FOUND)
        stubRetrieveCHRN(testJourneyId)(NOT_FOUND)

        stubStoreIdentifiersMatch(testJourneyId, UnMatchableKey)(OK)

        stubAudit()

        val result = post(s"/identify-your-unincorporated-association/$testJourneyId/check-your-answers-business")()

        result must have {
          httpStatus(SEE_OTHER)
          redirectUri(expectedValue = s"$testContinueUrl?journeyId=$testJourneyId")
        }

        verifyStoreIdentifiersMatch(testJourneyId, expBody = JsString(UnMatchableKey))
        verifyAudit()
      }

    }

    "handle an internal server error" when {

      "the backend service responds with an unexpected status" in {

        enable(EnableFullUAJourney)

        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
        ))

        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRetrieveUtr(testJourneyId)(OK, testCtutrJson)
        stubRetrievePostcode(testJourneyId)(OK, testOfficePostcode)
        stubRetrieveCHRN(testJourneyId)(NOT_FOUND)

        stubValidateUnincorporatedAssociationDetails(testCtutr, testOfficePostcode)(NOT_FOUND)

        val result = post(s"/identify-your-unincorporated-association/$testJourneyId/check-your-answers-business")()

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }

    "the user is UNAUTHORISED" should {
      "redirect to sign in page" in {
        enable(EnableFullUAJourney)
        stubAuthFailure()

        lazy val result: WSResponse = post(s"/identify-your-unincorporated-association/$testJourneyId/check-your-answers-business")()

        result must have {
          httpStatus(SEE_OTHER)
          redirectUri("/bas-gateway/sign-in" +
            s"?continue_url=%2Fidentify-your-unincorporated-association%2F$testJourneyId%2Fcheck-your-answers-business" +
            "&origin=minor-entity-identification-frontend"
          )
        }
      }
    }

    "return an INTERNAL_SERVER_ERROR status" when {
      "the user does not have an internal ID" in {
        enable(EnableFullUAJourney)
        stubAuth(OK, successfulAuthResponse(internalId = None))

        lazy val result: WSResponse = post(s"/identify-your-unincorporated-association/$testJourneyId/check-your-answers-business")()

        result.status mustBe INTERNAL_SERVER_ERROR

      }
    }
    "the EnableFullTrustJourney is disabled" should {
      "throw an internal server exception" in {
        disable(EnableFullTrustJourney)
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        val result = post(s"/identify-your-unincorporated-association/$testJourneyId/check-your-answers-business")()

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

}
