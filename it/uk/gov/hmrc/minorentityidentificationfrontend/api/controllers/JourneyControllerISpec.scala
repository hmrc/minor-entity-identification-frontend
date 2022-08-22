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

import play.api.libs.json.{JsObject, Json, __}
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.{NOT_FOUND, _}
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.overseasControllers.{routes => overseasControllerRoutes}
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.trustControllers.{routes => trustControllerRoutes}
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.uaControllers.{routes => uaControllerRoutes}
import uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.core.config.{EnableFullTrustJourney, EnableFullUAJourney, FeatureSwitching}
import uk.gov.hmrc.minorentityidentificationfrontend.models.JourneyConfig
import uk.gov.hmrc.minorentityidentificationfrontend.repositories.JourneyConfigRepository
import uk.gov.hmrc.minorentityidentificationfrontend.stubs.{AuthStub, JourneyStub, StorageStub}
import uk.gov.hmrc.minorentityidentificationfrontend.utils.AuditEnabledSpecHelper
import uk.gov.hmrc.minorentityidentificationfrontend.utils.WiremockHelper.stubAudit

class JourneyControllerISpec extends AuditEnabledSpecHelper with JourneyStub with AuthStub with StorageStub with FeatureSwitching {

  lazy val repo: JourneyConfigRepository = app.injector.instanceOf[JourneyConfigRepository]

  def extractActualBusinessVerificationStatus(response: WSResponse): String = response.json.as[String]((__ \ "businessVerification" \ "verificationStatus").read)

  val testJourneyConfigJson: JsObject = Json.obj(
    "continueUrl" -> testContinueUrl,
    "deskProServiceId" -> testDeskProServiceId,
    "signOutUrl" -> testSignOutUrl,
    "accessibilityUrl" -> testAccessibilityUrl,
    "businessVerificationCheck" -> true,
    "regime" -> testRegime
  )

  "POST /api/overseas-company-journey" should {
    "return a created journey" when {
      "all the URLs provided are relative" in {
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))
        stubAudit()

        lazy val result = post("/minor-entity-identification/api/overseas-company-journey", testJourneyConfigJson)

        (result.json \ "journeyStartUrl").as[String] must include(overseasControllerRoutes.CaptureUtrController.show(testJourneyId).url)

        result.status mustBe CREATED

        await(journeyConfigRepository.getJourneyConfig(testJourneyId, testInternalId)) mustBe Some(testOverseasCompanyJourneyConfig(businessVerificationCheck = true))
      }
      "a URL provided in non-relative but in allowed hosts list" in {
        val testJourneyConfigJson: JsObject = Json.obj(
          "continueUrl" -> testContinueUrl,
          "deskProServiceId" -> testDeskProServiceId,
          "signOutUrl" -> testSignOutUrl,
          "accessibilityUrl" -> testLocalAccessibilityUrl,
          "businessVerificationCheck" -> true,
          "regime" -> testRegime
        )
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))
        stubAudit()

        lazy val result = post("/minor-entity-identification/api/overseas-company-journey", testJourneyConfigJson)

        (result.json \ "journeyStartUrl").as[String] must include(overseasControllerRoutes.CaptureUtrController.show(testJourneyId).url)

        result.status mustBe CREATED
      }
    }

    "return correct Journey configuration data when an optional welsh service name is supplied" in {
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))
      stubAudit()

      lazy val result = post("/minor-entity-identification/api/overseas-company-journey", testJourneyConfigJson ++ optLabelsAsJson)

      result.status mustBe CREATED

      val expectedJourneyConfig: JourneyConfig = testOverseasCompanyJourneyConfig(businessVerificationCheck = true)
        .copy(pageConfig =  testOverseasCompanyJourneyConfig(businessVerificationCheck = true).pageConfig
          .copy(optLabels = Some(optLabels))
        )

      await(journeyConfigRepository.getJourneyConfig(testJourneyId, testInternalId)) mustBe Some(expectedJourneyConfig)

    }

    "return Bad Request" when {
      "one URL provided for the journey config is not relative" in {
        val testJourneyConfigJson: JsObject = Json.obj(
          "continueUrl" -> testContinueUrl,
          "deskProServiceId" -> testDeskProServiceId,
          "signOutUrl" -> testSignOutUrl,
          "accessibilityUrl" -> testStagingAccessibilityUrl,
          "businessVerificationCheck" -> true,
          "regime" -> testRegime
        )
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))
        stubAudit()

        lazy val result = post("/minor-entity-identification/api/overseas-company-journey", testJourneyConfigJson)

        result.status mustBe BAD_REQUEST
      }
    }
    "redirect to Sign In page" when {
      "the user is UNAUTHORISED" in {
        stubAuthFailure()
        stubAudit()

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

    "return an error" when {
      "we have no internalId after auth" in {
        stubAuth(OK, body = JsObject.empty)
        stubAudit()

        lazy val result = post("/minor-entity-identification/api/overseas-company-journey", testJourneyConfigJson)

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  "GET /api/journey/:journeyId" when {
    "the business entity is a trust" should {
      "return the correct json" when {
        "the user is on the new journey flow" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testTrustsJourneyConfig(businessVerificationCheck = true)
          ))

          val testDetailsJson = Json.obj(
            "sautr" -> "1234567890",
            "identifiersMatch" -> true,
            "businessVerification" -> Json.obj("verificationStatus" -> "PASS"),
            "registration" -> Json.obj("registrationStatus" -> "REGISTERED",
              "registeredBusinessPartnerId" -> testSafeId),
            "saPostcode" -> testSaPostcode
          )

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveEntityDetails(testJourneyId)(OK, testTrustJourneyDataJson)
          stubAudit()

          lazy val result = get(s"/minor-entity-identification/api/journey/$testJourneyId")

          result.status mustBe OK
          result.json mustBe testDetailsJson
        }
      }
      "the user is on the legacy journey" in {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          testTrustsJourneyConfig(businessVerificationCheck = true)
        ))

        val testDetailsJson = Json.obj(
          "identifiersMatch" -> false,
          "businessVerification" -> Json.obj("verificationStatus" -> "UNCHALLENGED"),
          "registration" -> Json.obj("registrationStatus" -> "REGISTRATION_NOT_CALLED")
        )

        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRetrieveEntityDetails(testJourneyId)(OK, testNoIdentifiersJourneyDataJson)
        stubAudit()

        lazy val result = get(s"/minor-entity-identification/api/journey/$testJourneyId")

        result.status mustBe OK
        result.json mustBe testDetailsJson
      }
    }
    "the business entity is an Unincorporated Entity" should {
      "return the correct json" when {
        "the user is on the new journey flow" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
          ))

          val testDetailsJson = Json.obj(
            "ctutr" -> testCtutr,
            "identifiersMatch" -> true,
            "businessVerification" -> Json.obj("verificationStatus" -> "PASS"),
            "registration" -> Json.obj(
              "registrationStatus" -> "REGISTERED",
              "registeredBusinessPartnerId" -> testSafeId),
            "ctPostcode" -> testPostcode
          )

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveEntityDetails(testJourneyId)(OK, testUAJourneyDataJson)
          stubAudit()

          lazy val result = get(s"/minor-entity-identification/api/journey/$testJourneyId")

          result.status mustBe OK
          result.json mustBe testDetailsJson
        }
      }
      "the user is on the legacy journey" in {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          testTrustsJourneyConfig(businessVerificationCheck = true)
        ))

        val testDetailsJson = Json.obj(
          "identifiersMatch" -> false,
          "businessVerification" -> Json.obj("verificationStatus" -> "UNCHALLENGED"),
          "registration" -> Json.obj("registrationStatus" -> "REGISTRATION_NOT_CALLED")
        )

        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRetrieveEntityDetails(testJourneyId)(OK, testNoIdentifiersJourneyDataJson)
        stubAudit()

        lazy val result = get(s"/minor-entity-identification/api/journey/$testJourneyId")

        result.status mustBe OK
        result.json mustBe testDetailsJson
      }
      "return a json with businessVerification PASS" when {
        "BV is equals to PASS in the db" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveEntityDetails(testJourneyId)(OK, testUAJourneyDataJson(verificationStatusValue = "PASS"))
          stubAudit()

          lazy val result = get(s"/minor-entity-identification/api/journey/$testJourneyId")

          result.status mustBe OK

          extractActualBusinessVerificationStatus(response = result) mustBe "PASS"

        }
      }
      "return a json with businessVerification FAIL" when {
        "BV is equals to FAIL in the db" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveEntityDetails(testJourneyId)(OK, testUAJourneyDataJson(verificationStatusValue = "FAIL"))
          stubAudit()

          lazy val result = get(s"/minor-entity-identification/api/journey/$testJourneyId")

          result.status mustBe OK

          extractActualBusinessVerificationStatus(response = result) mustBe "FAIL"

        }
      }
      "return a json with businessVerification UNCHALLENGED" when {
        "BV is equals to NOT_ENOUGH_INFORMATION_TO_CALL_BV in the db" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveEntityDetails(testJourneyId)(OK, testUAJourneyDataJson(verificationStatusValue = "NOT_ENOUGH_INFORMATION_TO_CALL_BV"))
          stubAudit()

          lazy val result = get(s"/minor-entity-identification/api/journey/$testJourneyId")

          result.status mustBe OK

          extractActualBusinessVerificationStatus(response = result) mustBe "UNCHALLENGED"

        }
      }
      "return a json with businessVerification UNCHALLENGED" when {
        "BV is equals to NOT_ENOUGH_INFORMATION_TO_CHALLENGE in the db" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveEntityDetails(testJourneyId)(OK, testUAJourneyDataJson(verificationStatusValue = "NOT_ENOUGH_INFORMATION_TO_CHALLENGE"))
          stubAudit()

          lazy val result = get(s"/minor-entity-identification/api/journey/$testJourneyId")

          result.status mustBe OK

          extractActualBusinessVerificationStatus(response = result) mustBe "UNCHALLENGED"

        }
      }
      "return a json with no businessVerification" when {
        "businessVerificationCheck is false" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = false)
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveEntityDetails(testJourneyId)(OK, testUAJourneyDataJson)
          stubAudit()

          lazy val result = get(s"/minor-entity-identification/api/journey/$testJourneyId")

          result.status mustBe OK

          result.json.toString() mustNot include("businessVerification")
          result.json.toString() mustNot include("verificationStatus")
        }
      }
      "return a json with businessVerification UNCHALLENGED" when {
        "businessVerification is not persisted at all (legacy behavior)" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveEntityDetails(testJourneyId)(OK, JsObject.empty)
          stubAudit()

          lazy val result = get(s"/minor-entity-identification/api/journey/$testJourneyId")

          result.status mustBe OK

          extractActualBusinessVerificationStatus(response = result) mustBe "UNCHALLENGED"
        }
      }
    }
    "the business entity is an Overseas Company" should {
      "return the correct json" when {
        "the user has a ctutr and the overseas tax identifiers are defined separately" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testOverseasCompanyJourneyConfig(businessVerificationCheck = true)
          ))

          val testDetailsJson = Json.obj(
            "ctutr" -> testCtutr,
            "identifiersMatch" -> false,
            "businessVerification" -> Json.obj(
              "verificationStatus" -> "UNCHALLENGED"
            ),
            "registration" -> Json.obj(
              "registrationStatus" -> "REGISTRATION_NOT_CALLED"
            ),
            "overseas" -> testOverseasTaxIdentifiersJson
          )

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveEntityDetails(testJourneyId)(OK, testOverseasJourneyDataJson(testCtutrJson))
          stubAudit()

          lazy val result = get(s"/minor-entity-identification/api/journey/$testJourneyId")

          result.status mustBe OK
          result.json mustBe testDetailsJson
        }
        "the user has a sautr" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testOverseasCompanyJourneyConfig(businessVerificationCheck = true)
          ))

          val testDetailsJson = Json.obj(
            "sautr" -> testSautr,
            "identifiersMatch" -> false,
            "businessVerification" -> Json.obj(
              "verificationStatus" -> "UNCHALLENGED"
            ),
            "registration" -> Json.obj(
              "registrationStatus" -> "REGISTRATION_NOT_CALLED"
            ),
            "overseas" -> testOverseasTaxIdentifiersJson
          )

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveEntityDetails(testJourneyId)(OK, testOverseasJourneyDataJson(testSautrJson))
          stubAudit()

          lazy val result = get(s"/minor-entity-identification/api/journey/$testJourneyId")

          result.status mustBe OK
          result.json mustBe testDetailsJson
        }
      }
    }

    "redirect to Sign In Page" when {
      "the user is UNAUTHORISED" in {
        stubAuthFailure()
        stubAudit()

        lazy val result = get(s"/minor-entity-identification/api/journey/$testJourneyId")

        result must have {
          httpStatus(SEE_OTHER)
          redirectUri("/bas-gateway/sign-in" +
            s"?continue_url=%2Fminor-entity-identification%2Fapi%2Fjourney%2F$testJourneyId" +
            "&origin=minor-entity-identification-frontend")
        }
      }
    }

    "return an error" when {
      "we have no internalId after auth" in {
        stubAuth(OK, body = JsObject.empty)
        stubAudit()

        lazy val result = get(s"/minor-entity-identification/api/journey/$testJourneyId")

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  "POST /api/trusts-journey" should {
    val testJourneyConfigJson: JsObject = Json.obj(
      "continueUrl" -> testContinueUrl,
      "deskProServiceId" -> testDeskProServiceId,
      "signOutUrl" -> testSignOutUrl,
      "accessibilityUrl" -> testAccessibilityUrl,
      "businessVerificationCheck" -> true,
      "regime" -> testRegime
    )

    "return a created journey" when {
      "trust journey FS enabled" in {
        enable(EnableFullTrustJourney)
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))
        stubRetrieveUtr(testJourneyId)(NOT_FOUND)
        stubRetrieveRegistrationStatus(testJourneyId)(NOT_FOUND)
        stubAudit()

        lazy val result = post("/minor-entity-identification/api/trusts-journey", testJourneyConfigJson)

        (result.json \ "journeyStartUrl").as[String] must include(trustControllerRoutes.CaptureSautrController.show(testJourneyId).url)

        result.status mustBe CREATED

        await(journeyConfigRepository.getJourneyConfig(testJourneyId, testInternalId)) mustBe Some(testTrustsJourneyConfig(businessVerificationCheck = true))
      }
      "the trust journey FS disabled" in {
        disable(EnableFullTrustJourney)
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))
        stubAudit()
        stubRetrieveEntityDetails(testJourneyId)(OK, testNoIdentifiersJourneyDataJson)

        lazy val result = post("/minor-entity-identification/api/trusts-journey", testJourneyConfigJson)

        (result.json \ "journeyStartUrl").as[String] must include(testContinueUrl + s"?journeyId=$testJourneyId")

        result.status mustBe CREATED

        await(journeyConfigRepository.getJourneyConfig(testJourneyId, testInternalId)) mustBe Some(testTrustsJourneyConfig(businessVerificationCheck = true))
      }
      "a URL provided is non-relative but in allowed hosts list" in {
        enable(EnableFullTrustJourney)
        val testJourneyConfigJson: JsObject = Json.obj(
          "continueUrl" -> testContinueUrl,
          "deskProServiceId" -> testDeskProServiceId,
          "signOutUrl" -> testSignOutUrl,
          "accessibilityUrl" -> testLocalAccessibilityUrl,
          "businessVerificationCheck" -> true,
          "regime" -> testRegime
        )
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))
        stubAudit()

        lazy val result = post("/minor-entity-identification/api/trusts-journey", testJourneyConfigJson)

        (result.json \ "journeyStartUrl").as[String] must include(trustControllerRoutes.CaptureSautrController.show(testJourneyId).url)

        result.status mustBe CREATED
      }
    }

    "return correct Journey configuration data when an optional welsh service name is supplied" when {
      "trust journey FS is enabled" in {
        enable(EnableFullTrustJourney)
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))
        stubRetrieveUtr(testJourneyId)(NOT_FOUND)
        stubRetrieveRegistrationStatus(testJourneyId)(NOT_FOUND)
        stubAudit()

        lazy val result = post("/minor-entity-identification/api/trusts-journey", testJourneyConfigJson ++ optLabelsAsJson)

        result.status mustBe CREATED

        val expectedJourneyConfig: JourneyConfig = testTrustsJourneyConfig(businessVerificationCheck = true)
          .copy(pageConfig = testTrustsJourneyConfig(businessVerificationCheck = true).pageConfig
            .copy(optLabels = Some(optLabels))
          )

        await(journeyConfigRepository.getJourneyConfig(testJourneyId, testInternalId)) mustBe Some(expectedJourneyConfig)
      }
      "trust journey FS is disabled" in {
        disable(EnableFullTrustJourney)
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))
        stubRetrieveEntityDetails(testJourneyId)(NOT_FOUND)
        stubAudit()

        lazy val result = post("/minor-entity-identification/api/trusts-journey", testJourneyConfigJson ++ optLabelsAsJson)

        result.status mustBe CREATED

        val expectedJourneyConfig: JourneyConfig = testTrustsJourneyConfig(businessVerificationCheck = true)
          .copy(pageConfig = testTrustsJourneyConfig(businessVerificationCheck = true).pageConfig
            .copy(optLabels = Some(optLabels))
          )

        await(journeyConfigRepository.getJourneyConfig(testJourneyId, testInternalId)) mustBe Some(expectedJourneyConfig)
      }
    }

    "return Bad Request" when {
      "one URL provided for the journey config is not relative" in {
        val testJourneyConfigJson: JsObject = Json.obj(
          "continueUrl" -> testContinueUrl,
          "deskProServiceId" -> testDeskProServiceId,
          "signOutUrl" -> testSignOutUrl,
          "accessibilityUrl" -> testStagingAccessibilityUrl,
          "businessVerificationCheck" -> true,
          "regime" -> testRegime
        )
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))
        stubAudit()

        lazy val result = post("/minor-entity-identification/api/trusts-journey", testJourneyConfigJson)

        result.status mustBe BAD_REQUEST
      }
    }

    "redirect to Sign In page" when {
      "the user is UNAUTHORISED" in {
        stubAuthFailure()
        stubAudit()

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
        stubAudit()

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
      "accessibilityUrl" -> testAccessibilityUrl,
      "businessVerificationCheck" -> true,
      "regime" -> testRegime
    )
    "return a created journey" when {
      "the UA journey FS is enabled" when {
        "the urls provided are all relative" in {
          enable(EnableFullUAJourney)
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))
          stubAudit()

          lazy val result = post("/minor-entity-identification/api/unincorporated-association-journey", testJourneyConfigJson)

          (result.json \ "journeyStartUrl").as[String] must include(uaControllerRoutes.CaptureCtutrController.show(testJourneyId).url)

          result.status mustBe CREATED

          await(journeyConfigRepository.getJourneyConfig(testJourneyId, testInternalId)) mustBe Some(testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true))
        }
        "a URL provided is non-relative but in allowed hosts list" in {
          enable(EnableFullUAJourney)
          val testJourneyConfigJson: JsObject = Json.obj(
            "continueUrl" -> testContinueUrl,
            "deskProServiceId" -> testDeskProServiceId,
            "signOutUrl" -> testSignOutUrl,
            "accessibilityUrl" -> testLocalAccessibilityUrl,
            "businessVerificationCheck" -> true,
            "regime" -> testRegime
          )
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))
          stubAudit()

          lazy val result = post("/minor-entity-identification/api/unincorporated-association-journey", testJourneyConfigJson)

          (result.json \ "journeyStartUrl").as[String] must include(uaControllerRoutes.CaptureCtutrController.show(testJourneyId).url)

          result.status mustBe CREATED
        }
      }
      "the UA journey FS is disabled" in {
        disable(EnableFullUAJourney)
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))
        stubRetrieveEntityDetails(testJourneyId)(NOT_FOUND)
        stubAudit()

        lazy val result = post("/minor-entity-identification/api/unincorporated-association-journey", testJourneyConfigJson)

        (result.json \ "journeyStartUrl").as[String] must include(testContinueUrl + s"?journeyId=$testJourneyId")

        result.status mustBe CREATED

        await(journeyConfigRepository.getJourneyConfig(testJourneyId, testInternalId)) mustBe Some(testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true))
      }
    }

    "return correct Journey configuration data when an optional welsh service name is supplied" when {
      "UA journey FS is enabled" in {
        enable(EnableFullUAJourney)
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))
        stubAudit()

        lazy val result = post("/minor-entity-identification/api/unincorporated-association-journey", testJourneyConfigJson ++ optLabelsAsJson)

        result.status mustBe CREATED

        val expectedJourneyConfig: JourneyConfig = testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
          .copy(pageConfig = testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true).pageConfig
            .copy(optLabels = Some(optLabels))
          )

        await(journeyConfigRepository.getJourneyConfig(testJourneyId, testInternalId)) mustBe Some(expectedJourneyConfig)
      }
      "UA journey FS is disabled" in {
        disable(EnableFullUAJourney)
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))
        stubRetrieveEntityDetails(testJourneyId)(NOT_FOUND)
        stubAudit()

        lazy val result = post("/minor-entity-identification/api/unincorporated-association-journey", testJourneyConfigJson ++ optLabelsAsJson)

        result.status mustBe CREATED

        val expectedJourneyConfig: JourneyConfig = testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true)
          .copy(pageConfig = testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true).pageConfig
            .copy(optLabels = Some(optLabels))
          )

        await(journeyConfigRepository.getJourneyConfig(testJourneyId, testInternalId)) mustBe Some(expectedJourneyConfig)
      }
    }

    "return Bad Request" when {
      "one URL provided for the journey config is not relative" in {
        enable(EnableFullTrustJourney)
        val testJourneyConfigJson: JsObject = Json.obj(
          "continueUrl" -> testContinueUrl,
          "deskProServiceId" -> testDeskProServiceId,
          "signOutUrl" -> testSignOutUrl,
          "accessibilityUrl" -> testStagingAccessibilityUrl,
          "businessVerificationCheck" -> true,
          "regime" -> testRegime
        )
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))
        stubAudit()

        lazy val result = post("/minor-entity-identification/api/unincorporated-association-journey", testJourneyConfigJson)

        result.status mustBe BAD_REQUEST
      }
    }

    "redirect to Sign In page" when {
      "the user is UNAUTHORISED" in {
        stubAuthFailure()
        stubAudit()

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
        stubAudit()

        lazy val result = post("/minor-entity-identification/api/unincorporated-association-journey", testJourneyConfigJson)

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

}
