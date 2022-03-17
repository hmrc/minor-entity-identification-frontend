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

import play.api.libs.json.{JsString, Json}
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.core.config.{EnableFullUAJourney, FeatureSwitching}
import uk.gov.hmrc.minorentityidentificationfrontend.models.KnownFactsMatchingResult._
import uk.gov.hmrc.minorentityidentificationfrontend.models.{Registered, RegistrationNotCalled}
import uk.gov.hmrc.minorentityidentificationfrontend.stubs._
import uk.gov.hmrc.minorentityidentificationfrontend.utils.AuditEnabledSpecHelper
import uk.gov.hmrc.minorentityidentificationfrontend.utils.WiremockHelper.{stubAudit, verifyAudit}
import uk.gov.hmrc.minorentityidentificationfrontend.views.CheckYourAnswersCommonViewTests
import uk.gov.hmrc.minorentityidentificationfrontend.views.uaViews.UaCheckYourAnswersSpecificViewTests

class CheckYourAnswersControllerISpec extends AuditEnabledSpecHelper
  with AuthStub
  with StorageStub
  with BusinessVerificationStub
  with RegisterStub
  with ValidateUnincorporatedAssociationDetailsConnectorStub
  with CheckYourAnswersCommonViewTests
  with UaCheckYourAnswersSpecificViewTests
  with FeatureSwitching {

      "GET /identify-your-unincorporated-association/<testJourneyId>/check-your-answers-business" when {

      "the EnableFullUAJourney is enabled" when {
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
            stubAudit()

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
            stubAudit()

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
            stubRetrieveCHRN(testJourneyId)(NOT_FOUND)
            stubAudit()

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
            stubAudit()

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
            stubAudit()

            lazy val result: WSResponse = get(s"/identify-your-unincorporated-association/$testJourneyId/check-your-answers-business")

            result.status mustBe INTERNAL_SERVER_ERROR

          }
        }
      }
      "the EnableFullUAJourney is disabled" should {
        "throw an internal server exception" in {
          disable(EnableFullUAJourney)
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubAudit()

          val result = get(s"/identify-your-unincorporated-association/$testJourneyId/check-your-answers-business")

          result.status mustBe INTERNAL_SERVER_ERROR
        }
      }
    }

  "POST /identify-your-unincorporated-association/check-your-answers-business" when {

        "identifiers are matched, calls BV. Given BV creates a journey, the redirect url is the BV start journey url. The journey is not audited" in {
          enable(EnableFullUAJourney)

          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveUtr(testJourneyId)(OK, testCtutrJson)
          stubRetrievePostcode(testJourneyId)(OK, testOfficePostcode)

          stubValidateUnincorporatedAssociationDetails(testCtutr, testOfficePostcode)(OK, Json.obj("matched" -> true))
          stubStoreIdentifiersMatch(testJourneyId, SuccessfulMatchKey)(OK)

          val expectedBVJson = testCreateBusinessVerificationUAJourneyJson(
            testCtutr,
            testJourneyId,
            testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true))

          stubCreateBusinessVerificationJourney(expectedBVJson)(CREATED, testBVRedirectURIJson(testBusinessVerificationRedirectUrl))

          stubAudit()

          val result = post(s"/identify-your-unincorporated-association/$testJourneyId/check-your-answers-business")()

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(expectedValue = testBusinessVerificationRedirectUrl)
          }

          verifyStoreIdentifiersMatch(testJourneyId, expBody = JsString(SuccessfulMatchKey))
          verifyCreateBusinessVerificationJourney(expectedBVJson)

          verifyAudit()
        }

        "identifiers are matched, calls BV. Given BV returns FORBIDDEN, the redirect url is the provided continueUrl. The journey is audited" in {
          enable(EnableFullUAJourney)

          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveUtr(testJourneyId)(OK, testCtutrJson)
          stubRetrievePostcode(testJourneyId)(OK, testOfficePostcode)

          stubValidateUnincorporatedAssociationDetails(testCtutr, testOfficePostcode)(OK, Json.obj("matched" -> true))
          stubStoreIdentifiersMatch(testJourneyId, SuccessfulMatchKey)(OK)

          stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(OK)

          stubRetrieveEntityDetails(testJourneyId)(OK, testThisIsADummyJson)

          val expectedBVJson = testCreateBusinessVerificationUAJourneyJson(
            testCtutr,
            testJourneyId,
            testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true))

          stubCreateBusinessVerificationJourney(expectedBVJson)(FORBIDDEN)
          stubStoreBusinessVerificationStatus(testJourneyId, expBody = testVerificationStatusJson(verificationStatusValue = "FAIL"))(OK)

          stubAudit()

          val result = post(s"/identify-your-unincorporated-association/$testJourneyId/check-your-answers-business")()

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(expectedValue = s"$testContinueUrl?journeyId=$testJourneyId")
          }

          verifyStoreIdentifiersMatch(testJourneyId, expBody = JsString(SuccessfulMatchKey))
          verifyCreateBusinessVerificationJourney(expectedBVJson)
          verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)

          verifyAudit()
        }

        "identifiers are matched, calls BV and given BV returns NOT_FOUND, it redirects to the provided continueUrl. The journey is audited" in {
          enable(EnableFullUAJourney)

          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveUtr(testJourneyId)(OK, testCtutrJson)
          stubRetrievePostcode(testJourneyId)(OK, testOfficePostcode)

          stubValidateUnincorporatedAssociationDetails(testCtutr, testOfficePostcode)(OK, Json.obj("matched" -> true))
          stubStoreIdentifiersMatch(testJourneyId, SuccessfulMatchKey)(OK)

          val expectedBVJson = testCreateBusinessVerificationUAJourneyJson(
            testCtutr,
            testJourneyId,
            testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true))

          stubCreateBusinessVerificationJourney(expectedBVJson)(NOT_FOUND)
          stubStoreBusinessVerificationStatus(testJourneyId, expBody = testVerificationStatusJson(verificationStatusValue = "NOT_ENOUGH_INFORMATION_TO_CHALLENGE"))(OK)

          stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(OK)

          stubRetrieveEntityDetails(testJourneyId)(OK, testThisIsADummyJson)
          stubAudit()

          val result = post(s"/identify-your-unincorporated-association/$testJourneyId/check-your-answers-business")()

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(expectedValue = s"$testContinueUrl?journeyId=$testJourneyId")
          }

          verifyStoreIdentifiersMatch(testJourneyId, expBody = JsString(SuccessfulMatchKey))
          verifyCreateBusinessVerificationJourney(expectedBVJson)
          verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)

          verifyAudit()
        }

        "identifiers are not matched, does not call BV and it redirects to the cannot confirm business error page. The journey is audited" in {

          enable(EnableFullUAJourney)

          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveUtr(testJourneyId)(OK, testCtutrJson)
          stubRetrievePostcode(testJourneyId)(OK, testOfficePostcode)

          stubValidateUnincorporatedAssociationDetails(testCtutr, testOfficePostcode)(OK, Json.obj("matched" -> false))
          stubStoreIdentifiersMatch(testJourneyId, DetailsMismatchKey)(OK)

          stubStoreBusinessVerificationStatus(testJourneyId, expBody = testVerificationStatusJson(verificationStatusValue = "NOT_ENOUGH_INFORMATION_TO_CALL_BV"))(OK)
          stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(OK)

          stubRetrieveEntityDetails(testJourneyId)(OK, testThisIsADummyJson)
          stubAudit()

          val result = post(s"/identify-your-unincorporated-association/$testJourneyId/check-your-answers-business")()

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(expectedValue = errorControllers.routes.CannotConfirmBusinessController.show(testJourneyId).url)
          }

          verifyStoreIdentifiersMatch(testJourneyId, expBody = JsString(DetailsMismatchKey))
          verifyStoreBusinessVerificationStatus(testJourneyId, expBody = testVerificationStatusJson(verificationStatusValue = "NOT_ENOUGH_INFORMATION_TO_CALL_BV"))
          verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)
          verifyAudit()
        }

    "identifiers are matched but BV check is false, does not call BV (BV status None). Registration is called. The journey is audited" in {

      enable(EnableFullUAJourney)

      await(insertJourneyConfig(
        journeyId = testJourneyId,
        internalId = testInternalId,
        testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = false)
      ))

      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      stubRetrieveUtr(testJourneyId)(OK, testCtutrJson)
      stubRetrievePostcode(testJourneyId)(OK, testOfficePostcode)

      stubValidateUnincorporatedAssociationDetails(testCtutr, testOfficePostcode)(OK, Json.obj("matched" -> true))
      stubStoreIdentifiersMatch(testJourneyId, SuccessfulMatchKey)(OK)

      stubRetrieveBusinessVerificationStatus(testJourneyId)(NOT_FOUND)
      stubRegisterUA(testCtutr, testRegime)(OK, Registered(testSafeId))
      stubStoreRegistrationStatus(testJourneyId, testSuccessfulRegistrationJson)(OK)

      stubRetrieveEntityDetails(testJourneyId)(OK, testThisIsADummyJson)

      stubAudit()

      val result = post(s"/identify-your-unincorporated-association/$testJourneyId/check-your-answers-business")()

      result must have {
        httpStatus(SEE_OTHER)
        redirectUri(expectedValue = s"$testContinueUrl?journeyId=$testJourneyId")
      }

      verifyStoreIdentifiersMatch(testJourneyId, expBody = JsString(SuccessfulMatchKey))
      verifyStoreRegistrationStatus(testJourneyId, testSuccessfulRegistrationJson)
      verifyRegisterUA(testRegisterUAJson(testCtutr, testRegime))
      verifyAudit()
    }

        "the unincorporated association's validate details cannot be found, " +
          "does not call BV and it redirects to the cannot confirm business error page. " +
          "The journey is audited" in {

          enable(EnableFullUAJourney)

          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveUtr(testJourneyId)(OK, testCtutrJson)
          stubRetrievePostcode(testJourneyId)(OK, testOfficePostcode)

          stubValidateUnincorporatedAssociationDetails(testCtutr, testOfficePostcode)(BAD_REQUEST, Json.obj("code" -> "NOT_FOUND",
            "reason" -> "The back end has indicated that CT UTR cannot be returned"
          ))

          stubStoreIdentifiersMatch(testJourneyId, DetailsNotFoundKey)(OK)
          stubStoreBusinessVerificationStatus(testJourneyId, expBody = testVerificationStatusJson(verificationStatusValue = "NOT_ENOUGH_INFORMATION_TO_CALL_BV"))(OK)
          stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(OK)

          stubRetrieveEntityDetails(testJourneyId)(OK, testThisIsADummyJson)
          stubAudit()

          val result = post(s"/identify-your-unincorporated-association/$testJourneyId/check-your-answers-business")()

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(expectedValue = errorControllers.routes.CannotConfirmBusinessController.show(testJourneyId).url)
          }

          verifyStoreIdentifiersMatch(testJourneyId, expBody = JsString(DetailsNotFoundKey))
          verifyStoreBusinessVerificationStatus(testJourneyId, expBody = testVerificationStatusJson(verificationStatusValue = "NOT_ENOUGH_INFORMATION_TO_CALL_BV"))
          verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)
          verifyAudit()
        }

        "redirect to the full continue url" when {

          "the user provides no utr an no postcode. The journey is audited" in {

            enable(EnableFullUAJourney)

            await(insertJourneyConfig(
              journeyId = testJourneyId,
              internalId = testInternalId,
              testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
            ))

            stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
            stubRetrieveUtr(testJourneyId)(NOT_FOUND)
            stubRetrievePostcode(testJourneyId)(NOT_FOUND)

            stubStoreIdentifiersMatch(testJourneyId, UnMatchableKey)(OK)

            stubStoreBusinessVerificationStatus(testJourneyId, expBody = testVerificationStatusJson(verificationStatusValue = "NOT_ENOUGH_INFORMATION_TO_CALL_BV"))(OK)
            stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(OK)

            stubRetrieveEntityDetails(testJourneyId)(OK, testThisIsADummyJson)
            stubAudit()

            val result = post(s"/identify-your-unincorporated-association/$testJourneyId/check-your-answers-business")()

            result must have {
              httpStatus(SEE_OTHER)
              redirectUri(expectedValue = s"$testContinueUrl?journeyId=$testJourneyId")
            }

            verifyStoreIdentifiersMatch(testJourneyId, expBody = JsString(UnMatchableKey))
            verifyStoreBusinessVerificationStatus(testJourneyId, expBody = testVerificationStatusJson(verificationStatusValue = "NOT_ENOUGH_INFORMATION_TO_CALL_BV"))
            verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)
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
            stubValidateUnincorporatedAssociationDetails(testCtutr, testOfficePostcode)(NOT_FOUND)
            stubAudit()

            val result = post(s"/identify-your-unincorporated-association/$testJourneyId/check-your-answers-business")()

            result.status mustBe INTERNAL_SERVER_ERROR
          }
          "the user does not have an internal ID" in {
            enable(EnableFullUAJourney)
            stubAuth(OK, successfulAuthResponse(internalId = None))
            stubAudit()

            lazy val result: WSResponse = post(s"/identify-your-unincorporated-association/$testJourneyId/check-your-answers-business")()

            result.status mustBe INTERNAL_SERVER_ERROR

          }
        }

        "redirect to sign in page" when {
          "the user is UNAUTHORISED" in {
            enable(EnableFullUAJourney)
            stubAuthFailure()
            stubAudit()

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

        "throw an internal server exception" when {
          "the EnableFullTrustJourney is disabled" in {
            disable(EnableFullUAJourney)
            stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
            stubAudit()

            val result = post(s"/identify-your-unincorporated-association/$testJourneyId/check-your-answers-business")()

            result.status mustBe INTERNAL_SERVER_ERROR
          }
        }
  }

}
