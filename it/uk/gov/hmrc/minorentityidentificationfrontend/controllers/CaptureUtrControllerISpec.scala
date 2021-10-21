/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.libs.ws.WSResponse
import play.api.test.Helpers.{BAD_REQUEST, OK, SEE_OTHER, await, defaultAwaitTimeout}
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.models.BusinessEntity.OverseasCompany
import uk.gov.hmrc.minorentityidentificationfrontend.models.{Ctutr, Sautr}
import uk.gov.hmrc.minorentityidentificationfrontend.stubs.{AuthStub, StorageStub}
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.minorentityidentificationfrontend.views.CaptureUtrViewTests

class CaptureUtrControllerISpec extends ComponentSpecHelper with AuthStub with StorageStub with CaptureUtrViewTests {

  "GET /non-uk-company-utr" should {
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
      get(s"/identify-your-overseas-business/$testJourneyId/non-uk-company-utr")
    }

    "return OK" in {
      result.status mustBe OK
    }

    "return a view which" should {
      testCaptureUtrView(result)
    }

    "redirect to sign in page" when {
      "the user is UNAUTHORISED" in {
        stubAuthFailure()
        lazy val result: WSResponse = get(s"/identify-your-overseas-business/$testJourneyId/non-uk-company-utr")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri("/bas-gateway/sign-in" +
            s"?continue_url=%2Fidentify-your-overseas-business%2F$testJourneyId%2Fnon-uk-company-utr" +
            "&origin=minor-entity-identification-frontend"
          )
        )
      }
    }
  }

  "POST /non-uk-company-utr" when {
    "the utr is correctly formatted" should {
      "redirect to check your answers" in {
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
        stubStoreUtr(testJourneyId, Sautr(testUtr))(OK)

        lazy val result = post(s"/identify-your-overseas-business/$testJourneyId/non-uk-company-utr")("utr" -> testUtr)

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CheckYourAnswersController.show(testJourneyId).url)
        )
      }
    }

    "the utr is a ctutr" should {
      "redirect to check your answers" in {
        val testCtutr = "1234529999"

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
        stubStoreUtr(testJourneyId, Ctutr(testCtutr))(OK)

        lazy val result = post(s"/identify-your-overseas-business/$testJourneyId/non-uk-company-utr")("utr" -> testCtutr)

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CheckYourAnswersController.show(testJourneyId).url)
        )
      }
    }

    "the utr is an sautr" should {
      "redirect to check your answers" in {
        val testSautr = "1234530000"

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
        stubStoreUtr(testJourneyId, Sautr(testSautr))(OK)

        lazy val result = post(s"/identify-your-overseas-business/$testJourneyId/non-uk-company-utr")("utr" -> testSautr)

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CheckYourAnswersController.show(testJourneyId).url)
        )
      }
    }

    "no utr is submitted" should {
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
        post(s"/identify-your-overseas-business/$testJourneyId/non-uk-company-utr")("utr" -> "")
      }

      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }

      testCaptureUtrViewNoUtr(result)
    }

    "the utr is in an invalid format" should {
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
        post(s"/identify-your-overseas-business/$testJourneyId/non-uk-company-utr")("utr" -> "1@34567890")
      }

      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }

      testCaptureUtrViewInvalidUtr(result)
    }

    "the utr is too long" should {
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
        post(s"/identify-your-overseas-business/$testJourneyId/non-uk-company-utr")("utr" -> "123456789123456789")
      }

      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }

      testCaptureUtrViewInvalidUtrLength(result)
    }
  }

}
