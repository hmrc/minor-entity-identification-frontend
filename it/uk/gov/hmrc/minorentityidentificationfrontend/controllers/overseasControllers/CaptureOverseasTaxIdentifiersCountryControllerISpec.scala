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
import play.api.test.Helpers.{BAD_REQUEST, OK, SEE_OTHER, await, defaultAwaitTimeout}
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.overseasControllers
import uk.gov.hmrc.minorentityidentificationfrontend.stubs.{AuthStub, StorageStub}
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.minorentityidentificationfrontend.views.overseasViews.CaptureOverseasTaxIdentifiersCountryTests


class CaptureOverseasTaxIdentifiersCountryControllerISpec extends ComponentSpecHelper
  with AuthStub
  with StorageStub
  with CaptureOverseasTaxIdentifiersCountryTests {

  "GET /overseas-tax-identifier-country" should {
    lazy val result = {
      await(insertJourneyConfig(
        journeyId = testJourneyId,
        internalId = testInternalId,
        testOverseasCompanyJourneyConfig(businessVerificationCheck = true)
      ))
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      get(s"/identify-your-overseas-business/$testJourneyId/overseas-tax-identifier-country")
    }

    "return OK" in {
      result.status mustBe OK
    }

    "return a view which" should {
      testCaptureOverseasTaxIdentifiersCountryView(result)
    }

    "redirect to sign in page" when {
      "the user is UNAUTHORISED" in {
        stubAuthFailure()
        lazy val result: WSResponse = get(s"/identify-your-overseas-business/$testJourneyId/overseas-tax-identifier-country")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri("/bas-gateway/sign-in" +
            s"?continue_url=%2Fidentify-your-overseas-business%2F$testJourneyId%2Foverseas-tax-identifier-country" +
            "&origin=minor-entity-identification-frontend"
          )
        )
      }
    }
  }

  "POST /overseas-tax-identifier-country" when {
    "the tax identifiers country is correctly input" should {
      "redirect to Check Your Answers" in {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          testOverseasCompanyJourneyConfig(businessVerificationCheck = true)
        ))
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubStoreOverseasTaxIdentifiersCountry(testJourneyId, testOverseasTaxIdentifiersCountry)(OK)

        lazy val result = post(s"/identify-your-overseas-business/$testJourneyId/overseas-tax-identifier-country"
        )("country" -> testOverseasTaxIdentifiersCountry)

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(overseasControllers.routes.CheckYourAnswersController.show(testJourneyId).url)
        )

        verifyStoreOverseasTaxIdentifierCountry(testJourneyId, testOverseasTaxIdentifiersCountry)
      }
    }
    "no country is submitted" should {
      lazy val result = {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          testOverseasCompanyJourneyConfig(businessVerificationCheck = true)
        ))
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        post(s"/identify-your-overseas-business/$testJourneyId/overseas-tax-identifier-country"
        )("country" -> "")
      }

      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }

      testCaptureOverseasTaxIdentifiersCountryErrorMessages(result)
    }
    "the country name submitted is invalid" should {
      lazy val result: WSResponse = {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          testOverseasCompanyJourneyConfig(businessVerificationCheck = true)
        ))

        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        post(uri = s"/identify-your-overseas-business/$testJourneyId/overseas-tax-identifier-country"
        )("countryAutocomplete" -> "unknown") // If the country is not recognised the user's input will be submitted with the key "countryAutocomplete"
      }

      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }

      testCaptureOverseasTaxIdentifiersCountryErrorMessages(result)
    }
  }
}
