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

package uk.gov.hmrc.minorentityidentificationfrontend.controllers.uaControllers.errorControllers

import play.api.libs.ws.WSResponse
import play.api.test.Helpers.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NO_CONTENT, OK, SEE_OTHER, await, defaultAwaitTimeout}
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.uaControllers.{routes => uaRoutes}
import uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.core.config.{EnableFullUAJourney, FeatureSwitching}
import uk.gov.hmrc.minorentityidentificationfrontend.models.{JourneyLabels, PageConfig}
import uk.gov.hmrc.minorentityidentificationfrontend.stubs.{AuthStub, StorageStub}
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.minorentityidentificationfrontend.views.CannotConfirmBusinessViewTests

class CannotConfirmBusinessControllerISpec extends ComponentSpecHelper
  with AuthStub
  with StorageStub
  with CannotConfirmBusinessViewTests
  with FeatureSwitching {

  "GET /cannot-confirm-business" when {

    "the EnableFullUAJourney is enabled" should {

      enable(EnableFullUAJourney)

      lazy val result = {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testUnincorporatedAssociationJourneyConfigWithCallingService(true)
        ))

        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        get(s"/identify-your-unincorporated-association/$testJourneyId/cannot-confirm-business")
      }

      lazy val resultWithNoServiceName = {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testUnincorporatedAssociationJourneyConfig(true)
        ))

        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        get(s"/identify-your-unincorporated-association/$testJourneyId/cannot-confirm-business")
      }

      lazy val resultWithServiceNameFromLabels = {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testUnincorporatedAssociationJourneyConfig(true).copy(pageConfig = PageConfig(
            optServiceName = Some(testCallingServiceName),
            deskProServiceId = testDeskProServiceId,
            signOutUrl = testSignOutUrl,
            accessibilityUrl = testAccessibilityUrl,
            optLabels = Some(JourneyLabels(None, Some(testCallingServiceNameFromLabels)))
          ))
        ))

        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        get(s"/identify-your-unincorporated-association/$testJourneyId/cannot-confirm-business")
      }


      "return OK" in {
        result.status mustBe OK
      }

      "return a view" when {
        "there is no serviceName passed in the journeyConfig" should {
          testCannotConfirmBusinessView(resultWithNoServiceName)
          testServiceName(testDefaultServiceName, resultWithNoServiceName)
        }
        "there is a serviceName passed in the journeyConfig" should {
          testCannotConfirmBusinessView(result)
          testServiceName(testCallingServiceName, result)
        }
        "there is a serviceName passed in the journeyConfig labels object" should {
          testCannotConfirmBusinessView(resultWithServiceNameFromLabels)
          testServiceName(testCallingServiceNameFromLabels, resultWithServiceNameFromLabels)
        }
      }

      "redirect to sign in page" when {
        "the user is UNAUTHORISED" in {
          stubAuthFailure()
          lazy val result: WSResponse = get(s"/identify-your-unincorporated-association/$testJourneyId/cannot-confirm-business")

          result must have(
            httpStatus(SEE_OTHER),
            redirectUri("/bas-gateway/sign-in" +
              s"?continue_url=%2Fidentify-your-unincorporated-association%2F$testJourneyId%2Fcannot-confirm-business" +
              "&origin=minor-entity-identification-frontend"
            )
          )
        }
      }

    }

    "the EnableFullUAJourney is disabled" should {
      "throw an internal server exception" in {
        disable(EnableFullUAJourney)
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        val result = get(s"/identify-your-unincorporated-association/$testJourneyId/cannot-confirm-business")

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }

  }

  "POST /cannot-confirm-business" when {

    "the EnableFullUAJourney is enabled" when {

      "the user selects yes" should {

        "redirect to the continue url" in {

          enable(EnableFullUAJourney)

          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testUnincorporatedAssociationJourneyConfig(true)
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

          lazy val result = post(s"/identify-your-unincorporated-association/$testJourneyId/cannot-confirm-business")(
            "yes_no" -> "yes"
          )

          result must have(
            httpStatus(SEE_OTHER),
            redirectUri(testContinueUrl + s"?journeyId=$testJourneyId")
          )

        }
      }

      "the user selects no" should {

        "redirect to the capture Ct utr page" in {

          enable(EnableFullUAJourney)

          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testUnincorporatedAssociationJourneyConfig(true)
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRemoveAllData(testJourneyId)(NO_CONTENT)

          lazy val result = post(s"/identify-your-unincorporated-association/$testJourneyId/cannot-confirm-business")(
            "yes_no" -> "no"
          )

          result must have(
            httpStatus(SEE_OTHER),
            redirectUri(uaRoutes.CaptureCtutrController.show(testJourneyId).url)
          )

        }
      }

      "the user selects no radio box" should {

        enable(EnableFullUAJourney)

        lazy val result = {
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testUnincorporatedAssociationJourneyConfig(true)
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRemoveAllData(testJourneyId)(NO_CONTENT)

          post(s"/identify-your-unincorporated-association/$testJourneyId/cannot-confirm-business")()
        }

        "return a bad request" in {
          result.status mustBe BAD_REQUEST
        }

        testCannotConfirmBusinessErrorView(result)
      }

      "redirect to sign in page" when {

        "the user is UNAUTHORISED" in {

          stubAuthFailure()

          lazy val result: WSResponse = post(s"/identify-your-unincorporated-association/$testJourneyId/cannot-confirm-business")(
            "yes_no" -> "no"
          )

          result must have(
            httpStatus(SEE_OTHER),
            redirectUri("/bas-gateway/sign-in" +
              s"?continue_url=%2Fidentify-your-unincorporated-association%2F$testJourneyId%2Fcannot-confirm-business" +
              "&origin=minor-entity-identification-frontend"
            )
          )

        }
      }

    }

    "the EnableFullUAJourney is disabled" should {

      "throw an internal server exception" in {

        disable(EnableFullUAJourney)

        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        val result = post(s"/identify-your-unincorporated-association/$testJourneyId/cannot-confirm-business")(
          "yes_no" -> "no"
        )

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }

  }

}
