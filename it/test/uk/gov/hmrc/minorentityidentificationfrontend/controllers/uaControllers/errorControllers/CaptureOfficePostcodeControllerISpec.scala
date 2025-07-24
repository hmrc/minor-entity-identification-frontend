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
import uk.gov.hmrc.minorentityidentificationfrontend.views.uaViews.CaptureOfficePostcodeViewTests

class CaptureOfficePostcodeControllerISpec extends ComponentSpecHelper
  with AuthStub
  with StorageStub
  with FeatureSwitching
  with CaptureOfficePostcodeViewTests {

  val unauthorizedRedirectUri: String = "/bas-gateway/sign-in" +
    s"?continue_url=%2Fidentify-your-unincorporated-association%2F$testJourneyId%2Fregistered-office-postcode" +
    "&origin=minor-entity-identification-frontend"

  "GET /registered-office-postcode" should {
    lazy val result = {
      await(insertJourneyConfig(
        journeyId = testJourneyId,
        internalId = testInternalId,
        testUnincorporatedAssociationJourneyConfigWithCallingService(businessVerificationCheck = true)
      ))
      enable(EnableFullUAJourney)
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      get(s"/identify-your-unincorporated-association/$testJourneyId/registered-office-postcode")
    }

    lazy val resultWithNoServiceName = {
      await(insertJourneyConfig(
        journeyId = testJourneyId,
        internalId = testInternalId,
        testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
      ))
      enable(EnableFullUAJourney)
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      get(s"/identify-your-unincorporated-association/$testJourneyId/registered-office-postcode")
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
      get(s"/identify-your-unincorporated-association/$testJourneyId/registered-office-postcode")
    }

    "return OK" in {
      result.status mustBe OK
    }

    "return a view" when {
      "there is no serviceName passed in the journeyConfig" should {
        testCaptureOfficePostcodeView(resultWithNoServiceName)
        testServiceName(testDefaultServiceName, resultWithNoServiceName)
      }
      "there is a serviceName passed in the journeyConfig" should {
        testCaptureOfficePostcodeView(result)
        testServiceName(testCallingServiceName, result)
      }
      "there is a serviceName passed in the journeyConfig labels object" should {
        testCaptureOfficePostcodeView(resultWithServiceNameFromLabels)
        testServiceName(testCallingServiceNameFromLabels, resultWithServiceNameFromLabels)
      }
    }

    "redirect to sign in page" when {
      "the user is UNAUTHORISED" in {
        stubAuthFailure()
        lazy val result: WSResponse = get(s"/identify-your-unincorporated-association/$testJourneyId/registered-office-postcode")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(unauthorizedRedirectUri)
        )
      }
    }

  }

  "POST /registered-office-postcode" when {
    "the registered office postcode is correctly formatted" should {
      "redirect to CYA page" in {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
        ))
        enable(EnableFullUAJourney)
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubStorePostcode(testJourneyId, testOfficePostcode)(status = OK)

        lazy val result = post(s"/identify-your-unincorporated-association/$testJourneyId/registered-office-postcode")("officePostcode" -> testOfficePostcode)

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CheckYourAnswersController.show(testJourneyId).url)
        )
      }

      "no registered office postcode is submitted" should {
        lazy val result = {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
          ))
          enable(EnableFullUAJourney)
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          post(s"/identify-your-unincorporated-association/$testJourneyId/registered-office-postcode")("officePostcode" -> "")
        }

        "return a bad request" in {
          result.status mustBe BAD_REQUEST
        }

        testCaptureOfficePostcodeErrorMessageNoEntryPostcode(result)
      }

      "an invalid registered office postcode is submitted" should {
        lazy val result = {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
          ))
          enable(EnableFullUAJourney)
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          post(s"/identify-your-unincorporated-association/$testJourneyId/registered-office-postcode")("officePostcode" -> "AA!0!!")
        }

        "return a bad request" in {
          result.status mustBe BAD_REQUEST
        }

        testCaptureOfficePostcodeErrorMessageInvalidPostcode(result)
      }

      "the user is not authorized" should {

        "redirect the user to the login page" in {

          stubAuthFailure()

          val result = post(s"/identify-your-unincorporated-association/$testJourneyId/registered-office-postcode")("officePostcode" -> testOfficePostcode)

          result must have(
            httpStatus(SEE_OTHER),
            redirectUri(unauthorizedRedirectUri)
          )
        }
      }
    }
  }
}
