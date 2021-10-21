/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.models.BusinessEntity.OverseasCompany
import uk.gov.hmrc.minorentityidentificationfrontend.stubs.{AuthStub, StorageStub}
import uk.gov.hmrc.minorentityidentificationfrontend.utils.{ComponentSpecHelper, WiremockHelper}
import uk.gov.hmrc.minorentityidentificationfrontend.utils.WiremockHelper.{stubAudit, verifyAudit}
import uk.gov.hmrc.minorentityidentificationfrontend.views.CheckYourAnswersViewTests

class CheckYourAnswersControllerISpec extends ComponentSpecHelper with AuthStub with StorageStub with CheckYourAnswersViewTests with WiremockHelper {

  def extraConfig: Map[String, String] = Map(
    "auditing.enabled" -> "true",
    "auditing.consumer.baseUri.host" -> mockHost,
    "auditing.consumer.baseUri.port" -> mockPort
  )

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(config ++ extraConfig)
    .build

  override def beforeEach(): Unit = {
    await(journeyConfigRepository.drop)
    super.beforeEach()
  }

  "GET /check-your-answers-business" when {
    "the applicant has an sautr" should {
      lazy val result: WSResponse = {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          continueUrl = testContinueUrl,
          optServiceName = None,
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl,
          accessibilityUrl = testAccessibilityUrl,
          OverseasCompany
        ))
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRetrieveUtr(testJourneyId)(OK, testUtrJson)
        stubAudit()
        get(s"/identify-your-overseas-business/$testJourneyId/check-your-answers-business")
      }

      "return OK" in {
        result.status mustBe OK
      }

      "return a view which" should {
        testCheckYourAnswersView(result)
      }

      "redirect to sign in page" when {
        "the user is UNAUTHORISED" in {
          stubAuthFailure()
          stubAudit()

          lazy val result: WSResponse = get(s"/identify-your-overseas-business/$testJourneyId/check-your-answers-business")

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri("/bas-gateway/sign-in" +
              s"?continue_url=%2Fidentify-your-overseas-business%2F$testJourneyId%2Fcheck-your-answers-business" +
              "&origin=minor-entity-identification-frontend"
            )
          }
        }
      }
    }

    "the applicant does not have a sautr" should {
      lazy val result: WSResponse = {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          continueUrl = testContinueUrl,
          optServiceName = None,
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl,
          accessibilityUrl = testAccessibilityUrl,
          OverseasCompany
        ))
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRetrieveUtr(testJourneyId)(NOT_FOUND)
        stubAudit()
        get(s"/identify-your-overseas-business/$testJourneyId/check-your-answers-business")
      }

      "return OK" in {
        result.status mustBe OK
      }

      "return a view which" should {
        testCheckYourAnswersNoUtrView(result)
      }

      "redirect to sign in page" when {
        "the user is UNAUTHORISED" in {
          stubAuthFailure()
          stubAudit()

          lazy val result: WSResponse = get(s"/identify-your-overseas-business/$testJourneyId/check-your-answers-business")

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri("/bas-gateway/sign-in" +
              s"?continue_url=%2Fidentify-your-overseas-business%2F$testJourneyId%2Fcheck-your-answers-business" +
              "&origin=minor-entity-identification-frontend"
            )
          }
        }
      }
    }

  }

  "POST /check-your-answers-business" should {
    "redirect to the provided continueUrl" in {
      await(insertJourneyConfig(
        journeyId = testJourneyId,
        internalId = testInternalId,
        continueUrl = testContinueUrl,
        optServiceName = None,
        deskProServiceId = testDeskProServiceId,
        signOutUrl = testSignOutUrl,
        accessibilityUrl = testAccessibilityUrl,
        OverseasCompany
      ))
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      stubRetrieveUtr(testJourneyId)(OK, testUtrJson)
      stubAudit()

      val result = post(s"/identify-your-overseas-business/$testJourneyId/check-your-answers-business")()

      result must have {
        httpStatus(SEE_OTHER)
        redirectUri(testContinueUrl)
      }
      verifyAudit()
    }
  }

}
