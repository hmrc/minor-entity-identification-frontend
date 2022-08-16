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

package uk.gov.hmrc.minorentityidentificationfrontend.controllers.overseasControllers

import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.stubs.{AuthStub, StorageStub}
import uk.gov.hmrc.minorentityidentificationfrontend.utils.AuditEnabledSpecHelper
import uk.gov.hmrc.minorentityidentificationfrontend.utils.WiremockHelper.{stubAudit, verifyAudit}
import uk.gov.hmrc.minorentityidentificationfrontend.views.CheckYourAnswersCommonViewTests
import uk.gov.hmrc.minorentityidentificationfrontend.views.overseasViews.OverseasCheckYourAnswersSpecificViewTests

class OverseasCheckYourAnswersControllerISpec extends AuditEnabledSpecHelper
  with AuthStub
  with StorageStub
  with CheckYourAnswersCommonViewTests
  with OverseasCheckYourAnswersSpecificViewTests {

  "GET /check-your-answers-business" when {
    "the applicant has a sautr and an overseas tax identifier" should {
      lazy val result: WSResponse = {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          testOverseasCompanyJourneyConfig(businessVerificationCheck = true)
        ))
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRetrieveUtr(testJourneyId)(OK, testSautrJson)
        stubAudit()
        stubRetrieveOverseasTaxIdentifier(testJourneyId)(OK, testOverseasTaxIdentifier)
        stubRetrieveOverseasTaxIdentifiersCountry(testJourneyId)(OK, testOverseasTaxIdentifiersCountry)

        get(s"/identify-your-overseas-business/$testJourneyId/check-your-answers-business")
      }

      "return OK" in {
        result.status mustBe OK
      }

      "return a view which" should {
        testCheckYourAnswersCommonView(result)
        testOverseasSummaryViewWithUtrAndOverseasTaxIdentifier(result, testJourneyId)
      }

    }

    "the applicant does not provide a sautr and an overseas tax identifier" should {
      lazy val result: WSResponse = {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          testOverseasCompanyJourneyConfig(businessVerificationCheck = true)
        ))
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRetrieveUtr(testJourneyId)(NOT_FOUND)
        stubRetrieveOverseasTaxIdentifier(testJourneyId)(NOT_FOUND)
        stubRetrieveOverseasTaxIdentifiersCountry(testJourneyId)(NOT_FOUND)
        stubRetrieveOverseasTaxIdentifiers(testJourneyId)(NOT_FOUND)
        stubAudit()

        get(s"/identify-your-overseas-business/$testJourneyId/check-your-answers-business")
      }

      "return OK" in {
        result.status mustBe OK
      }

      "return a view which" should {
        testCheckYourAnswersCommonView(result)
        testOverseasSummaryViewWithUtrAndOverseasTaxIdentifierNotProvided(result, testJourneyId)
      }

    }

    "the user is UNAUTHORISED" should {
      "redirect to sign in page" in {
        stubAuthFailure()
        stubAudit()

        lazy val result: WSResponse = get(s"/identify-your-overseas-business/$testJourneyId/check-your-answers-business")

        result must have {
          httpStatus(SEE_OTHER)
          redirectUri("/bas-gateway/sign-in" +
            s"?continue_url=%2Fidentify-your-overseas-business%2F$testJourneyId%2Fcheck-your-answers-business" +
            "&origin=minor-entity-identification-frontend"
          )
        }
      }
    }

    "the internal authorization identifier is undefined" should {
      "raise an internal server exception" in {
        lazy val result: WSResponse = {
          stubAuth(OK, successfulAuthResponse(None))
          stubAudit()

          get(s"/identify-your-overseas-business/$testJourneyId/check-your-answers-business")
        }

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }

  }

  "POST /check-your-answers-business" should {
    "redirect to the provided continueUrl" in {
      await(insertJourneyConfig(
        journeyId = testJourneyId,
        internalId = testInternalId,
        testOverseasCompanyJourneyConfig(businessVerificationCheck = true)
      ))
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      stubRetrieveUtr(testJourneyId)(OK, testSautrJson)
      stubRetrieveOverseasTaxIdentifiers(testJourneyId)(OK, testOverseasTaxIdentifiersJson)
      stubRetrieveEntityDetails(testJourneyId)(OK, testOverseasJourneyDataJson(testSautrJson))
      stubAudit()

      val result = post(s"/identify-your-overseas-business/$testJourneyId/check-your-answers-business")()

      result must have {
        httpStatus(SEE_OTHER)
        redirectUri(expectedValue = s"$testContinueUrl?journeyId=$testJourneyId")
      }
      verifyAudit()
    }

    "raise an internal server exception" when {
      "the internal authorization identifier is undefined" in {
        lazy val result: WSResponse = {
          stubAuth(OK, successfulAuthResponse(None))
          stubAudit()

          post(s"/identify-your-overseas-business/$testJourneyId/check-your-answers-business")()
        }

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

}
