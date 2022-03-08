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
import play.api.test.Helpers._
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.overseasControllers.{routes => overseasControllerRoutes}
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.trustControllers.{routes => trustControllerRoutes}
import uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.core.config.{EnableFullTrustJourney, FeatureSwitching}
import uk.gov.hmrc.minorentityidentificationfrontend.models.KnownFactsMatchingResult
import uk.gov.hmrc.minorentityidentificationfrontend.models.KnownFactsMatchingResult._
import uk.gov.hmrc.minorentityidentificationfrontend.repositories.JourneyConfigRepository
import uk.gov.hmrc.minorentityidentificationfrontend.stubs.{AuthStub, JourneyStub, StorageStub}
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ComponentSpecHelper

class JourneyControllerISpec extends ComponentSpecHelper with JourneyStub with AuthStub with StorageStub with FeatureSwitching {

  lazy val repo: JourneyConfigRepository = app.injector.instanceOf[JourneyConfigRepository]

  "POST /api/overseas-company-journey" should {
    val testJourneyConfigJson: JsObject = Json.obj(
      "continueUrl" -> testContinueUrl,
      "deskProServiceId" -> testDeskProServiceId,
      "signOutUrl" -> testSignOutUrl,
      "accessibilityUrl" -> testAccessibilityUrl,
      "businessVerificationCheck" -> true,
      "regime" -> testRegime
      )

    "return a created journey" in {
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))

      lazy val result = post("/minor-entity-identification/api/overseas-company-journey", testJourneyConfigJson)

      (result.json \ "journeyStartUrl").as[String] must include(overseasControllerRoutes.CaptureUtrController.show(testJourneyId).url)

      result.status mustBe CREATED
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

    "return an error" when {
      "we have no internalId after auth" in {
        stubAuth(OK, body = JsObject.empty)

        lazy val result = post("/minor-entity-identification/api/overseas-company-journey", testJourneyConfigJson)

        result.status mustBe INTERNAL_SERVER_ERROR

      }
    }

  }

  "GET /api/journey/:journeyId" should {
    "return sautr, business registration status, registration status, postcode, chrn and identifiersMatch true" when {
      "they exist in the database and KnownFactsMatchingResult is SuccessfulMatch" in {

        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          testOverseasCompanyJourneyConfig(businessVerificationCheck = true)
          ))

        val testDetailsJson = Json.obj(
          "sautr" -> "1234567890",
          "identifiersMatch" -> true,
          "businessVerification" -> Json.obj("verificationStatus" -> "UNCHALLENGED"),
          "registration" -> Json.obj("registrationStatus" -> "REGISTRATION_NOT_CALLED"),
          "saPostcode" -> testSaPostcode,
          "chrn" -> testCHRN
          )

        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRetrieveUtr(testJourneyId)(OK, testUtrJson)
        stubRetrieveSaPostcode(testJourneyId)(OK, testSaPostcode)
        stubRetrieveCHRN(testJourneyId)(OK, testCHRN)
        stubRetrieveOfficePostcode(testJourneyId)(NOT_FOUND)
        stubRetrieveOverseasTaxIdentifiers(testJourneyId)(NOT_FOUND)
        stubRetrieveIdentifiersMatch(testJourneyId)(OK, testIdentifiersMatchJson(KnownFactsMatchingResult.SuccessfulMatchKey))

        lazy val result = get(s"/minor-entity-identification/api/journey/$testJourneyId")

        result.status mustBe OK
        result.json mustBe testDetailsJson
      }
    }

    "return the business verification status, registration status and identifiersMatch false (no sautr, no postcode, no chrn)" when {
      "the utr, SAPostcode, CHRN and identifiersMatch do not exist in the database" in {

        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          testOverseasCompanyJourneyConfig(businessVerificationCheck = true)
          ))

        val testDetailsJson = Json.obj(
          "identifiersMatch" -> false,
          "businessVerification" -> Json.obj("verificationStatus" -> "UNCHALLENGED"),
          "registration" -> Json.obj("registrationStatus" -> "REGISTRATION_NOT_CALLED")
          )

        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRetrieveUtr(testJourneyId)(status = NOT_FOUND)
        stubRetrieveSaPostcode(testJourneyId)(status = NOT_FOUND)
        stubRetrieveCHRN(testJourneyId)(status = NOT_FOUND)
        stubRetrieveOfficePostcode(testJourneyId)(NOT_FOUND)
        stubRetrieveOverseasTaxIdentifiers(testJourneyId)(status = NOT_FOUND)
        stubRetrieveIdentifiersMatch(testJourneyId)(NOT_FOUND)

        lazy val result = get(s"/minor-entity-identification/api/journey/$testJourneyId")

        result.status mustBe OK
        result.json mustBe testDetailsJson
      }
    }

    "map verificationStatus in the right way for all journey types:" when {

      def extractActualBusinessVerificationStatus(response: WSResponse): String = response.json.as[String]((__ \ "businessVerification" \ "verificationStatus").read)

      "journey config BVCheck is true and BV is BusinessVerificationPass in the DB it returns PASS" in {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          testOverseasCompanyJourneyConfig(businessVerificationCheck = true)
          ))

        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRetrieveUtr(testJourneyId)(OK, testUtrJson)
        stubRetrieveSaPostcode(testJourneyId)(OK, testSaPostcode)
        stubRetrieveCHRN(testJourneyId)(NOT_FOUND)
        stubRetrieveOfficePostcode(testJourneyId)(NOT_FOUND)
        stubRetrieveOverseasTaxIdentifiers(testJourneyId)(NOT_FOUND)
        stubRetrieveIdentifiersMatch(testJourneyId)(OK, testIdentifiersMatchJson(KnownFactsMatchingResult.SuccessfulMatchKey))

        stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, body = testBusinessVerificationPassJson)

        lazy val result: WSResponse = get(s"/minor-entity-identification/api/journey/$testJourneyId")

        result.status mustBe OK

        extractActualBusinessVerificationStatus(response = result) mustBe "PASS"

      }

      "journey config BVCheck is true and BV is BusinessVerificationFail in the DB it returns FAIL" in {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          testOverseasCompanyJourneyConfig(businessVerificationCheck = true)
          ))

        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRetrieveUtr(testJourneyId)(OK, testUtrJson)
        stubRetrieveSaPostcode(testJourneyId)(OK, testSaPostcode)
        stubRetrieveCHRN(testJourneyId)(NOT_FOUND)
        stubRetrieveOfficePostcode(testJourneyId)(NOT_FOUND)
        stubRetrieveOverseasTaxIdentifiers(testJourneyId)(NOT_FOUND)
        stubRetrieveIdentifiersMatch(testJourneyId)(OK, testIdentifiersMatchJson(KnownFactsMatchingResult.SuccessfulMatchKey))

        stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, testBusinessVerificationFailJson)

        lazy val result = get(s"/minor-entity-identification/api/journey/$testJourneyId")

        result.status mustBe OK

        extractActualBusinessVerificationStatus(response = result) mustBe "FAIL"

      }

      "journey config BVCheck is true and BV is BusinessVerificationNotEnoughInformationToCallBV in the DB it returns UNCHALLENGED" in {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          testOverseasCompanyJourneyConfig(businessVerificationCheck = true)
          ))
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRetrieveUtr(testJourneyId)(OK, testUtrJson)
        stubRetrieveSaPostcode(testJourneyId)(OK, testSaPostcode)
        stubRetrieveCHRN(testJourneyId)(NOT_FOUND)
        stubRetrieveOfficePostcode(testJourneyId)(NOT_FOUND)
        stubRetrieveOverseasTaxIdentifiers(testJourneyId)(NOT_FOUND)
        stubRetrieveIdentifiersMatch(testJourneyId)(OK, testIdentifiersMatchJson(KnownFactsMatchingResult.SuccessfulMatchKey))

        stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, testBusinessVerificationNotEnoughInfoToChallengeJson)

        lazy val result = get(s"/minor-entity-identification/api/journey/$testJourneyId")

        result.status mustBe OK

        extractActualBusinessVerificationStatus(response = result) mustBe "UNCHALLENGED"

      }

      "journey config BVCheck is true and BV is BusinessVerificationNotEnoughInformationToChallenge in the DB it returns UNCHALLENGED" in {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          testOverseasCompanyJourneyConfig(businessVerificationCheck = true)
          ))
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRetrieveUtr(testJourneyId)(OK, testUtrJson)
        stubRetrieveSaPostcode(testJourneyId)(OK, testSaPostcode)
        stubRetrieveCHRN(testJourneyId)(NOT_FOUND)
        stubRetrieveOfficePostcode(testJourneyId)(NOT_FOUND)
        stubRetrieveOverseasTaxIdentifiers(testJourneyId)(NOT_FOUND)
        stubRetrieveIdentifiersMatch(testJourneyId)(OK, testIdentifiersMatchJson(KnownFactsMatchingResult.SuccessfulMatchKey))

        stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, testBusinessVerificationNotEnoughInfoToCallJson)

        lazy val result = get(s"/minor-entity-identification/api/journey/$testJourneyId")

        result.status mustBe OK

        extractActualBusinessVerificationStatus(response = result) mustBe "UNCHALLENGED"

      }

      "journey config BVCheck is true and BV is not present in the DB it returns UNCHALLENGED (for all entity: OverseasCompany, UnincorporatedAssociation and Trust)" in {

        List(
          () => testOverseasCompanyJourneyConfig(businessVerificationCheck = true),
          () => testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true),
          () => testTrustsJourneyConfig(businessVerificationCheck = true)
          ).foreach(businessEntityLoader => {

          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            businessEntityLoader()
            ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          // we just want some data ... this test is related only to a specific piece of json
          stubRetrieveUtr(testJourneyId)(NOT_FOUND)
          stubRetrieveSaPostcode(testJourneyId)(NOT_FOUND)
          stubRetrieveCHRN(testJourneyId)(NOT_FOUND)
          stubRetrieveOfficePostcode(testJourneyId)(NOT_FOUND)
          stubRetrieveOverseasTaxIdentifiers(testJourneyId)(NOT_FOUND)
          stubRetrieveIdentifiersMatch(testJourneyId)(NOT_FOUND)

          stubRetrieveBusinessVerificationStatus(testJourneyId)(NOT_FOUND)

          lazy val result = get(s"/minor-entity-identification/api/journey/$testJourneyId")

          result.status mustBe OK

          extractActualBusinessVerificationStatus(response = result) mustBe "UNCHALLENGED"

          await(journeyConfigRepository.drop)

        })
      }

      "journey config BVCheck is false it returns not businessVerification json at all (for all entities: OverseasCompany, UnincorporatedAssociation and Trust)" in {
        List(
          () => testOverseasCompanyJourneyConfig(businessVerificationCheck = false),
          () => testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = false),
          () => testTrustsJourneyConfig(businessVerificationCheck = false)
          ).foreach(businessEntityLoader => {

          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            businessEntityLoader()
            ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          // we just want some data ... this test is related only to a specific piece of json
          stubRetrieveUtr(testJourneyId)(NOT_FOUND)
          stubRetrieveSaPostcode(testJourneyId)(NOT_FOUND)
          stubRetrieveCHRN(testJourneyId)(NOT_FOUND)
          stubRetrieveOfficePostcode(testJourneyId)(NOT_FOUND)
          stubRetrieveOverseasTaxIdentifiers(testJourneyId)(NOT_FOUND)
          stubRetrieveIdentifiersMatch(testJourneyId)(NOT_FOUND)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(NOT_FOUND)

          lazy val result = get(s"/minor-entity-identification/api/journey/$testJourneyId")

          result.status mustBe OK

          result.json.toString() mustNot include("businessVerification")
          result.json.toString() mustNot include("verificationStatus")

          await(journeyConfigRepository.drop)
        })
      }
    }

    "return identifiersMatch false" when {
      "identifiersMatch is different from SuccessfulMatch in the DB" in {

        List(UnMatchableWithoutRetryKey, UnMatchableWithRetryKey, DetailsMismatchKey, DetailsNotFoundKey).foreach(aNonSuccessfulMatch => {

          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            testOverseasCompanyJourneyConfig(businessVerificationCheck = true)
            ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

          // we just want some data ... this test is related only to a specific piece of json
          stubRetrieveUtr(testJourneyId)(status = NOT_FOUND)
          stubRetrieveSaPostcode(testJourneyId)(status = NOT_FOUND)
          stubRetrieveCHRN(testJourneyId)(status = NOT_FOUND)
          stubRetrieveOverseasTaxIdentifiers(testJourneyId)(status = NOT_FOUND)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(NOT_FOUND)
          stubRetrieveOfficePostcode(testJourneyId)(NOT_FOUND)

          stubRetrieveIdentifiersMatch(testJourneyId)(OK, testIdentifiersMatchJson(aNonSuccessfulMatch))

          lazy val result = get(s"/minor-entity-identification/api/journey/$testJourneyId")

          result.status mustBe OK

          result.json.as[Boolean]((__ \ "identifiersMatch").read[Boolean]) mustBe false

          await(journeyConfigRepository.drop)

        })

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

    "return an error" when {
      "we have no internalId after auth" in {
        stubAuth(OK, body = JsObject.empty)

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

    "return a created journey with the trust journey FS enabled" in {
      enable(EnableFullTrustJourney)
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))
      stubRetrieveUtr(testJourneyId)(NOT_FOUND)
      stubRetrieveRegistrationStatus(testJourneyId)(NOT_FOUND)

      lazy val result = post("/minor-entity-identification/api/trusts-journey", testJourneyConfigJson)

      (result.json \ "journeyStartUrl").as[String] must include(trustControllerRoutes.CaptureSautrController.show(testJourneyId).url)

      result.status mustBe CREATED

      await(journeyConfigRepository.getJourneyConfig(testJourneyId, testInternalId)) mustBe Some(testTrustsJourneyConfig(businessVerificationCheck = true))
    }

    "return a created journey with the trust journey FS disabled" in {
      disable(EnableFullTrustJourney)
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))

      lazy val result = post("/minor-entity-identification/api/trusts-journey", testJourneyConfigJson)

      (result.json \ "journeyStartUrl").as[String] must include(testContinueUrl + s"?journeyId=$testJourneyId")

      result.status mustBe CREATED

      await(journeyConfigRepository.getJourneyConfig(testJourneyId, testInternalId)) mustBe Some(testTrustsJourneyConfig(businessVerificationCheck = true))
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
      "accessibilityUrl" -> testAccessibilityUrl,
      "businessVerificationCheck" -> true,
      "regime" -> testRegime
      )
    "return a created journey" in {
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      stubRetrieveUtr(testJourneyId)(NOT_FOUND)
      stubRetrieveOverseasTaxIdentifiers(testJourneyId)(NOT_FOUND)
      stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))

      lazy val result = post("/minor-entity-identification/api/unincorporated-association-journey", testJourneyConfigJson)

      (result.json \ "journeyStartUrl").as[String] must include(testContinueUrl + s"?journeyId=$testJourneyId")

      result.status mustBe CREATED

      await(journeyConfigRepository.getJourneyConfig(testJourneyId, testInternalId)) mustBe Some(testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true))

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
