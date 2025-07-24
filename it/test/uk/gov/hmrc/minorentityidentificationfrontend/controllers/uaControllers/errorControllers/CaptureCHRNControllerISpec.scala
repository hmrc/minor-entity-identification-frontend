/*
 * Copyright 2025 HM Revenue & Customs
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

import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.core.config.{EnableFullUAJourney, FeatureSwitching}
import uk.gov.hmrc.minorentityidentificationfrontend.models.{JourneyLabels, PageConfig}
import uk.gov.hmrc.minorentityidentificationfrontend.stubs.{AuthStub, StorageStub}
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.minorentityidentificationfrontend.views.uaViews.CaptureCHRNumberViewTests

class CaptureCHRNControllerISpec extends ComponentSpecHelper
  with AuthStub
  with StorageStub
  with FeatureSwitching
  with CaptureCHRNumberViewTests {

  def unauthorizedRedirectUri(redirectUri: String): String = "/bas-gateway/sign-in" +
    s"?continue_url=%2Fidentify-your-unincorporated-association%2F$testJourneyId%2F$redirectUri" +
    "&origin=minor-entity-identification-frontend"

  "GET /chrn" when {

    "the user is authorized" when {

      "the feature switch is enabled" should {

        lazy val result = {
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testUnincorporatedAssociationJourneyConfigWithCallingService(businessVerificationCheck = true)
          ))

          enable(EnableFullUAJourney)
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          get(s"/identify-your-unincorporated-association/$testJourneyId/chrn")
        }

        lazy val resultWithNoServiceName = {
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
          ))

          enable(EnableFullUAJourney)
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          get(s"/identify-your-unincorporated-association/$testJourneyId/chrn")
        }

        lazy val resultWithServiceNameFromLabels = {
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true).copy(pageConfig = PageConfig(
              optServiceName = Some(testCallingServiceName),
              deskProServiceId = testDeskProServiceId,
              signOutUrl = testSignOutUrl,
              accessibilityUrl = testAccessibilityUrl,
              optLabels = Some(JourneyLabels(None, Some(testCallingServiceNameFromLabels)))
            ))
          ))

          enable(EnableFullUAJourney)
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          get(s"/identify-your-unincorporated-association/$testJourneyId/chrn")
        }

        "return OK" in {
          result.status mustBe OK
        }

        "return a view" when {
          "there is no serviceName passed in the journeyConfig" should {
            testCaptureCHRNView(resultWithNoServiceName)
            testServiceName(testDefaultServiceName, resultWithNoServiceName)
          }
          "there is a serviceName passed in the journeyConfig" should {
            testCaptureCHRNView(result)
            testServiceName(testCallingServiceName, result)
          }
          "there is a serviceName passed in the journeyConfig labels object" should {
            testCaptureCHRNView(resultWithServiceNameFromLabels)
            testServiceName(testCallingServiceNameFromLabels, resultWithServiceNameFromLabels)
          }
        }

        "the feature switch is disabled" should {

          "raise an internal server exception" in {

            lazy val result = {
              await(journeyConfigRepository.insertJourneyConfig(
                journeyId = testJourneyId,
                authInternalId = testInternalId,
                journeyConfig = testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
              ))

              disable(EnableFullUAJourney)
              stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

              get(s"/identify-your-unincorporated-association/$testJourneyId/chrn")
            }

            result.status mustBe INTERNAL_SERVER_ERROR
          }

        }
      }
    }

    "the user is not authorized" should {

      "redirect the user to the login page" in {

        stubAuthFailure()

        lazy val result: WSResponse = get(s"/identify-your-unincorporated-association/$testJourneyId/chrn")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(unauthorizedRedirectUri("chrn"))
        )
      }

    }

}

  "POST /chrn" when {

    "the user is authorized" when {

      "the feature switch is enabled" when {

        "a valid CHRN value is entered" should {

          "redirect to the CYA page when a valid CHRN is entered" in {

            await(journeyConfigRepository.insertJourneyConfig(
              journeyId = testJourneyId,
              authInternalId = testInternalId,
              journeyConfig = testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
            ))

            enable(EnableFullUAJourney)
            stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
            stubStoreCHRN(testJourneyId, testCHRN)(status = OK)

            lazy val result = post(s"/identify-your-unincorporated-association/$testJourneyId/chrn")("chrn" -> testCHRN)

            result must have(
              httpStatus(SEE_OTHER),
              redirectUri(routes.CheckYourAnswersController.show(testJourneyId).url)
            )
          }
        }

        "no CHRN value is entered" should {

          lazy val result = {
            await(journeyConfigRepository.insertJourneyConfig(
              journeyId = testJourneyId,
              authInternalId = testInternalId,
              journeyConfig = testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
            ))

            enable(EnableFullUAJourney)
            stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

            post(s"/identify-your-unincorporated-association/$testJourneyId/chrn")("chrn" -> "")
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
              journeyConfig = testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
            ))

            enable(EnableFullUAJourney)
            stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

            post(s"/identify-your-unincorporated-association/$testJourneyId/chrn")("chrn" -> "ab99p99")
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
              journeyConfig = testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
            ))

            enable(EnableFullUAJourney)
            stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

            post(s"/identify-your-unincorporated-association/$testJourneyId/chrn")("chrn" -> "AB9999999")
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
              journeyConfig = testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
            ))

            disable(EnableFullUAJourney)
            stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

            lazy val result = post(s"/identify-your-unincorporated-association/$testJourneyId/chrn")("chrn" -> testCHRN)

            result.status mustBe INTERNAL_SERVER_ERROR
          }

        }

      }

    }

    "the user is not authorized" should {

      "redirect the user to the login page" in {

        stubAuthFailure()

        val result = post(s"/identify-your-unincorporated-association/$testJourneyId/chrn")("chrn" -> testCHRN)

        result must have(
          httpStatus(SEE_OTHER),
            redirectUri(unauthorizedRedirectUri("chrn"))
        )
      }
    }
  }

  "GET /no-chrn" when {

    "the user is authorized" when {

      "the feature switch is enabled" should {

        "redirect the user to the check your answers page" in {

          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
          ))

          enable(EnableFullUAJourney)
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRemoveCHRN(testJourneyId)(status = NO_CONTENT)

          lazy val result = get(s"/identify-your-unincorporated-association/$testJourneyId/no-chrn")

          result must have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.CheckYourAnswersController.show(testJourneyId).url)
          )

          verifyRemoveCHRN(testJourneyId)
        }

        "return an internal server error when the back end raises an exception" in {

          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
          ))

          enable(EnableFullUAJourney)
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRemoveCHRN(testJourneyId)(status = INTERNAL_SERVER_ERROR, body = "Failed to remove field")

          lazy val result = get(s"/identify-your-unincorporated-association/$testJourneyId/no-chrn")

          result.status mustBe INTERNAL_SERVER_ERROR
        }
      }

      "the feature switch is not enabled" should {

        "raise an internal server error" in {

          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
          ))

          disable(EnableFullUAJourney)
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRemoveCHRN(testJourneyId)(status = NO_CONTENT)

          lazy val result = get(s"/identify-your-unincorporated-association/$testJourneyId/no-chrn")

          result.status mustBe INTERNAL_SERVER_ERROR
        }
      }

    }

    "the user is not authorized" should {

      "redirect the user to the login page" in {

        stubAuthFailure()

        val result = get(s"/identify-your-unincorporated-association/$testJourneyId/no-chrn")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(unauthorizedRedirectUri("no-chrn"))
        )
      }

    }
  }

}
