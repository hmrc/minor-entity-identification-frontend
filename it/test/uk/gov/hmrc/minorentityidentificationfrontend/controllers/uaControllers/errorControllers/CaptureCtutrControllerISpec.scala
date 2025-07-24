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

import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NO_CONTENT}
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.{OK, SEE_OTHER, await, defaultAwaitTimeout}
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.core.config.{EnableFullUAJourney, FeatureSwitching}
import uk.gov.hmrc.minorentityidentificationfrontend.models.{Ctutr, JourneyLabels, PageConfig}
import uk.gov.hmrc.minorentityidentificationfrontend.stubs.{AuthStub, StorageStub}
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.minorentityidentificationfrontend.views.uaViews.UaCaptureUtrViewTests

class CaptureCtutrControllerISpec extends ComponentSpecHelper
  with AuthStub with StorageStub with UaCaptureUtrViewTests with FeatureSwitching {

  "GET /ct-utr" when {

    "the user is authorized" when {

      "the feature switch is enabled" should {

        lazy val result = {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testUnincorporatedAssociationJourneyConfigWithCallingService(businessVerificationCheck = true)
          ))
          enable(EnableFullUAJourney)
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          get(s"/identify-your-unincorporated-association/$testJourneyId/ct-utr")
        }

        lazy val resultWithNoServiceName = {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
          ))
          enable(EnableFullUAJourney)
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          get(s"/identify-your-unincorporated-association/$testJourneyId/ct-utr")
        }

        lazy val resultWithServiceNameFromLabels = {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true).copy(pageConfig = PageConfig(
              optServiceName = Some(testCallingServiceName),
              deskProServiceId = testDeskProServiceId,
              signOutUrl = testSignOutUrl,
              accessibilityUrl = testAccessibilityUrl,
              optLabels = Some(JourneyLabels(None, Some(testCallingServiceNameFromLabels)))
            ))
          ))
          enable(EnableFullUAJourney)
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          get(s"/identify-your-unincorporated-association/$testJourneyId/ct-utr")
        }

        "return OK" in {
          result.status mustBe OK
        }

        "return a view" when {
          "there is no serviceName passed in the journeyConfig" should {
            testCaptureUtrView(resultWithNoServiceName)
            testServiceName(testDefaultServiceName, resultWithNoServiceName)
          }
          "there is a serviceName passed in the journeyConfig" should {
            testCaptureUtrView(result)
            testServiceName(testCallingServiceName, result)
          }
          "there is a serviceName passed in the journeyConfig labels object" should {
            testCaptureUtrView(resultWithServiceNameFromLabels)
            testServiceName(testCallingServiceNameFromLabels, resultWithServiceNameFromLabels)
          }
        }

        "redirect to sign in page" when {
          "the user is UNAUTHORISED" in {
            stubAuthFailure()
            lazy val result: WSResponse = get(s"/identify-your-unincorporated-association/$testJourneyId/ct-utr")

            result must have(
              httpStatus(SEE_OTHER),
              redirectUri("/bas-gateway/sign-in" +
                s"?continue_url=%2Fidentify-your-unincorporated-association%2F$testJourneyId%2Fct-utr" +
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
              testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
            ))
            disable(EnableFullUAJourney)
            stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
            get(s"/identify-your-unincorporated-association/$testJourneyId/ct-utr")
          }
          result.status mustBe INTERNAL_SERVER_ERROR
        }
      }

    }

    "the user is not authorized" when {

      "the feature switch is enabled" in {
        lazy val result = {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
          ))
          enable(EnableFullUAJourney)
          stubAuthFailure()
          get(s"/identify-your-unincorporated-association/$testJourneyId/ct-utr")
        }
        result.status mustBe SEE_OTHER
      }

      "the feature switch is disabled" in {
        lazy val result = {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
          ))
          disable(EnableFullUAJourney)
          stubAuthFailure()
          get(s"/identify-your-unincorporated-association/$testJourneyId/ct-utr")
        }
        result.status mustBe SEE_OTHER
      }

    }

  }

  "POST /ct-utr" when {

    "the user is authorized" when {

      "the feature switch is enabled" when {

        "the utr is correctly formatted" should {
          "redirect to enter your postcode and remove CHRN " +
            "(maybe we arrived to CtutrController from from CYA page and CHRN could have been set during the previous journey)" in {
            await(insertJourneyConfig(
              journeyId = testJourneyId,
              internalId = testInternalId,
              testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
            ))
            enable(EnableFullUAJourney)
            stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
            stubStoreUtr(testJourneyId, Ctutr("1234500000"))(OK)
            stubRemoveCHRN(testJourneyId)(status = NO_CONTENT)

            lazy val result = post(s"/identify-your-unincorporated-association/$testJourneyId/ct-utr")("utr" -> "1234500000")

            result must have(
              httpStatus(SEE_OTHER),
              redirectUri(routes.CaptureOfficePostcodeController.show(testJourneyId).url)
            )

            verifyRemoveCHRN(testJourneyId)

          }
        }

        "the utr is an ctutr" should {
          "redirect to capture postcode and remove CHRN " +
            "(maybe we arrived to CtutrController from from CYA page and CHRN could have been set during the previous journey)" in {
            val testCtutr = "1234500000"

            await(insertJourneyConfig(
              journeyId = testJourneyId,
              internalId = testInternalId,
              testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
            ))
            enable(EnableFullUAJourney)
            stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
            stubStoreUtr(testJourneyId, Ctutr(testCtutr))(OK)
            stubRemoveCHRN(testJourneyId)(status = NO_CONTENT)

            lazy val result = post(s"/identify-your-unincorporated-association/$testJourneyId/ct-utr")("utr" -> "1234500000")

            result must have(
              httpStatus(SEE_OTHER),
              redirectUri(routes.CaptureOfficePostcodeController.show(testJourneyId).url)
            )

            verifyRemoveCHRN(testJourneyId)
          }
        }

        "no utr is submitted" should {
          lazy val result = {
            await(insertJourneyConfig(
              journeyId = testJourneyId,
              internalId = testInternalId,
              testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
            ))
            enable(EnableFullUAJourney)
            stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
            post(s"/identify-your-unincorporated-association/$testJourneyId/ct-utr")("utr" -> "")
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
              testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
            ))
            enable(EnableFullUAJourney)
            stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
            post(s"/identify-your-unincorporated-association/$testJourneyId/ct-utr")("utr" -> "1@34567890")
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
              testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
            ))
            enable(EnableFullUAJourney)
            stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
            post(s"/identify-your-unincorporated-association/$testJourneyId/ct-utr")("utr" -> "123456789123456789")
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
              journeyConfig = testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
            ))

            disable(EnableFullUAJourney)
            stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

            lazy val result = post(s"/identify-your-unincorporated-association/$testJourneyId/ct-utr")("utr" -> "1234567891")

            result.status mustBe INTERNAL_SERVER_ERROR
          }

        }

      }

    }

    "the user is not authorized" when {

      "the feature switch is enabled" in {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
        ))

        enable(EnableFullUAJourney)
        stubAuthFailure()

        lazy val result = post(s"/identify-your-unincorporated-association/$testJourneyId/ct-utr")("utr" -> "1234567891")

        result.status mustBe SEE_OTHER

      }

      "the feature switch is disabled" in {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
        ))

        disable(EnableFullUAJourney)
        stubAuthFailure()

        lazy val result = post(s"/identify-your-unincorporated-association/$testJourneyId/ct-utr")("utr" -> "1234567891")

        result.status mustBe SEE_OTHER

      }

    }

  }

  "GET /no-utr" should {
    "redirect to enter your CHRN, remove UTR and remove UaPostcode " +
      "(maybe we arrived to CtutrController from CYA page and UaPostcode could have been set during the previous journey)" in {
      await(insertJourneyConfig(
        journeyId = testJourneyId,
        internalId = testInternalId,
        testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
      ))
      enable(EnableFullUAJourney)
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      stubRemoveUtr(testJourneyId)(status = NO_CONTENT)
      stubRemovePostcode(testJourneyId)(status = NO_CONTENT)

      lazy val result = get(s"/identify-your-unincorporated-association/$testJourneyId/no-utr")

      result must have(
        httpStatus(SEE_OTHER),
        redirectUri(routes.CaptureCHRNController.show(testJourneyId).url)
      )

      verifyRemoveUtr(testJourneyId)
      verifyRemovePostcode(testJourneyId)
    }

    "redirect to Sign In page" when {
      "the user is UNAUTHORISED" in {
        stubAuthFailure()

        lazy val result = get(s"/identify-your-unincorporated-association/$testJourneyId/no-utr")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(
            s"/bas-gateway/sign-in?continue_url=%2Fidentify-your-unincorporated-association%2F$testJourneyId%2Fno-utr&origin=minor-entity-identification-frontend"
          )
        )
      }
    }

  }
}
