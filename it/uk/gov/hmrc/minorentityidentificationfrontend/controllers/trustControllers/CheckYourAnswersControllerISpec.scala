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

package uk.gov.hmrc.minorentityidentificationfrontend.controllers.trustControllers

import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.trustControllers.errorControllers.{routes => errorRoutes}
import uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.core.config.{EnableFullTrustJourney, FeatureSwitching}
import uk.gov.hmrc.minorentityidentificationfrontend.stubs.{AuthStub, StorageStub}
import uk.gov.hmrc.minorentityidentificationfrontend.utils.AuditEnabledSpecHelper
import uk.gov.hmrc.minorentityidentificationfrontend.views.{CheckYourAnswersCommonViewTests, TrustCheckYourAnswersSpecificViewTests}

class CheckYourAnswersControllerISpec extends AuditEnabledSpecHelper
  with AuthStub
  with StorageStub
  with CheckYourAnswersCommonViewTests
  with TrustCheckYourAnswersSpecificViewTests
  with FeatureSwitching {

  "GET /identify-your-trust/<testJourneyId>/check-your-answers-business" when {

    "the EnableFullTrustJourney is enabled" when {

      "the applicant has a utr and a postcode" should {
        enable(EnableFullTrustJourney)
        lazy val result: WSResponse = {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testTrustsJourneyConfig(businessVerificationCheck = true)
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveUtr(testJourneyId)(OK, testUtrJson)
          stubRetrieveSaPostcode(testJourneyId)(OK, testSaPostcode)
          stubRetrieveCHRN(testJourneyId)(NOT_FOUND)

          get(s"/identify-your-trust/$testJourneyId/check-your-answers-business")
        }

        "return OK" in {
          result.status mustBe OK
        }

        "return a view which" should {
          testCheckYourAnswersCommonView(result)
          testTrustWithUtrAndPostcodeSummaryListView(result, testJourneyId)
        }

      }

      "the applicant has a no utr and a no charity hmrc reference number" should {
        enable(EnableFullTrustJourney)
        lazy val result: WSResponse = {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testTrustsJourneyConfig(businessVerificationCheck = true)
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveUtr(testJourneyId)(NOT_FOUND)
          stubRetrieveCHRN(testJourneyId)(NOT_FOUND)
          stubRetrieveSaPostcode(testJourneyId)(NOT_FOUND)

          get(s"/identify-your-trust/$testJourneyId/check-your-answers-business")
        }

        "return OK" in {
          result.status mustBe OK
        }

        "return a view which" should {
          testCheckYourAnswersCommonView(result)
          testTrustWithNoUtrAndNoCharityHRMCReferenceNumberSummaryListView(result, testJourneyId)
        }

      }

      "the applicant has a postcode but they dont have utr (this is a impossible scenario)" should {
        enable(EnableFullTrustJourney)
        lazy val result: WSResponse = {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testTrustsJourneyConfig(businessVerificationCheck = true)
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveUtr(testJourneyId)(NOT_FOUND)
          stubRetrieveSaPostcode(testJourneyId)(OK, testSaPostcode)

          get(s"/identify-your-trust/$testJourneyId/check-your-answers-business")
        }

        "return INTERNAL_SERVER_ERROR" in {
          result.status mustBe INTERNAL_SERVER_ERROR
        }

      }

      "the user is UNAUTHORISED" should {
        "redirect to sign in page" in {
          enable(EnableFullTrustJourney)
          stubAuthFailure()

          lazy val result: WSResponse = get(s"/identify-your-trust/$testJourneyId/check-your-answers-business")

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri("/bas-gateway/sign-in" +
              s"?continue_url=%2Fidentify-your-trust%2F$testJourneyId%2Fcheck-your-answers-business" +
              "&origin=minor-entity-identification-frontend"
            )
          }
        }
      }

      "the user does not have an internal ID" should {
        "return an INTERNAL_SERVER_ERROR status" in {
          enable(EnableFullTrustJourney)
          stubAuth(OK, successfulAuthResponse(internalId = None))

          lazy val result: WSResponse = get(s"/identify-your-trust/$testJourneyId/check-your-answers-business")

          result.status mustBe INTERNAL_SERVER_ERROR

        }
      }

      "the EnableFullTrustJourney is disabled" should {
        "throw an internal server exception" in {
          disable(EnableFullTrustJourney)
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

          val result = get(s"/identify-your-trust/$testJourneyId/check-your-answers-business")

          result.status mustBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }

  "POST /identify-your-trust/check-your-answers-business" when {
    "the EnableFullTrustJourney is enabled" should {
      "redirect to the provided continueUrl" in {
        enable(EnableFullTrustJourney)
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          testTrustsJourneyConfig(businessVerificationCheck = true)
        ))

        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRetrieveUtr(testJourneyId)(OK, testUtrJson)
        stubRetrieveCHRN(testJourneyId)(OK, testCHRN)

        val result = post(s"/identify-your-trust/$testJourneyId/check-your-answers-business")()

        result must have {
          httpStatus(SEE_OTHER)
          redirectUri(expectedValue = s"$testContinueUrl?journeyId=$testJourneyId")
        }
      }
      "redirect to the Cannot Confirm Business error page" when {
        "the user has no sautr and no chrn" in {
          enable(EnableFullTrustJourney)
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testTrustsJourneyConfig(businessVerificationCheck = true)
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveUtr(testJourneyId)(NOT_FOUND)
          stubRetrieveCHRN(testJourneyId)(NOT_FOUND)

          val result = post(s"/identify-your-trust/$testJourneyId/check-your-answers-business")()

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri(expectedValue = errorRoutes.CannotConfirmBusinessController.show(testJourneyId).url)
          }
        }
      }

      "the user is UNAUTHORISED" should {
        "redirect to sign in page" in {
          enable(EnableFullTrustJourney)
          stubAuthFailure()

          lazy val result: WSResponse = post(s"/identify-your-trust/$testJourneyId/check-your-answers-business")()

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri("/bas-gateway/sign-in" +
              s"?continue_url=%2Fidentify-your-trust%2F$testJourneyId%2Fcheck-your-answers-business" +
              "&origin=minor-entity-identification-frontend"
            )
          }
        }
      }

      "return an INTERNAL_SERVER_ERROR status" when {
        "the user does not have an internal ID" in {
          enable(EnableFullTrustJourney)
          stubAuth(OK, successfulAuthResponse(internalId = None))

          lazy val result: WSResponse = post(s"/identify-your-trust/$testJourneyId/check-your-answers-business")()

          result.status mustBe INTERNAL_SERVER_ERROR

        }
      }
    }
    "the EnableFullTrustJourney is disabled" should {
      "throw an internal server exception" in {
        disable(EnableFullTrustJourney)
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        val result = post(s"/identify-your-trust/$testJourneyId/check-your-answers-business")()

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

}
