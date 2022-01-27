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

package uk.gov.hmrc.minorentityidentificationfrontend.controllers

import play.api.http.Status.NO_CONTENT
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.models.BusinessEntity.OverseasCompany
import uk.gov.hmrc.minorentityidentificationfrontend.stubs.{AuthStub, StorageStub}
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ComponentSpecHelper
import play.api.test.Helpers.{BAD_REQUEST, OK, SEE_OTHER, INTERNAL_SERVER_ERROR, await, defaultAwaitTimeout}
import uk.gov.hmrc.minorentityidentificationfrontend.views.CaptureOverseasTaxIdentifiersTests


class CaptureOverseasTaxIdentifiersControllerISpec extends ComponentSpecHelper
  with AuthStub
  with StorageStub
  with CaptureOverseasTaxIdentifiersTests {

  "GET /overseas-identifier" should {
    lazy val result = {
      await(insertJourneyConfig(
        journeyId = testJourneyId,
        internalId = testInternalId,
        continueUrl = testContinueUrl,
        optServiceName = None,
        deskProServiceId = testDeskProServiceId,
        signOutUrl = testSignOutUrl,
        accessibilityUrl = testAccessibilityUrl,
        OverseasCompany
      ))
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      get(s"/identify-your-overseas-business/$testJourneyId/overseas-identifier")
    }

    "return OK" in {
      result.status mustBe OK
    }

    "return a view which" should {
      testCaptureCaptureOverseasTaxIdentifiersView(result)
    }

    "redirect to sign in page" when {
      "the user is UNAUTHORISED" in {
        stubAuthFailure()
        lazy val result: WSResponse = get(s"/identify-your-overseas-business/$testJourneyId/overseas-identifier")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri("/bas-gateway/sign-in" +
            s"?continue_url=%2Fidentify-your-overseas-business%2F$testJourneyId%2Foverseas-identifier" +
            "&origin=minor-entity-identification-frontend"
          )
        )
      }
    }
  }

  "POST /overseas-identifier" when {
    "the tax identifiers are correctly formatted" should {
      "redirect to Check Your Answers" in {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          continueUrl = testContinueUrl,
          optServiceName = None,
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl,
          accessibilityUrl = testAccessibilityUrl,
          OverseasCompany
        ))
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubStoreOverseasTaxIdentifiers(testJourneyId, testOverseasTaxIdentifiers)(OK)

        lazy val result = post(s"/identify-your-overseas-business/$testJourneyId/overseas-identifier"
        )("tax-identifier" -> "134124532",
          "country" -> "AL")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CheckYourAnswersController.show(testJourneyId).url)
        )
      }
    }
    "no tax identifier or country is submitted" should {
      lazy val result = {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          continueUrl = testContinueUrl,
          optServiceName = None,
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl,
          accessibilityUrl = testAccessibilityUrl,
          OverseasCompany
        ))
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        post(s"/identify-your-overseas-business/$testJourneyId/overseas-identifier"
        )("tax-identifier" -> "",
          "country" -> "")
      }

      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }

      testCaptureCaptureOverseasTaxIdentifiersErrorMessages(result)
    }

    "an invalid tax identifier is submitted" should {
      lazy val result = {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          continueUrl = testContinueUrl,
          optServiceName = None,
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl,
          accessibilityUrl = testAccessibilityUrl,
          OverseasCompany
        ))
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        post(s"/identify-your-overseas-business/$testJourneyId/overseas-identifier"
        )("tax-identifier" -> "134124532$$$",
          "country" -> "AL")
      }

      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }

      testCaptureCaptureOverseasTaxIdentifiersErrorMessagesInvalidIdentifier(result)
    }

    "a tax identifier that is too long is submitted" should {
      lazy val result = {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          continueUrl = testContinueUrl,
          optServiceName = None,
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl,
          accessibilityUrl = testAccessibilityUrl,
          OverseasCompany
        ))
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        post(s"/identify-your-overseas-business/$testJourneyId/overseas-identifier"
        )("tax-identifier" -> "13412453134124531341245313412453134124531341245313412453134124531341245313412453134124531341245313412453134124531341245313412453134124531341245313412453134124531341245313412453134124531341245313412453",
          "country" -> "AL")
      }

      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }

      testCaptureCaptureOverseasTaxIdentifiersErrorMessagesTooLongIdentifier(result)
    }
  }

  "GET /no-overseas-identifier" should {
    "redirect to CYA page" when {
      "the overseas identifiers are successfully removed" in {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          continueUrl = testContinueUrl,
          optServiceName = None,
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl,
          accessibilityUrl = testAccessibilityUrl,
          OverseasCompany
        ))
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRemoveOverseasTaxIdentifiers(testJourneyId)(NO_CONTENT)

        val result = get(s"/identify-your-overseas-business/$testJourneyId/no-overseas-identifier")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CheckYourAnswersController.show(testJourneyId).url)
        )
      }
    }

    "throw an exception" when {
      "the backend returns a failure" in {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          continueUrl = testContinueUrl,
          optServiceName = None,
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl,
          accessibilityUrl = testAccessibilityUrl,
          OverseasCompany
        ))
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRemoveOverseasTaxIdentifiers(testJourneyId)(INTERNAL_SERVER_ERROR, "Failed to remove field")

        val result = get(s"/identify-your-overseas-business/$testJourneyId/no-overseas-identifier")

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

}
