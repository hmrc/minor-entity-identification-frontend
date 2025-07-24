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

package uk.gov.hmrc.minorentityidentificationfrontend.controllers.overseasControllers

import play.api.libs.ws.WSResponse
import play.api.test.Helpers.{BAD_REQUEST, NO_CONTENT, OK, SEE_OTHER, await, defaultAwaitTimeout}
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.overseasControllers
import uk.gov.hmrc.minorentityidentificationfrontend.models.{JourneyLabels, PageConfig}
import uk.gov.hmrc.minorentityidentificationfrontend.stubs.{AuthStub, StorageStub}
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.minorentityidentificationfrontend.views.overseasViews.CaptureOverseasTaxIdentifierTests


class CaptureOverseasTaxIdentifierControllerISpec extends ComponentSpecHelper
  with AuthStub
  with StorageStub
  with CaptureOverseasTaxIdentifierTests {

  "GET /overseas-identifier" when {

    "displayed" should {

      lazy val result = {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          testOverseasCompanyJourneyConfigWithCallingService(businessVerificationCheck = true)
        ))
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        get(s"/identify-your-overseas-business/$testJourneyId/overseas-identifier")
      }

      lazy val resultWithNoServiceName = {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          testOverseasCompanyJourneyConfig(businessVerificationCheck = true)
        ))
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        get(s"/identify-your-overseas-business/$testJourneyId/overseas-identifier")
      }

      lazy val resultWithServiceNameFromLabels = {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          testOverseasCompanyJourneyConfig(businessVerificationCheck = true).copy(pageConfig = PageConfig(
            optServiceName = Some(testCallingServiceName),
            deskProServiceId = testDeskProServiceId,
            signOutUrl = testSignOutUrl,
            accessibilityUrl = testAccessibilityUrl,
            optLabels = Some(JourneyLabels(None, Some(testCallingServiceNameFromLabels)))
          ))
        ))
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        get(s"/identify-your-overseas-business/$testJourneyId/overseas-identifier")
      }

      "return OK" in {
        result.status mustBe OK
      }

      "return a view" when {
        "there is no serviceName passed in the journeyConfig" should {
          testOverseasTaxIdentifierCommonViewTests(resultWithNoServiceName)
          testInitialCaptureOverseasTaxIdentifierView(resultWithNoServiceName)
          testServiceName(testDefaultServiceName, resultWithNoServiceName)
        }
        "there is a serviceName passed in the journeyConfig" should {
          testOverseasTaxIdentifierCommonViewTests(result)
          testInitialCaptureOverseasTaxIdentifierView(result)
          testServiceName(testCallingServiceName, result)
        }
        "there is a serviceName passed in the journeyConfig labels object" should {
          testOverseasTaxIdentifierCommonViewTests(resultWithServiceNameFromLabels)
          testInitialCaptureOverseasTaxIdentifierView(resultWithServiceNameFromLabels)
          testServiceName(testCallingServiceNameFromLabels, resultWithServiceNameFromLabels)
        }

      }
    }

      "an unauthorized user attempts to use the service" should {

        "redirect the user to the sign on page" in {
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

      "the user selects 'Yes' and the tax identifier is correctly formatted" should {

        "redirect to overseas tax identifier country page" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testOverseasCompanyJourneyConfig(businessVerificationCheck = true)
          ))
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubStoreOverseasTaxIdentifier(testJourneyId, testOverseasTaxIdentifier)(OK)

          lazy val result = post(s"/identify-your-overseas-business/$testJourneyId/overseas-identifier"
          )("tax-identifier-radio" -> "Yes",
            "tax-identifier" -> testOverseasTaxIdentifier)

          result must have(
            httpStatus(SEE_OTHER),
            redirectUri(overseasControllers.routes.CaptureOverseasTaxIdentifiersCountryController.show(testJourneyId).url)
          )

          verifyStoreOverseasTaxIdentifier(testJourneyId, testOverseasTaxIdentifier)
        }

      }

      "the user selects 'No'" should {

        "redirect to Check Your Answers" in {

          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testOverseasCompanyJourneyConfig(businessVerificationCheck = true)
          ))
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRemoveOverseasTaxIdentifier(testJourneyId)(NO_CONTENT)
          stubRemoveOverseasTaxIdentifiersCountry(testJourneyId)(NO_CONTENT)

          lazy val result = post(s"/identify-your-overseas-business/$testJourneyId/overseas-identifier"
          )("tax-identifier-radio" -> "No",
            "tax-identifier" -> "")

          result must have(
            httpStatus(SEE_OTHER),
            redirectUri(overseasControllers.routes.CheckYourAnswersController.show(testJourneyId).url)
          )

          verifyRemoveOverseasTaxIdentifier(testJourneyId)
          verifyRemoveOverseasTaxIdentifiersCountry(testJourneyId)
        }

      }

      "the user submits the form without making a selection" should {

        lazy val result = {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testOverseasCompanyJourneyConfig(businessVerificationCheck = true)
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

          post(s"/identify-your-overseas-business/$testJourneyId/overseas-identifier")("tax-identifier-radio" -> "", "tax-identifier" -> "")
        }

        "return a bad request" in {
          result.status mustBe BAD_REQUEST
        }

        testCaptureOverseasTaxIdentifierSelectionErrorMessages(result)
      }

      "the user selects 'Yes' but does not define the tax identifier" should {

        lazy val result = {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testOverseasCompanyJourneyConfig(businessVerificationCheck = true)
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          post(s"/identify-your-overseas-business/$testJourneyId/overseas-identifier"
          )("tax-identifier-radio" -> "Yes",
            "tax-identifier" -> "")
        }

        "return a bad request" in {
          result.status mustBe BAD_REQUEST
        }

        testCaptureCaptureOverseasTaxIdentifierUndefinedTaxIdentifierErrorMessages(result)
      }

      "the user selects 'Yes' but enters an invalid tax identifier" should {

        lazy val result = {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testOverseasCompanyJourneyConfig(businessVerificationCheck = true)
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          post(s"/identify-your-overseas-business/$testJourneyId/overseas-identifier"
          )("tax-identifier-radio" -> "Yes",
            "tax-identifier" -> "134124532$$$")
        }

        "return a bad request" in {
          result.status mustBe BAD_REQUEST
        }

        testCaptureCaptureOverseasTaxIdentifiersErrorMessagesInvalidIdentifier(result)
      }

      "the user selects 'Yes' but enters a tax identifier that is too long" should {

        lazy val result = {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testOverseasCompanyJourneyConfig(businessVerificationCheck = true)
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          post(s"/identify-your-overseas-business/$testJourneyId/overseas-identifier"
          )("tax-identifier-radio" -> "Yes",
            "tax-identifier" -> "0123456789012345678901234567890123456789012345678901234567890")
        }

        "return a bad request" in {
          result.status mustBe BAD_REQUEST
        }

        testCaptureCaptureOverseasTaxIdentifiersErrorMessagesTooLongIdentifier(result)
      }

      "an unauthorized user attempts to submit the form" should {

        "redirect to the user sign-on page" in {

          stubAuthFailure()

          lazy val result: WSResponse = post(s"/identify-your-overseas-business/$testJourneyId/overseas-identifier"
          )("tax-identifier-radio" -> "Yes",
            "tax-identifier" -> testOverseasTaxIdentifier)

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

  }
