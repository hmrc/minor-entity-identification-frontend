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

package uk.gov.hmrc.minorentityidentificationfrontend.api.controllers

import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers._
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.{routes => controllerRoutes}
import uk.gov.hmrc.minorentityidentificationfrontend.models._
import uk.gov.hmrc.minorentityidentificationfrontend.repositories.JourneyConfigRepository
import uk.gov.hmrc.minorentityidentificationfrontend.stubs.{AuthStub, JourneyStub, StorageStub}
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ComponentSpecHelper

class JourneyControllerISpec extends ComponentSpecHelper with JourneyStub with AuthStub with StorageStub {

  lazy val repo: JourneyConfigRepository = app.injector.instanceOf[JourneyConfigRepository]

  "POST /api/overseas-company-journey" should {
    val testJourneyConfigJson: JsObject = Json.obj(
      "continueUrl" -> testContinueUrl,
      "deskProServiceId" -> testDeskProServiceId,
      "signOutUrl" -> testSignOutUrl,
      "accessibilityUrl" -> testAccessibilityUrl
    )
    "return a created journey" in {
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))

      lazy val result = post("/minor-entity-identification/api/overseas-company-journey", testJourneyConfigJson)

      (result.json \ "journeyStartUrl").as[String] must include(controllerRoutes.CaptureUtrController.show(testJourneyId).url)

      result.status mustBe CREATED

//      await(repo.getJourneyConfig(testJourneyId, testInternalId)).map(_.-("creationTimestamp")) mustBe
//        Some(Json.obj("_id" -> testJourneyId, "authInternalId" -> testInternalId) ++ Json.toJsObject(testJourneyConfig))
    }

    "redirect to Sign In page" when {
      "the user is UNAUTHORISED" in {
        stubAuthFailure()

        lazy val result = post("/minor-entity-identification/api/overseas-company-journey", testJourneyConfigJson)

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri("/bas-gateway/sign-in" +
            "?continue_url=%2Fminor-entity-identification%2Fapi%2Foverseas-company-journey" +
            "&origin=minor-entity-identification-frontend"
          )
        )
      }
    }
  }

  "GET /api/journey/:journeyId" should {
    "return utr, business registration status and registration status" when {
      "the utr exists in the database" in {
        val testDetailsJson = Json.obj(
          "sautr" -> "1234567890",
          "identifiersMatch" -> false,
          "businessVerification" -> Json.toJson(BusinessVerificationUnchallenged)(BusinessVerificationStatus.format.writes),
          "registration" -> Json.toJson(RegistrationNotCalled)(RegistrationStatus.format.writes)
        )

        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRetrieveUtr(testJourneyId)(OK, testUtrJson)

        lazy val result = get(s"/minor-entity-identification/api/journey/$testJourneyId")

        result.status mustBe OK
        result.json mustBe testDetailsJson
      }

      "return the business verification status and registration status" when {
        "the utr does not exist in the database" in {
          val testDetailsJson = Json.obj(
            "identifiersMatch" -> false,
            "businessVerification" -> Json.toJson(BusinessVerificationUnchallenged)(BusinessVerificationStatus.format.writes),
            "registration" -> Json.toJson(RegistrationNotCalled)(RegistrationStatus.format.writes)
          )
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveUtr(testJourneyId)(status = NOT_FOUND)

          lazy val result = get(s"/minor-entity-identification/api/journey/$testJourneyId")

          result.status mustBe OK
          result.json mustBe testDetailsJson
        }
      }

      "redirect to Sign In Page" when {
        "the user is UNAUTHORISED" in {
          stubAuthFailure()

          lazy val result = get(s"/minor-entity-identification/api/journey/$testJourneyId")

          result must have {
            httpStatus(SEE_OTHER)
            redirectUri("/bas-gateway/sign-in" +
              s"?continue_url=%2Fminor-entity-identification%2Fapi%2Fjourney%2F$testJourneyId" +
              "&origin=minor-entity-identification-frontend")
          }
        }
      }
    }
  }

  "POST /api/trusts-journey" should {
    val testJourneyConfigJson: JsObject = Json.obj(
      "continueUrl" -> testContinueUrl,
      "deskProServiceId" -> testDeskProServiceId,
      "signOutUrl" -> testSignOutUrl,
      "accessibilityUrl" -> testAccessibilityUrl
    )
    "return a created journey" in {
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))

      lazy val result = post("/minor-entity-identification/api/trusts-journey", testJourneyConfigJson)

      (result.json \ "journeyStartUrl").as[String] must include(testContinueUrl + s"?journeyId=$testJourneyId")

      result.status mustBe CREATED

      await(journeyConfigRepository.getJourneyConfig(testJourneyId, testInternalId)) mustBe Some(testTrustsJourneyConfig)

    }

    "redirect to Sign In page" when {
      "the user is UNAUTHORISED" in {
        stubAuthFailure()

        lazy val result = post("/minor-entity-identification/api/trusts-journey", testJourneyConfigJson)

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri("/bas-gateway/sign-in" +
            "?continue_url=%2Fminor-entity-identification%2Fapi%2Ftrusts-journey" +
            "&origin=minor-entity-identification-frontend"
          )
        )
      }
    }

    "throw an Internal Server Exception" when {
      "the user does not have an internal ID" in {
        stubAuth(OK, successfulAuthResponse(None))
        stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))

        lazy val result = post("/minor-entity-identification/api/trusts-journey", testJourneyConfigJson)

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  "POST /api/unincorporated-association-journey" should {
    val testJourneyConfigJson: JsObject = Json.obj(
      "continueUrl" -> testContinueUrl,
      "deskProServiceId" -> testDeskProServiceId,
      "signOutUrl" -> testSignOutUrl,
      "accessibilityUrl" -> testAccessibilityUrl
    )
    "return a created journey" in {
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))

      lazy val result = post("/minor-entity-identification/api/unincorporated-association-journey", testJourneyConfigJson)

      (result.json \ "journeyStartUrl").as[String] must include(testContinueUrl + s"?journeyId=$testJourneyId")

      result.status mustBe CREATED

      await(journeyConfigRepository.getJourneyConfig(testJourneyId, testInternalId)) mustBe Some(testUnincorporatedAssociationJourneyConfig)

    }

    "redirect to Sign In page" when {
      "the user is UNAUTHORISED" in {
        stubAuthFailure()

        lazy val result = post("/minor-entity-identification/api/unincorporated-association-journey", testJourneyConfigJson)

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri("/bas-gateway/sign-in" +
            "?continue_url=%2Fminor-entity-identification%2Fapi%2Funincorporated-association-journey" +
            "&origin=minor-entity-identification-frontend"
          )
        )
      }
    }

    "throw an Internal Server Exception" when {
      "the user does not have an internal ID" in {
        stubAuth(OK, successfulAuthResponse(None))
        stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))

        lazy val result = post("/minor-entity-identification/api/unincorporated-association-journey", testJourneyConfigJson)

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
