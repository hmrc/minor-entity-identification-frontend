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

package uk.gov.hmrc.minorentityidentificationfrontend.assets

import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.trustControllers.{routes => trustControllersRoutes}
import uk.gov.hmrc.minorentityidentificationfrontend.models.BusinessEntity._
import uk.gov.hmrc.minorentityidentificationfrontend.models.{JourneyConfig, Overseas, PageConfig, TrustKnownFacts}

import java.util.UUID

object TestConstants {

  val testJourneyId: String = UUID.randomUUID().toString
  val testInternalId: String = UUID.randomUUID().toString
  val testOverseasTaxIdentifiers: Overseas = Overseas("134124532", "AL")
  val testSautr: String = "1234567890"
  val testSaPostcode: String =  "AA00 0AA"
  val testPostcode: String = "AA1 1AA"
  val testOfficePostcode: String =  "AA22 2AA"
  val testCHRN: String = "AB99999"
  val testContinueUrl: String = "/test"
  val testDeskProServiceId: String = "vrs"
  val testSignOutUrl: String = "/sign-out"
  val testAccessibilityUrl: String = "/accessibility"
  val testRegime: String = "VATC"
  val testOverseasTaxIdentifiersJson: JsObject = Json.obj(
    "taxIdentifier" -> testOverseasTaxIdentifiers.taxIdentifier,
    "country" -> testOverseasTaxIdentifiers.country
  )
  val testSafeId: String = UUID.randomUUID().toString

  def testJourneyConfig(serviceName: Option[String] = None,
                        businessEntity: BusinessEntity,
                        businessVerificationCheck: Boolean,
                        regime: String): JourneyConfig =
    JourneyConfig(
      testContinueUrl,
      PageConfig(serviceName, testDeskProServiceId, testSignOutUrl, testAccessibilityUrl),
      businessEntity,
      businessVerificationCheck,
      regime
    )

  def testTrustsJourneyConfig(businessVerificationCheck: Boolean): JourneyConfig =
    testJourneyConfig(businessEntity = Trusts, businessVerificationCheck = businessVerificationCheck, regime = testRegime)

  def testUnincorporatedAssociationJourneyConfig(businessVerificationCheck: Boolean): JourneyConfig =
    testJourneyConfig(businessEntity = UnincorporatedAssociation, businessVerificationCheck = businessVerificationCheck, regime = testRegime)

  def testOverseasCompanyJourneyConfig(businessVerificationCheck: Boolean): JourneyConfig =
    testJourneyConfig(businessEntity = OverseasCompany, businessVerificationCheck = businessVerificationCheck, regime = testRegime)

  val testUtr: String = "1234567890"
  val testCtutr: String = "1234500000"
  val testUtrType: String = "sautr"
  val testCtutrType: String = "ctutr"

  val testUtrJson: JsObject = Json.obj(
      "type" -> testUtrType,
      "value" -> testUtr
    )

  val testCtutrJson: JsObject = Json.obj(
    "type" -> testCtutrType,
    "value" -> testCtutr
  )

  val testTrustKnownFactsResponse: TrustKnownFacts = TrustKnownFacts(Some(testPostcode), Some(testSaPostcode), isAbroad = false)

  val testKnownFactsJson: JsObject = testKnownFactsJson(correspondencePostcode = "AA1 1AA",declarationPostcode = "AA00 0AA")

  def testKnownFactsJson(correspondencePostcode: String, declarationPostcode: String): JsObject = Json.obj(
    "getTrust" -> Json.obj(
      "declaration" -> Json.obj(
        "name" -> Json.obj(
          "firstName" -> "Joe",
          "lastName" -> "Bloggs"
        ),
        "address" -> Json.obj(
          "postCode" -> declarationPostcode,
          "country" -> "GB",
          "line1" -> "Test Line 1",
          "line2" -> "Test Line 2"
        )
      ),
      "matchData" -> (
        "utr" -> "1234567890"
        ),
      "applicationType" -> "01",
      "submissionDate" -> "2021-03-31",
      "correspondence" -> Json.obj(
        "abroadIndicator" -> false,
        "name" -> "Test UK Trust",
        "address" -> Json.obj(
          "line1" -> "Test Line 1",
          "line2" -> "Test Lane",
          "postCode" -> correspondencePostcode,
          "country" -> "GB"
        ),
        "phoneNumber" -> "0191 2929292"
      ),
      "details" -> Json.obj(
        "trust" -> Json.obj(
          "details" -> Json.obj(
            "administrationCountry" -> "GB",
            "trustUKResident" -> true,
            "trustUKProperty" -> false,
            "trustRecorded" -> false,
            "residentialStatus" -> Json.obj(
              "uk" -> Json.obj(
                "scottishLaw" -> false
              )
            ),
            "startDate" -> "2021-03-31",
            "expressTrust" -> true,
            "typeOfTrust" -> "Will Trust or Intestacy Trust",
            "trustTaxable" -> true
          ),
          "entities" -> Json.obj(
            "beneficiary" -> Json.obj(
              "unidentified" -> Json.arr(
                Json.obj(
                  "lineNo" -> "3",
                  "description" -> "Grandchildren of John",
                  "entityStart" -> "2021-03-31"
                )
              )
            ),
            "deceased" -> Json.obj(
              "name" -> Json.obj(
                "firstName" -> "Adam",
                "lastName" -> "Conder"
              ),
              "bpMatchStatus" -> "98",
              "dateOfDeath" -> "2019-08-27",
              "dateOfBirth" -> "1990-06-21",
              "entityStart" -> "2021-03-31",
              "lineNo" -> "2"
            ),
            "leadTrustees" -> Json.obj(
              "name" -> Json.obj(
                "firstName" -> "John",
                "lastName" -> "Whitfield"
              ),
              "bpMatchStatus" -> "02",
              "phoneNumber" -> "0191 2929292",
              "identification" -> Json.obj(
                "address" -> Json.obj(
                  "line1" -> "33 New Crest",
                  "line2" -> "Lane",
                  "postCode" -> "NE7 8JP",
                  "country" -> "GB"
                ),
                "passport" -> Json.obj(
                  "number" -> "987345987398457",
                  "expirationDate" -> "2025-05-21",
                  "countryOfIssue" -> "GB"
                ),
                "safeId" -> "XF0000100351861"
              ),
              "dateOfBirth" -> "1980-10-10",
              "entityStart" -> "2021-03-31",
              "lineNo" -> "1",
              "nationality" -> "GB",
              "countryOfResidence" -> "GB"
            )
          ),
          "assets" -> Json.obj(
            "monetary" -> Json.arr(
              "assetMonetaryAmount" -> 10000
            )
          )
        )
      )
    ),
    "responseHeader" -> Json.obj(
      "dfmcaReturnUserStatus" -> "Processed",
      "formBundleNo" -> "000001230962"
    )
  )

  val testIdentifiersMatchSuccessfulMatchJson: JsObject = Json.obj("identifiersMatch" -> "SuccessfulMatch")
  val testIdentifiersMatchDetailsMismatchJson: JsObject = Json.obj("identifiersMatch" -> "DetailsMismatch")
  val testIdentifiersMatchUnmatchableWithRetry: JsObject = Json.obj("identifiersMatch" -> "UnMatchableWithRetry")
  val testIdentifiersMatchUnmatchableWithoutRetry: JsObject = Json.obj("identifiersMatch" -> "UnMatchableWithoutRetry")

  val testBusinessVerificationRedirectUrl = "/business-verification-start"
  val testBusinessVerificationJourneyId = "TestBusinessVerificationJourneyId"
  val testBusinessVerificationPassJson: JsObject = Json.obj("verificationStatus" -> "PASS")
  val testBusinessVerificationFailJson: JsObject = Json.obj("verificationStatus" -> "FAIL")
  val testBusinessVerificationNotEnoughInfoToChallengeJson: JsObject = Json.obj("verificationStatus" -> "NOT_ENOUGH_INFORMATION_TO_CHALLENGE")
  val testBusinessVerificationNotEnoughInfoToCallJson: JsObject = Json.obj("verificationStatus" -> "NOT_ENOUGH_INFORMATION_TO_CALL_BV")

  val testSuccessfulRegistrationJson: JsObject = Json.obj(
    "registrationStatus" -> "REGISTERED",
    "registeredBusinessPartnerId" -> testSafeId)

  val testRegistrationNotCalledJson: JsObject = Json.obj("registrationStatus" -> "REGISTRATION_NOT_CALLED")

  def testIdentifiersMatchJson(identifiersMatchValue: String): JsObject = Json.obj("identifiersMatch" -> identifiersMatchValue)

  def testVerificationStatusJson(verificationStatusValue: String): JsObject =
    Json.obj("verificationStatus" -> verificationStatusValue)

  def testCreateBusinessVerificationJourneyJson(sautr: String,
                                                journeyId: String,
                                                accessibilityUrl: String,
                                                regime: String): JsObject =
    Json.obj("journeyType" -> "BUSINESS_VERIFICATION",
      "origin" -> regime,
      "identifiers" -> Json.arr(
        Json.obj(
          "saUtr" -> sautr
        )
      ),
      "continueUrl" -> trustControllersRoutes.BusinessVerificationController.retrieveBusinessVerificationResult(journeyId).url,
      "accessibilityStatementUrl" -> accessibilityUrl
    )
}
