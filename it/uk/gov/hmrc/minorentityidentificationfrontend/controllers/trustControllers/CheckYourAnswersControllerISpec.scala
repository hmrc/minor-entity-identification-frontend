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

import play.api.libs.json.JsString
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.core.config.{EnableFullTrustJourney, FeatureSwitching}
import uk.gov.hmrc.minorentityidentificationfrontend.models.KnownFactsMatchingResult._
import uk.gov.hmrc.minorentityidentificationfrontend.stubs.{AuthStub, BusinessVerificationStub, RetrieveTrustKnownFactsStub, StorageStub}
import uk.gov.hmrc.minorentityidentificationfrontend.utils.AuditEnabledSpecHelper
import uk.gov.hmrc.minorentityidentificationfrontend.utils.WiremockHelper.{stubAudit, verifyAudit}
import uk.gov.hmrc.minorentityidentificationfrontend.views.CheckYourAnswersCommonViewTests
import uk.gov.hmrc.minorentityidentificationfrontend.views.trustViews.TrustCheckYourAnswersSpecificViewTests

class CheckYourAnswersControllerISpec extends AuditEnabledSpecHelper
  with AuthStub
  with StorageStub
  with BusinessVerificationStub
  with CheckYourAnswersCommonViewTests
  with TrustCheckYourAnswersSpecificViewTests
  with RetrieveTrustKnownFactsStub
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
          stubRetrieveUtr(testJourneyId)(OK, testSautrJson)
          stubRetrievePostcode(testJourneyId)(OK, testSaPostcode)
          stubRetrieveCHRN(testJourneyId)(NOT_FOUND)
          stubAudit()

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
          stubRetrievePostcode(testJourneyId)(NOT_FOUND)
          stubAudit()

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
          stubRetrievePostcode(testJourneyId)(OK, testSaPostcode)
          stubAudit()

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
          stubAudit()

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
          stubAudit()

          lazy val result: WSResponse = get(s"/identify-your-trust/$testJourneyId/check-your-answers-business")

          result.status mustBe INTERNAL_SERVER_ERROR

        }
      }

      "the EnableFullTrustJourney is disabled" should {
        "throw an internal server exception" in {
          disable(EnableFullTrustJourney)
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubAudit()

          val result = get(s"/identify-your-trust/$testJourneyId/check-your-answers-business")

          result.status mustBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }

  "POST /identify-your-trust/check-your-answers-business" when {
    "the EnableFullTrustJourney is enabled and identifier match is SuccessfulMatch (for example all postcodes are the same)" should {
      "contact TrustKnownFacts api and create a BV journey. The redirect url is the BV start journey url" in {
        enable(EnableFullTrustJourney)

        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          testTrustsJourneyConfig(businessVerificationCheck = true)
        ))

        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        stubRetrieveUtr(testJourneyId)(OK, testSautrJson)
        stubRetrievePostcode(testJourneyId)(OK, testSaPostcode)
        stubRetrieveCHRN(testJourneyId)(NOT_FOUND)
        stubRetrieveTrustKnownFacts(testSautr)(OK, testKnownFactsJson(correspondencePostcode = testSaPostcode, declarationPostcode = testSaPostcode))
        stubStoreIdentifiersMatch(testJourneyId, SuccessfulMatchKey)(OK)
        stubRetrieveCHRN(testJourneyId)(NOT_FOUND)
        stubRetrieveTrustKnownFacts(testSautr)(OK, testKnownFactsJson(correspondencePostcode = testSaPostcode, declarationPostcode = testSaPostcode))
        stubCreateBusinessVerificationJourney(expBody = testCreateBusinessVerificationJourneyJson(testSautr, testJourneyId, testTrustsJourneyConfig(businessVerificationCheck = true)))(NOT_FOUND)
        stubStoreBusinessVerificationStatus(testJourneyId, expBody = testVerificationStatusJson(verificationStatusValue = "NOT_ENOUGH_INFORMATION_TO_CHALLENGE"))(OK)

        stubRetrieveIdentifiersMatch(testJourneyId)(OK, SuccessfulMatchKey)
        stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, testBusinessVerificationPassJson)
        stubRetrieveRegistrationStatus(testJourneyId)(OK, testSuccessfulRegistrationJson)
        stubAudit()

        val result = post(s"/identify-your-trust/$testJourneyId/check-your-answers-business")()

        result must have {
          httpStatus(SEE_OTHER)
          redirectUri(expectedValue = s"$testContinueUrl?journeyId=$testJourneyId")
        }

        verifyStoreIdentifiersMatch(testJourneyId, expBody = JsString(SuccessfulMatchKey))
        verifyStoreBusinessVerificationStatus(testJourneyId, expBody = testVerificationStatusJson(verificationStatusValue = "NOT_ENOUGH_INFORMATION_TO_CHALLENGE"))
        verifyCreateBusinessVerificationJourney(expBody = testCreateBusinessVerificationJourneyJson(testSautr, testJourneyId, testTrustsJourneyConfig(businessVerificationCheck = true)))
        verifyAudit()
      }
    }

    "the EnableFullTrustJourney is enabled and identifier match is SuccessfulMatch (for example all postcodes are the same)" should {
      "contact TrustKnownFacts api and try to create a BV journey. " +
        "Given BV returns FORBIDDEN a BV status is persisted. " +
        "The redirect url is the fullContinueUrl" in {
        enable(EnableFullTrustJourney)

        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          testTrustsJourneyConfig(businessVerificationCheck = true)
        ))

        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        stubRetrieveUtr(testJourneyId)(OK, testSautrJson)
        stubRetrievePostcode(testJourneyId)(OK, testSaPostcode)
        stubRetrieveCHRN(testJourneyId)(NOT_FOUND)
        stubRetrieveTrustKnownFacts(testSautr)(OK, testKnownFactsJson(correspondencePostcode = testSaPostcode, declarationPostcode = testSaPostcode))
        stubStoreIdentifiersMatch(testJourneyId, SuccessfulMatchKey)(OK)
        stubCreateBusinessVerificationJourney(expBody = testCreateBusinessVerificationJourneyJson(testSautr, testJourneyId, testTrustsJourneyConfig(businessVerificationCheck = true)))(FORBIDDEN)
        stubStoreBusinessVerificationStatus(testJourneyId, expBody = testVerificationStatusJson(verificationStatusValue = "FAIL"))(OK)
        stubAudit()
        stubRetrieveIdentifiersMatch(testJourneyId)(OK, SuccessfulMatchKey)
        stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, testBusinessVerificationFailJson)
        stubRetrieveRegistrationStatus(testJourneyId)(OK, testRegistrationNotCalledJson)

        val result = post(s"/identify-your-trust/$testJourneyId/check-your-answers-business")()

        result must have {
          httpStatus(SEE_OTHER)
          redirectUri(expectedValue = s"$testContinueUrl?journeyId=$testJourneyId")
        }

        verifyStoreIdentifiersMatch(testJourneyId, expBody = JsString(SuccessfulMatchKey))
        verifyCreateBusinessVerificationJourney(expBody = testCreateBusinessVerificationJourneyJson(testSautr, testJourneyId, testTrustsJourneyConfig(businessVerificationCheck = true)))
        verifyStoreBusinessVerificationStatus(testJourneyId, expBody = testVerificationStatusJson(verificationStatusValue = "FAIL"))
        verifyAudit()
      }
    }

    "the EnableFullTrustJourney is enabled and identifier match is DetailsMismatch (for example all postcodes are different)" should {
      "persist NOT_ENOUGH_INFORMATION_TO_CALL_BV BV status " +
        "and redirect to the Cannot Confirm Business error page after contacting TrustKnownFacts api" in {
        enable(EnableFullTrustJourney)

        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          testTrustsJourneyConfig(businessVerificationCheck = true)
        ))

        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        stubRetrieveUtr(testJourneyId)(OK, testSautrJson)
        stubRetrievePostcode(testJourneyId)(OK, testSaPostcode)

        val correspondencePostcode = testSaPostcode + "X"
        val declarationPostcode = testSaPostcode + "Y"

        stubRetrieveTrustKnownFacts(testSautr)(OK, testKnownFactsJson(correspondencePostcode, declarationPostcode))
        stubStoreIdentifiersMatch(testJourneyId, DetailsMismatchKey)(OK)
        stubStoreBusinessVerificationStatus(testJourneyId, expBody = testVerificationStatusJson(verificationStatusValue = "NOT_ENOUGH_INFORMATION_TO_CALL_BV"))(OK)

        stubRetrieveCHRN(testJourneyId)(NOT_FOUND)
        stubRetrieveIdentifiersMatch(testJourneyId)(OK, DetailsMismatchKey)
        stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, testBusinessVerificationNotEnoughInfoToCallJson)
        stubRetrieveRegistrationStatus(testJourneyId)(OK, testRegistrationNotCalledJson)

        stubAudit()

        val result = post(s"/identify-your-trust/$testJourneyId/check-your-answers-business")()

        result must have {
          httpStatus(SEE_OTHER)
          redirectUri(expectedValue = errorControllers.routes.CannotConfirmBusinessController.show(testJourneyId).url)
        }

        verifyStoreIdentifiersMatch(testJourneyId, expBody = JsString(DetailsMismatchKey))
        verifyStoreBusinessVerificationStatus(testJourneyId, expBody = testVerificationStatusJson(verificationStatusValue = "NOT_ENOUGH_INFORMATION_TO_CALL_BV"))
        verifyAudit()
      }
    }

    "the EnableFullTrustJourney is enabled and identifier match is UnMatchableWithoutRetry (No SaUtr but CHRN provided)" should {
      "redirect to the provided continueUrl without contacting TrustKnownFacts api and BV" in {
        enable(EnableFullTrustJourney)

        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          testTrustsJourneyConfig(businessVerificationCheck = true)
        ))

        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        stubRetrieveUtr(testJourneyId)(NOT_FOUND)
        stubRetrievePostcode(testJourneyId)(NOT_FOUND)
        stubRetrieveCHRN(testJourneyId)(OK, testCHRN)
        stubStoreIdentifiersMatch(testJourneyId, UnMatchableWithoutRetryKey)(OK)
        stubRetrieveIdentifiersMatch(testJourneyId)(OK, UnMatchableWithoutRetryKey)
        stubStoreBusinessVerificationStatus(testJourneyId, expBody = testVerificationStatusJson(verificationStatusValue = "NOT_ENOUGH_INFORMATION_TO_CALL_BV"))(OK)
        stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, testBusinessVerificationNotEnoughInfoToCallJson)
        stubRetrieveRegistrationStatus(testJourneyId)(OK, testRegistrationNotCalledJson)
        stubAudit()

        val result = post(s"/identify-your-trust/$testJourneyId/check-your-answers-business")()

        result must have {
          httpStatus(SEE_OTHER)
          redirectUri(expectedValue = s"$testContinueUrl?journeyId=$testJourneyId")
        }

        verifyStoreIdentifiersMatch(testJourneyId, expBody = JsString(UnMatchableWithoutRetryKey))
        verifyStoreBusinessVerificationStatus(testJourneyId, expBody = testVerificationStatusJson(verificationStatusValue = "NOT_ENOUGH_INFORMATION_TO_CALL_BV"))
        verifyAudit()
      }
    }

    "the EnableFullTrustJourney is enabled and identifier match is UnMatchableWithRetry (No SaUtr and No CHRN provided)" should {
      "redirect to the Cannot Confirm Business error page without contacting TrustKnownFacts api and BV" in {
        enable(EnableFullTrustJourney)

        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          testTrustsJourneyConfig(businessVerificationCheck = true)
        ))

        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        stubRetrieveUtr(testJourneyId)(NOT_FOUND)
        stubRetrievePostcode(testJourneyId)(NOT_FOUND)
        stubRetrieveCHRN(testJourneyId)(NOT_FOUND)
        stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = UnMatchableWithRetryKey)(OK)
        stubRetrieveIdentifiersMatch(testJourneyId)(OK, UnMatchableWithRetryKey)
        stubStoreBusinessVerificationStatus(testJourneyId, expBody = testVerificationStatusJson(verificationStatusValue = "NOT_ENOUGH_INFORMATION_TO_CALL_BV"))(OK)
        stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, testBusinessVerificationNotEnoughInfoToCallJson)
        stubRetrieveRegistrationStatus(testJourneyId)(OK, testRegistrationNotCalledJson)
        stubAudit()

        val result = post(s"/identify-your-trust/$testJourneyId/check-your-answers-business")()

        result must have {
          httpStatus(SEE_OTHER)
          redirectUri(expectedValue = errorControllers.routes.CannotConfirmBusinessController.show(testJourneyId).url)
        }

        verifyStoreIdentifiersMatch(testJourneyId, expBody = JsString(UnMatchableWithRetryKey))
        verifyStoreBusinessVerificationStatus(testJourneyId, expBody = testVerificationStatusJson(verificationStatusValue = "NOT_ENOUGH_INFORMATION_TO_CALL_BV"))
        verifyAudit()
      }
    }

  }

  "POST /identify-your-trust/check-your-answers-business" when {

    "the user is UNAUTHORISED" should {
      "redirect to sign in page" in {
        enable(EnableFullTrustJourney)
        stubAuthFailure()
        stubAudit()

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
        stubAudit()

        lazy val result: WSResponse = post(s"/identify-your-trust/$testJourneyId/check-your-answers-business")()

        result.status mustBe INTERNAL_SERVER_ERROR

      }
    }
    "the EnableFullTrustJourney is disabled" should {
      "throw an internal server exception" in {
        disable(EnableFullTrustJourney)
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubAudit()

        val result = post(s"/identify-your-trust/$testJourneyId/check-your-answers-business")()

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

}
