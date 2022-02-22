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

package uk.gov.hmrc.minorentityidentificationfrontend.controllers.trustControllers.errorControllers

import play.api.libs.ws.WSResponse
import play.api.test.Helpers.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NO_CONTENT, OK, SEE_OTHER, await, defaultAwaitTimeout}
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants.{testContinueUrl, testInternalId, testJourneyId, testTrustsJourneyConfig}
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.trustControllers.{routes => trustRoutes}
import uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.core.config.{EnableFullTrustJourney, FeatureSwitching}
import uk.gov.hmrc.minorentityidentificationfrontend.stubs.{AuthStub, StorageStub}
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.minorentityidentificationfrontend.views.trustViews.CannotConfirmBusinessViewTests

class CannotConfirmBusinessControllerISpec extends ComponentSpecHelper
  with AuthStub
  with StorageStub
  with CannotConfirmBusinessViewTests
  with FeatureSwitching {

  "GET /cannot-confirm-business" when {
    "the EnableFullTrustJourney is enabled" should {
      enable(EnableFullTrustJourney)
      lazy val result = {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testTrustsJourneyConfig(true)
        ))
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        get(s"/identify-your-trust/$testJourneyId/cannot-confirm-business")
      }

      "return OK" in {
        result.status mustBe OK
      }

      "return a view which" should {
        testCannotConfirmBusinessView(result)
      }

      "redirect to sign in page" when {
        "the user is UNAUTHORISED" in {
          enable(EnableFullTrustJourney)
          stubAuthFailure()
          lazy val result: WSResponse = get(s"/identify-your-trust/$testJourneyId/cannot-confirm-business")
          result must have(
            httpStatus(SEE_OTHER),
            redirectUri("/bas-gateway/sign-in" +
              s"?continue_url=%2Fidentify-your-trust%2F$testJourneyId%2Fcannot-confirm-business" +
              "&origin=minor-entity-identification-frontend"
            )
          )
        }
      }
    }
    "the EnableFullTrustJourney is disabled" should {
      "throw an internal server exception" in {
        disable(EnableFullTrustJourney)
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        val result = get(s"/identify-your-trust/$testJourneyId/cannot-confirm-business")

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  "POST /cannot-confirm-business" when {
    "the EnableFullTrustJourney is enabled" when {
      "the user selects yes" should {
        "redirect to the continue url" in {
          enable(EnableFullTrustJourney)
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testTrustsJourneyConfig(true)
          ))
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

          lazy val result = post(s"/identify-your-trust/$testJourneyId/cannot-confirm-business")(
            "yes_no" -> "yes"
          )
          result must have(
            httpStatus(SEE_OTHER),
            redirectUri(testContinueUrl + s"?journeyId=$testJourneyId")
          )
        }
      }
      "the user selects no" should {
        "redirect to the capture sautr page" in {
          enable(EnableFullTrustJourney)
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testTrustsJourneyConfig(true)
          ))
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRemoveAllData(testJourneyId)(NO_CONTENT)

          lazy val result = post(s"/identify-your-trust/$testJourneyId/cannot-confirm-business")(
            "yes_no" -> "no"
          )
          result must have(
            httpStatus(SEE_OTHER),
            redirectUri(trustRoutes.CaptureSautrController.show(testJourneyId).url)
          )
        }
      }
      "the user selects no radio box" should {
        enable(EnableFullTrustJourney)
        lazy val result = {
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testTrustsJourneyConfig(true)
          ))
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRemoveAllData(testJourneyId)(NO_CONTENT)
          post(s"/identify-your-trust/$testJourneyId/cannot-confirm-business")()
        }

        "return a bad request" in {
          result.status mustBe BAD_REQUEST
        }

        testCannotConfirmBusinessErrorView(result)
      }
      "redirect to sign in page" when {
        "the user is UNAUTHORISED" in {
          enable(EnableFullTrustJourney)
          stubAuthFailure()
          lazy val result: WSResponse = post(s"/identify-your-trust/$testJourneyId/cannot-confirm-business")(
            "yes_no" -> "no"
          )

          result must have(
            httpStatus(SEE_OTHER),
            redirectUri("/bas-gateway/sign-in" +
              s"?continue_url=%2Fidentify-your-trust%2F$testJourneyId%2Fcannot-confirm-business" +
              "&origin=minor-entity-identification-frontend"
            )
          )
        }
      }
    }
    "the EnableFullTrustJourney is disabled" should {
      "throw an internal server exception" in {
        disable(EnableFullTrustJourney)
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        val result = post(s"/identify-your-trust/$testJourneyId/cannot-confirm-business")(
          "yes_no" -> "no"
        )

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
