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

package uk.gov.hmrc.minorentityidentificationfrontend.controllers.trustControllers

import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.trustControllers.{routes => trustControllersRoutes}
import uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.core.config.{EnableFullTrustJourney, FeatureSwitching}
import uk.gov.hmrc.minorentityidentificationfrontend.models.{JourneyLabels, PageConfig}
import uk.gov.hmrc.minorentityidentificationfrontend.stubs.{AuthStub, StorageStub}
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.minorentityidentificationfrontend.views.trustViews.CaptureSaPostcodeViewTests

class CaptureSaPostcodeControllerISpec extends ComponentSpecHelper
  with AuthStub
  with StorageStub
  with FeatureSwitching
  with CaptureSaPostcodeViewTests {

  "GET /self-assessment-postcode" should {
    lazy val result = {
      await(insertJourneyConfig(
        journeyId = testJourneyId,
        internalId = testInternalId,
        testTrustsJourneyConfigWithCallingService
      ))
      enable(EnableFullTrustJourney)
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      get(s"/identify-your-trust/$testJourneyId/self-assessment-postcode")
    }

    lazy val resultWithNoServiceName = {
      await(insertJourneyConfig(
        journeyId = testJourneyId,
        internalId = testInternalId,
        testTrustsJourneyConfig(businessVerificationCheck = true)
      ))
      enable(EnableFullTrustJourney)
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      get(s"/identify-your-trust/$testJourneyId/self-assessment-postcode")
    }

    lazy val resultWithServiceNameFromLabels = {
      await(insertJourneyConfig(
        journeyId = testJourneyId,
        internalId = testInternalId,
        testTrustsJourneyConfig(businessVerificationCheck = true).copy(pageConfig = PageConfig(
          optServiceName = Some(testCallingServiceName),
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl,
          accessibilityUrl = testAccessibilityUrl,
          optLabels = Some(JourneyLabels(None, Some(testCallingServiceNameFromLabels)))
        ))
      ))
      enable(EnableFullTrustJourney)
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      get(s"/identify-your-trust/$testJourneyId/self-assessment-postcode")
    }

    "return OK" in {
      result.status mustBe OK
    }

    "return a view" when {
      "there is no serviceName passed in the journeyConfig" should {
        testCaptureSaPostcodeView(resultWithNoServiceName)
        testServiceName(testDefaultServiceName, resultWithNoServiceName)
      }
      "there is a serviceName passed in the journeyConfig" should {
        testCaptureSaPostcodeView(result)
        testServiceName(testCallingServiceName, result)
      }
      "there is a serviceName passed in the journeyConfig labels object" should {
        testCaptureSaPostcodeView(resultWithServiceNameFromLabels)
        testServiceName(testCallingServiceNameFromLabels, resultWithServiceNameFromLabels)
      }
    }

    "redirect to sign in page" when {
      "the user is UNAUTHORISED" in {
        stubAuthFailure()
        lazy val result: WSResponse = get(s"/identify-your-trust/$testJourneyId/self-assessment-postcode")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri("/bas-gateway/sign-in" +
            s"?continue_url=%2Fidentify-your-trust%2F$testJourneyId%2Fself-assessment-postcode" +
            "&origin=minor-entity-identification-frontend"
          )
        )
      }
    }

  }

  "POST /self-assessment-postcode" when {
    "the SA Postcode is correctly formatted" should {
      "redirect to CYA page" in {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          testTrustsJourneyConfig(businessVerificationCheck = true)
        ))
        enable(EnableFullTrustJourney)
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubStorePostcode(testJourneyId, testSaPostcode)(status = OK)

        lazy val result = post(s"/identify-your-trust/$testJourneyId/self-assessment-postcode")("saPostcode" -> testSaPostcode)

        result.status mustBe SEE_OTHER
        result.header("Location") mustBe Some(trustControllersRoutes.CheckYourAnswersController.show(testJourneyId).url)
      }
    }
    "no SA postcode is submitted" should {
      lazy val result = {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          testTrustsJourneyConfig(businessVerificationCheck = true)
        ))
        enable(EnableFullTrustJourney)
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        post(s"/identify-your-trust/$testJourneyId/self-assessment-postcode")("saPostcode" -> "")
      }

      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }

      testCaptureSaPostcodeErrorMessageNoEntryPostcode(result)
    }

    "an invalid SA postcode is submitted" should {
      lazy val result = {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          testTrustsJourneyConfig(businessVerificationCheck = true)
        ))
        enable(EnableFullTrustJourney)
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        post(s"/identify-your-trust/$testJourneyId/self-assessment-postcode")("saPostcode" -> "AA!0!!")
      }

      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }

      testCaptureSaPostcodeErrorMessageInvalidPostcode(result)
    }
  }

  "GET /no-self-assessment-postcode" should {
    "redirect to CYA page" when {
      "the SA postcode is successfully removed" in {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          testTrustsJourneyConfig(businessVerificationCheck = true)
        ))
        enable(EnableFullTrustJourney)
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRemovePostcode(testJourneyId)(NO_CONTENT)

        val result = get(s"/identify-your-trust/$testJourneyId/no-self-assessment-postcode")

        result.status mustBe SEE_OTHER
        result.header("Location") mustBe Some(trustControllersRoutes.CheckYourAnswersController.show(testJourneyId).url)

      }
    }

    "throw an exception" when {
      "the backend returns a failure" in {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          testTrustsJourneyConfig(businessVerificationCheck = true)
        ))
        enable(EnableFullTrustJourney)
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRemovePostcode(testJourneyId)(INTERNAL_SERVER_ERROR, "Failed to remove field")

        val result = get(s"/identify-your-trust/$testJourneyId/no-self-assessment-postcode")

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

}
