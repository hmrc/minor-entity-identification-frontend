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
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.overseasControllers.{routes => overseasControllersRoutes}
import uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.core.config.{EnableFullTrustJourney, FeatureSwitching}
import uk.gov.hmrc.minorentityidentificationfrontend.stubs.{AuthStub, StorageStub}
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.minorentityidentificationfrontend.views.trustViews.CaptureCHRNumberViewTests

class CaptureCHRNControllerISpec extends ComponentSpecHelper
  with AuthStub
  with StorageStub
  with FeatureSwitching
  with CaptureCHRNumberViewTests {

  def unauthorizedRedirectUri(redirectUri: String): String = "/bas-gateway/sign-in" +
    s"?continue_url=%2Fidentify-your-trust%2F$testJourneyId%2F$redirectUri" +
    "&origin=minor-entity-identification-frontend"

  "GET /CHRN" when {

    "the user is authorized" when {

      "the feature switch is enabled" should {

        lazy val result = {
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testTrustsJourneyConfig(businessVerificationCheck = true)
          ))

          enable(EnableFullTrustJourney)
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          get(s"/identify-your-trust/$testJourneyId/CHRN")
        }

        "return OK" in {
          result.status mustBe OK
        }

        "return a view that" should {
          testCaptureCHRNView(result)
        }
      }

      "the feature switch is disabled" should {

        "raise an internal server exception" in {

          lazy val result = {
            await(journeyConfigRepository.insertJourneyConfig(
              journeyId = testJourneyId,
              authInternalId = testInternalId,
              journeyConfig = testTrustsJourneyConfig(businessVerificationCheck = true)
            ))

            disable(EnableFullTrustJourney)
            stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

            get(s"/identify-your-trust/$testJourneyId/CHRN")
          }

          result.status mustBe INTERNAL_SERVER_ERROR
        }

      }

    }

    "the user is not authorized" should {

      "redirect the user to the login page" in {

        stubAuthFailure()

        lazy val result: WSResponse = get(s"/identify-your-trust/$testJourneyId/CHRN")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(unauthorizedRedirectUri("CHRN"))
        )
      }

    }

}

  "POST /CHRN" when {

    "the user is authorized" when {

      "the feature switch is enabled" when {

        "a valid CHRN value is entered" should {

          "redirect to the CYA page when a valid CHRN is entered" in {

            await(journeyConfigRepository.insertJourneyConfig(
              journeyId = testJourneyId,
              authInternalId = testInternalId,
              journeyConfig = testTrustsJourneyConfig(businessVerificationCheck = true)
            ))

            enable(EnableFullTrustJourney)
            stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
            stubStoreCHRN(testJourneyId, testCHRN)(status = OK)

            lazy val result = post(s"/identify-your-trust/$testJourneyId/CHRN")("chrn" -> testCHRN)

            result.status mustBe SEE_OTHER

            result.header("Location") mustBe Some(overseasControllersRoutes.CheckYourAnswersController.show(testJourneyId).url) // TODO Change location to trusts CYA
          }

        }

        "no CHRN value is entered" should {

          lazy val result = {
            await(journeyConfigRepository.insertJourneyConfig(
              journeyId = testJourneyId,
              authInternalId = testInternalId,
              journeyConfig = testTrustsJourneyConfig(businessVerificationCheck = true)
            ))

            enable(EnableFullTrustJourney)
            stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

            post(s"/identify-your-trust/$testJourneyId/CHRN")("chrn" -> "")
          }

          "return a bad request" in {
            result.status mustBe BAD_REQUEST
          }

          testCaptureCHRNErrorMessageNotEntered(result)

        }

        "a CHRN with invalid characters is entered" should {

          lazy val result = {
            await(journeyConfigRepository.insertJourneyConfig(
              journeyId = testJourneyId,
              authInternalId = testInternalId,
              journeyConfig = testTrustsJourneyConfig(businessVerificationCheck = true)
            ))

            enable(EnableFullTrustJourney)
            stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

            post(s"/identify-your-trust/$testJourneyId/CHRN")("chrn" -> "ab99999")
          }

          "return a bad request" in {
            result.status mustBe BAD_REQUEST
          }

          testCaptureCHRNErrorMessageInvalidCharacters(result)
        }

        "the length of the entered CHRN exceeds the specified maximum length" should {

          lazy val result = {
            await(journeyConfigRepository.insertJourneyConfig(
              journeyId = testJourneyId,
              authInternalId = testInternalId,
              journeyConfig = testTrustsJourneyConfig(businessVerificationCheck = true)
            ))

            enable(EnableFullTrustJourney)
            stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

            post(s"/identify-your-trust/$testJourneyId/CHRN")("chrn" -> "AB9999999")
          }

          "return a bad request" in {
            result.status mustBe BAD_REQUEST
          }

          testCaptureCHRNErrorMessageMaximumLengthExceeded(result)

        }
      }

      "the feature switch is disabled" when {

        "a valid CHRN is entered" should {

          "raise an internal server error" in {

            await(journeyConfigRepository.insertJourneyConfig(
              journeyId = testJourneyId,
              authInternalId = testInternalId,
              journeyConfig = testTrustsJourneyConfig(businessVerificationCheck = true)
            ))

            disable(EnableFullTrustJourney)
            stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

            lazy val result = post(s"/identify-your-trust/$testJourneyId/CHRN")("chrn" -> testCHRN)

            result.status mustBe INTERNAL_SERVER_ERROR
          }

        }

      }

    }

    "the user is not authorized" should {

      "redirect the user to the login page" in {

        stubAuthFailure()

        val result = post(s"/identify-your-trust/$testJourneyId/CHRN")("chrn" -> testCHRN)

        result must have(
          httpStatus(SEE_OTHER),
            redirectUri(unauthorizedRedirectUri("CHRN"))
        )
      }
    }
  }

  "GET /no-CHRN" when {

    "the user is authorized" when {

      "the feature switch is enabled" should {

        "redirect the user to the check your answers page" in {

          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testTrustsJourneyConfig(businessVerificationCheck = true)
          ))

          enable(EnableFullTrustJourney)
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRemoveCHRN(testJourneyId)(status = NO_CONTENT)

          lazy val result = get(s"/identify-your-trust/$testJourneyId/no-CHRN")

          result.status mustBe SEE_OTHER

          result.header("Location") mustBe Some(overseasControllersRoutes.CheckYourAnswersController.show(testJourneyId).url) // TODO Change location to trusts CYA
        }

        "return an internal server error when the back end raises an exception" in {

          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testTrustsJourneyConfig(businessVerificationCheck = true)
          ))

          enable(EnableFullTrustJourney)
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRemoveCHRN(testJourneyId)(status = INTERNAL_SERVER_ERROR, body = "Failed to remove field")

          lazy val result = get(s"/identify-your-trust/$testJourneyId/no-CHRN")

          result.status mustBe INTERNAL_SERVER_ERROR
        }
      }

      "the feature switch is not enabled" should {

        "raise an internal server error" in {

          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testTrustsJourneyConfig(businessVerificationCheck = true)
          ))

          disable(EnableFullTrustJourney)
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRemoveCHRN(testJourneyId)(status = NO_CONTENT)

          lazy val result = get(s"/identify-your-trust/$testJourneyId/no-CHRN")

          result.status mustBe INTERNAL_SERVER_ERROR
        }
      }

    }

    "the user is not authorized" should {

      "redirect the user to the login page" in {

        stubAuthFailure()

        val result = get(s"/identify-your-trust/$testJourneyId/no-CHRN")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(unauthorizedRedirectUri("no-CHRN"))
        )
      }

    }
  }

}
