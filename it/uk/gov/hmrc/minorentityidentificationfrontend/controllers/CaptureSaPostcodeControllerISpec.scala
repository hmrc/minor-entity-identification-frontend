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

import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.core.config.{EnableFullTrustJourney, FeatureSwitching}
import uk.gov.hmrc.minorentityidentificationfrontend.stubs.{AuthStub, StorageStub}
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.minorentityidentificationfrontend.views.CaptureSaPostcodeViewTests

class CaptureSaPostcodeControllerISpec extends ComponentSpecHelper
  with AuthStub
  with StorageStub
  with CaptureSaPostcodeViewTests with FeatureSwitching {

  "GET /self-assessment-postcode" when {

    "FS is enabled and user is authenticated" should {

      lazy val result = {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          testTrustsJourneyConfig(businessVerificationCheck = true)
        ))
        enable(EnableFullTrustJourney)
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        get(s"/identify-your-trust/$testJourneyId/self-assessment-postcode")
      }

      "return OK" in {
        result.status mustBe OK
      }

      "return a view which" should {
        testCaptureSaPostcodeView(result)
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

    "fs is disabled and user is authenticated" should {

      "raise an internal server error" in {
        lazy val result = {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testTrustsJourneyConfig(businessVerificationCheck = true)
          ))
          disable(EnableFullTrustJourney)
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          get(s"/identify-your-trust/$testJourneyId/self-assessment-postcode")
        }
        result.status mustBe INTERNAL_SERVER_ERROR
      }

    }

  }

  "POST /self-assessment-postcode" when {

    "fs is enabled and user is authenticated" when {

      "the SA Postcode is correctly formatted" should {
        "redirect to CYA page" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testTrustsJourneyConfig(businessVerificationCheck = true)
          ))
          enable(EnableFullTrustJourney)
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubStoreSaPostcode(testJourneyId, testSaPostcode)(status = OK)

          lazy val result = post(s"/identify-your-trust/$testJourneyId/self-assessment-postcode")("saPostcode" -> testSaPostcode)

          result.status mustBe NOT_IMPLEMENTED
          //TODO: Update this to redirect to CYA page
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

    "fs is disabled and user is authenticated" when {

      "the SA Postcode is correctly formatted" should {

        "return internal server exception" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testTrustsJourneyConfig(businessVerificationCheck = true)
          ))
          disable(EnableFullTrustJourney)
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubStoreSaPostcode(testJourneyId, testSaPostcode)(status = OK)

          lazy val result = post(s"/identify-your-trust/$testJourneyId/self-assessment-postcode")("saPostcode" -> testSaPostcode)

          result.status mustBe INTERNAL_SERVER_ERROR
        }

      }

    }

  }

  "GET /no-self-assessment-postcode" when {

    "fs is enabled and user is authenticated" should {

      "redirect to CYA page" when {
        "the user wants to go forward with not having postcode" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testTrustsJourneyConfig(businessVerificationCheck = true)
          ))
          enable(EnableFullTrustJourney)
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRemoveSaPostcode(testJourneyId)(NO_CONTENT)

          val result = get(s"/identify-your-trust/$testJourneyId/no-self-assessment-postcode")

          result.status mustBe NOT_IMPLEMENTED // TODO - this needs to be updated

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
          stubRemoveSaPostcode(testJourneyId)(INTERNAL_SERVER_ERROR, "Failed to remove field")

          val result = get(s"/identify-your-trust/$testJourneyId/no-self-assessment-postcode")

          result.status mustBe INTERNAL_SERVER_ERROR
        }
      }

    }

    "fs is enabled and user is authenticated" should {

      "throw internal server exception" when {

        "the user wants to go forward with not having postcode" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testTrustsJourneyConfig(businessVerificationCheck = true)
          ))
          disable(EnableFullTrustJourney)
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRemoveSaPostcode(testJourneyId)(NO_CONTENT)

          val result = get(s"/identify-your-trust/$testJourneyId/no-self-assessment-postcode")

          result.status mustBe INTERNAL_SERVER_ERROR

        }

      }

    }

  }

}
