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

import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR}
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.{OK, SEE_OTHER, await, defaultAwaitTimeout}
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.core.config.{EnableFullTrustJourney, FeatureSwitching}
import uk.gov.hmrc.minorentityidentificationfrontend.models.Sautr
import uk.gov.hmrc.minorentityidentificationfrontend.stubs.{AuthStub, StorageStub}
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.minorentityidentificationfrontend.views.trustViews.TrustCaptureUtrViewTests

class CaptureSautrControllerISpec extends ComponentSpecHelper
  with AuthStub with StorageStub with TrustCaptureUtrViewTests with FeatureSwitching {

  "GET /sa-utr" when {

    "the user is authorized" when {

      "the feature switch is enabled" should {

        lazy val result = {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testTrustsJourneyConfig(businessVerificationCheck = true)
          ))
          enable(EnableFullTrustJourney)
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          get(s"/identify-your-trust/$testJourneyId/sa-utr")
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
            lazy val result: WSResponse = get(s"/identify-your-trust/$testJourneyId/sa-utr")

            result must have(
              httpStatus(SEE_OTHER),
              redirectUri("/bas-gateway/sign-in" +
                s"?continue_url=%2Fidentify-your-trust%2F$testJourneyId%2Fsa-utr" +
                "&origin=minor-entity-identification-frontend"
              )
            )
          }
        }

      }

      "the feature switch is disabled" should {
        "raise an internal server exception" in {
          lazy val result = {
            await(insertJourneyConfig(
              journeyId = testJourneyId,
              internalId = testInternalId,
              testTrustsJourneyConfig(businessVerificationCheck = true)
            ))
            disable(EnableFullTrustJourney)
            stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
            get(s"/identify-your-trust/$testJourneyId/sa-utr")
          }
          result.status mustBe INTERNAL_SERVER_ERROR
        }
      }

    }
  }

  "POST /sa-utr" when {

    "the user is authorized" when {

      "the feature switch is enabled" when {

        "the utr is correctly formatted" should {
          "redirect to enter your postcode" in {
            await(insertJourneyConfig(
              journeyId = testJourneyId,
              internalId = testInternalId,
              testTrustsJourneyConfig(businessVerificationCheck = true)
            ))
            enable(EnableFullTrustJourney)
            stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
            stubStoreUtr(testJourneyId, Sautr(testUtr))(OK)

            lazy val result = post(s"/identify-your-trust/$testJourneyId/sa-utr")("utr" -> testUtr)

            result must have(
              httpStatus(SEE_OTHER),
              redirectUri(routes.CaptureSaPostcodeController.show(testJourneyId).url)
            )
          }
        }

        "the utr is an sautr" should {
          "redirect to check your answers" in {
            val testSautr = "1234530000"

            await(insertJourneyConfig(
              journeyId = testJourneyId,
              internalId = testInternalId,
              testTrustsJourneyConfig(businessVerificationCheck = true)
            ))
            enable(EnableFullTrustJourney)
            stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
            stubStoreUtr(testJourneyId, Sautr(testSautr))(OK)

            lazy val result = post(s"/identify-your-trust/$testJourneyId/sa-utr")("utr" -> testSautr)

            result must have(
              httpStatus(SEE_OTHER),
              redirectUri(routes.CaptureSaPostcodeController.show(testJourneyId).url)
            )
          }
        }

        "no utr is submitted" should {
          lazy val result = {
            await(insertJourneyConfig(
              journeyId = testJourneyId,
              internalId = testInternalId,
              testTrustsJourneyConfig(businessVerificationCheck = true)
            ))
            enable(EnableFullTrustJourney)
            stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
            post(s"/identify-your-trust/$testJourneyId/sa-utr")("utr" -> "")
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
              testTrustsJourneyConfig(businessVerificationCheck = true)
            ))
            enable(EnableFullTrustJourney)
            stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
            post(s"/identify-your-trust/$testJourneyId/sa-utr")("utr" -> "1@34567890")
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
              testTrustsJourneyConfig(businessVerificationCheck = true)
            ))
            enable(EnableFullTrustJourney)
            stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
            post(s"/identify-your-trust/$testJourneyId/sa-utr")("utr" -> "123456789123456789")
          }

          "return a bad request" in {
            result.status mustBe BAD_REQUEST
          }

          testCaptureUtrViewInvalidUtrLength(result)
        }

      }

      "the feature switch is disabled" when {


        "a valid utr is entered" should {

          "raise an internal server error" in {

            await(journeyConfigRepository.insertJourneyConfig(
              journeyId = testJourneyId,
              authInternalId = testInternalId,
              journeyConfig = testTrustsJourneyConfig(businessVerificationCheck = true)
            ))

            disable(EnableFullTrustJourney)
            stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

            lazy val result = post(s"/identify-your-trust/$testJourneyId/sa-utr")("utr" -> "1234567891")

            result.status mustBe INTERNAL_SERVER_ERROR
          }

        }

      }

    }
  }

}
