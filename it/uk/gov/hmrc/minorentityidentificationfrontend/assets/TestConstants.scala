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

import play.api.libs.json.{JsArray, JsObject, Json}
import play.api.mvc.Call
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.trustControllers.{routes => trustControllersRoutes}
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.uaControllers
import uk.gov.hmrc.minorentityidentificationfrontend.models.BusinessEntity._
import uk.gov.hmrc.minorentityidentificationfrontend.models.BusinessVerificationStatus._
import uk.gov.hmrc.minorentityidentificationfrontend.models.KnownFactsMatchingResult._
import uk.gov.hmrc.minorentityidentificationfrontend.models.RegistrationStatus._
import uk.gov.hmrc.minorentityidentificationfrontend.models._

import java.util.UUID

object TestConstants {

  val testJourneyId: String = UUID.randomUUID().toString
  val testInternalId: String = UUID.randomUUID().toString

  val testOverseasTaxIdentifier: String = "134124532"
  val testOverseasTaxIdentifiersCountry: String = "AL"
  val testOverseasTaxIdentifiersCountryFullName: String = "Albania"
  val testSautr: String = "1234567890"
  val testCtutr: String = "1000000001"
  val testSaPostcode: String = "AA00 0AA"
  val testPostcode: String = "AA1 1AA"
  val testOfficePostcode: String = "AA22 2AA"
  val testCHRN: String = "Ab99999"
  val testContinueUrl: String = "/test"
  val testContinueUrlToPassToBVCall: String = s"/thisWhenWeCallBV/${UUID.randomUUID().toString}"
  val testDeskProServiceId: String = "vrs"
  val testSignOutUrl: String = "/sign-out"
  val testAccessibilityUrl: String = "/accessibility"
  val testLocalAccessibilityUrl: String = "http://localhost:12346/accessibility-statement/vat-registration"
  val testStagingAccessibilityUrl: String = "https://www.staging.tax.service.gov.uk/accessibility-statement/vat-registration"
  val testRegime: String = "VATC"
  val testServiceName: String = "TestService"
  val testOverseasTaxIdentifiersJson: JsObject = Json.obj(
    "taxIdentifier" -> testOverseasTaxIdentifier,
    "country" -> testOverseasTaxIdentifiersCountry
  )
  val testSafeId: String = UUID.randomUUID().toString
  val testTrustsJourneyConfig: JourneyConfig = testTrustsJourneyConfig(businessVerificationCheck = true)
  val testTrustsJourneyConfigWithCallingService: JourneyConfig = testTrustJourneyConfigWithCallingService(businessVerificationCheck = true)
  val testUAJourneyConfig: JourneyConfig = testTrustsJourneyConfig.copy(businessEntity = UnincorporatedAssociation)
  val testUAJourneyConfigWithCallingService: JourneyConfig = testTrustsJourneyConfigWithCallingService.copy(businessEntity = UnincorporatedAssociation)
  val testDefaultServiceName: String = "Entity Validation Service"
  val testWelshServiceName: String = "Welsh Service Name"
  val testDefaultWelshServiceName: String = "Gwasanaeth Dilysu Endid"
  val testTechnicalHelpUrl: String = "http://localhost:9250/contact/report-technical-problem?newTab=true&service=vrs"

  def testJourneyConfig(serviceName: Option[String] = None,
                        businessEntity: BusinessEntity,
                        businessVerificationCheck: Boolean,
                        regime: String): JourneyConfig =
    JourneyConfig(
      testContinueUrl,
      PageConfig(serviceName, testDeskProServiceId, testSignOutUrl, testAccessibilityUrl, None),
      businessEntity,
      businessVerificationCheck,
      regime
    )

  val testDefaultWelshJourneyConfig: JourneyConfig = JourneyConfig(
    continueUrl = testContinueUrl,
    pageConfig = PageConfig(
      optServiceName = None,
      deskProServiceId = testDeskProServiceId,
      signOutUrl = testSignOutUrl,
      accessibilityUrl = testAccessibilityUrl,
      optLabels = Some(JourneyLabels(welshServiceName = "This is a welsh service name from Journey labels"))
    ),
    businessEntity = UnincorporatedAssociation,
    businessVerificationCheck = true,
    regime = testRegime
  )

  def testTrustsJourneyConfig(businessVerificationCheck: Boolean): JourneyConfig =
    testJourneyConfig(businessEntity = Trusts, businessVerificationCheck = businessVerificationCheck, regime = testRegime)

  def testTrustJourneyConfigWithCallingService(businessVerificationCheck: Boolean): JourneyConfig =
    testJourneyConfig(serviceName = Some(testServiceName), businessEntity = Trusts, businessVerificationCheck = businessVerificationCheck, regime = testRegime)

  def testUnincorporatedAssociationJourneyConfig(businessVerificationCheck: Boolean): JourneyConfig =
    testJourneyConfig(businessEntity = UnincorporatedAssociation, businessVerificationCheck = businessVerificationCheck, regime = testRegime)

  def testUnincorporatedAssociationJourneyConfigWithCallingService(businessVerificationCheck: Boolean): JourneyConfig =
    testJourneyConfig(
      serviceName = Some(testServiceName),
      businessEntity = UnincorporatedAssociation,
      businessVerificationCheck = businessVerificationCheck,
      regime = testRegime)

  def testOverseasCompanyJourneyConfig(businessVerificationCheck: Boolean): JourneyConfig =
    testJourneyConfig(businessEntity = OverseasCompany, businessVerificationCheck = businessVerificationCheck, regime = testRegime)

  def testOverseasCompanyJourneyConfigWithCallingService(businessVerificationCheck: Boolean): JourneyConfig =
    testJourneyConfig(
      serviceName = Some(testServiceName),
      businessEntity = OverseasCompany,
      businessVerificationCheck = businessVerificationCheck,
      regime = testRegime
    )

  val testSautrJson: JsObject = Json.obj(
    "type" -> "sautr",
    "value" -> testSautr
  )

  val testCtutrJson: JsObject = Json.obj(
    "type" -> "ctutr",
    "value" -> testCtutr
  )

  val testTrustJourneyDataJson: JsObject = Json.obj(
    "utr" -> testSautrJson,
    "postcode" -> testSaPostcode,
    "identifiersMatch" -> SuccessfulMatchKey,
    "businessVerification" -> Json.obj(BusinessVerificationStatusKey -> BusinessVerificationPassKey),
    "registration" -> Json.obj(
      registrationStatusKey -> RegisteredKey,
      registeredBusinessPartnerIdKey -> testSafeId)
  )

  val testRegistrationFailure: Array[Failure] = Array(Failure(code = "PARTY_TYPE_MISMATCH", reason = "The remote endpoint has indicated there is Party Type mismatch"))

  val testTrustJourneyDataWithRegistrationFailedJson: JsObject = Json.obj(
    "utr" -> testSautrJson,
    "postcode" -> testSaPostcode,
    "identifiersMatch" -> SuccessfulMatchKey,
    "businessVerification" -> Json.obj(BusinessVerificationStatusKey -> BusinessVerificationPassKey),
    "registration" -> Json.obj(
      "registrationStatus" -> "REGISTRATION_FAILED",
      "failures" -> Json.toJson(testRegistrationFailure)
    ))

  val testTrustIdFalseJourneyDataJson: JsObject = Json.obj(
    "utr" -> testSautrJson,
    "postcode" -> testSaPostcode,
    "identifiersMatch" -> DetailsMismatchKey,
    "businessVerification" -> Json.obj(BusinessVerificationStatusKey -> BusinessVerificationNotEnoughInfoToCallKey),
    "registration" -> Json.obj(registrationStatusKey -> RegistrationNotCalledKey)
  )

  val testTrustBvFailJourneyDataJson: JsObject = Json.obj(
    "utr" -> testSautrJson,
    "postcode" -> testSaPostcode,
    "identifiersMatch" -> SuccessfulMatchKey,
    "businessVerification" -> Json.obj(BusinessVerificationStatusKey -> BusinessVerificationFailKey),
    "registration" -> Json.obj(registrationStatusKey -> RegistrationNotCalledKey)
  )

  val testThisIsADummyJson: JsObject = Json.obj()

  val testNoIdentifiersJourneyDataJson: JsObject = Json.obj(
    "identifiersMatch" -> UnMatchableKey,
    "businessVerification" -> Json.obj(BusinessVerificationStatusKey -> BusinessVerificationNotEnoughInfoToCallKey),
    "registration" -> Json.obj(registrationStatusKey -> RegistrationNotCalledKey)
  )

  val testCHRNJourneyDataJson: JsObject = Json.obj(
    "CHRN" -> testCHRN,
    "identifiersMatch" -> UnMatchableKey,
    "businessVerification" -> Json.obj(BusinessVerificationStatusKey -> BusinessVerificationNotEnoughInfoToCallKey),
    "registration" -> Json.obj(registrationStatusKey -> RegistrationNotCalledKey)
  )

  val testUAJourneyDataJson: JsObject = testUAJourneyDataJson(verificationStatusValue = "PASS")

  def testUAJourneyDataJson(verificationStatusValue: String): JsObject = Json.obj(
    "utr" -> testCtutrJson,
    "postcode" -> testPostcode,
    "identifiersMatch" -> SuccessfulMatchKey,
    "businessVerification" -> Json.obj("verificationStatus" -> verificationStatusValue),
    "registration" -> Json.obj(
      registrationStatusKey -> RegisteredKey,
      registeredBusinessPartnerIdKey -> testSafeId)
  )

  def testUAJourneyDataWithRegistrationFailedJson: JsObject = Json.obj(
    "utr" -> testCtutrJson,
    "postcode" -> testPostcode,
    "identifiersMatch" -> SuccessfulMatchKey,
    "businessVerification" -> Json.obj("verificationStatus" -> "PASS"),
    "registration" -> Json.obj(
      "registrationStatus" -> "REGISTRATION_FAILED",
      "failures" -> Json.toJson(testRegistrationFailure)
    ))

  val testUAJourneyDataJsonNotFound: JsObject = Json.obj(
    "utr" -> testCtutrJson,
    "postcode" -> testPostcode,
    "identifiersMatch" -> DetailsNotFoundKey,
    "businessVerification" -> Json.obj(BusinessVerificationStatusKey -> BusinessVerificationNotEnoughInfoToCallKey),
    "registration" -> Json.obj(registrationStatusKey -> RegistrationNotCalledKey)
  )

  def testOverseasJourneyDataJson(utrBlock: JsObject): JsObject = Json.obj(
    "utr" -> utrBlock,
    "overseasTaxIdentifier" -> testOverseasTaxIdentifier,
    "country" -> testOverseasTaxIdentifiersCountry
  )

  val testTrustKnownFactsResponse: TrustKnownFacts = TrustKnownFacts(Some(testPostcode), Some(testSaPostcode), isAbroad = false)

  val testKnownFactsJson: JsObject = testKnownFactsJson(correspondencePostcode = "AA1 1AA", declarationPostcode = "AA00 0AA")

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

  val optLabelsAsJson: JsObject = Json.obj({
    "labels" -> Json.obj (
      "cy" -> Json.obj (
      "optServiceName" -> s"$testWelshServiceName"
      )
    )
  })

  val optLabels: JourneyLabels = JourneyLabels(testWelshServiceName)

  val testIdentifiersMatchSuccessfulMatchJson: JsObject = Json.obj("identifiersMatch" -> "SuccessfulMatch")
  val testIdentifiersMatchDetailsMismatchJson: JsObject = Json.obj("identifiersMatch" -> "DetailsMismatch")
  val testIdentifiersMatchUnmatchable: JsObject = Json.obj("identifiersMatch" -> "UnMatchable")

  val testBusinessVerificationRedirectUrl = "/business-verification-start"
  val testBusinessVerificationJourneyId = "TestBusinessVerificationJourneyId"
  val testBusinessVerificationPassJson: JsObject = Json.obj("verificationStatus" -> "PASS")
  val testBusinessVerificationFailJson: JsObject = Json.obj("verificationStatus" -> "FAIL")
  val testBusinessVerificationNotEnoughInfoToChallengeJson: JsObject = Json.obj("verificationStatus" -> "NOT_ENOUGH_INFORMATION_TO_CHALLENGE")
  val testBusinessVerificationNotEnoughInfoToCallJson: JsObject = Json.obj("verificationStatus" -> "NOT_ENOUGH_INFORMATION_TO_CALL_BV")

  def testVerificationStatusJson(verificationStatusValue: String): JsObject =
    Json.obj("verificationStatus" -> verificationStatusValue)

  def testCreateBusinessVerificationTrustJourneyJson(sautr: String,
                                                     journeyId: String,
                                                     journeyConfig: JourneyConfig): JsObject =
    testCreateBusinessVerificationTrustJourneyJson(
      utrJson = testBVSaUtrJson(sautr),
      continueUrlForBVCall = trustControllersRoutes.BusinessVerificationController.retrieveBusinessVerificationResult(journeyId),
      journeyConfig = journeyConfig
    )

  def testCreateBusinessVerificationUAJourneyJson(ctutr: String,
                                                  journeyId: String,
                                                  journeyConfig: JourneyConfig): JsObject =
    testCreateBusinessVerificationUAJourneyJson(
      utrJson = testBVCtUtrJson(ctutr),
      continueUrlForBVCall = uaControllers.routes.BusinessVerificationController.retrieveBusinessVerificationResult(journeyId),
      journeyConfig = journeyConfig
    )

  def testBVSaUtrJson(utr: String): JsObject = Json.obj("saUtr" -> utr)

  def testBVCtUtrJson(utr: String): JsObject = Json.obj("ctUtr" -> utr)

  def testCreateBusinessVerificationTrustJourneyJson(utrJson: JsObject,
                                                     continueUrlForBVCall: Call,
                                                     journeyConfig: JourneyConfig): JsObject = {

    testCreateBusinessVerificationUAJourneyJson(utrJson, continueUrlForBVCall, journeyConfig) ++
      Json.obj("entityType" -> "TRUST")
  }

  def testCreateBusinessVerificationUAJourneyJson(utrJson: JsObject,
                                                  continueUrlForBVCall: Call,
                                                  journeyConfig: JourneyConfig): JsObject = {

    val callingService: String = journeyConfig.pageConfig.optServiceName.getOrElse(testDefaultServiceName)

    Json.obj("continueUrl" -> continueUrlForBVCall.url,
      "origin" -> journeyConfig.regime,
      "deskproServiceName" -> journeyConfig.pageConfig.deskProServiceId,
      "accessibilityStatementUrl" -> journeyConfig.pageConfig.accessibilityUrl,
      "pageTitle" -> callingService,
      "journeyType" -> "BUSINESS_VERIFICATION",
      "identifiers" -> Json.arr(utrJson)
    )
  }

  def testBVRedirectURIJson(redirectUrl: String): JsObject = Json.obj("redirectUri" -> redirectUrl)

  def testRegisterUAJson(utr: String, regime: String): JsObject = Json.obj(
    "ctutr" -> utr.toUpperCase,
    "regime" -> regime
  )

  val testRegistrationNotCalledJson: JsObject = Json.obj("registrationStatus" -> "REGISTRATION_NOT_CALLED")

  def testRegistrationJourneyDataPart(value: String): JsObject = registrationJson(content = Json.obj("registrationStatus" -> value))

  def testBackendFailedRegistrationJson(failures: JsArray): JsObject = registrationJson(content = Json.obj(
    "registrationStatus" -> "REGISTRATION_FAILED",
    "failures" -> failures
  ))

  def testBackEndRegisteredJson(safeId: String): JsObject = registrationJson(content = testSuccessfulRegistrationJson(safeId))

  def testSuccessfulRegistrationJson(safeId: String): JsObject = Json.obj(
    "registrationStatus" -> "REGISTERED",
    "registeredBusinessPartnerId" -> safeId)

  private def registrationJson(content: JsObject): JsObject = Json.obj("registration" -> content)

  val expectedBVTrustsJson: JsObject = testCreateBusinessVerificationTrustJourneyJson(
    testSautr,
    testJourneyId,
    testTrustsJourneyConfig(businessVerificationCheck = true).copy(regime = testRegime.toLowerCase))

  val expectedBvUAJson: JsObject = testCreateBusinessVerificationUAJourneyJson(
    testCtutr,
    testJourneyId,
    testUnincorporatedAssociationJourneyConfig(businessVerificationCheck = true).copy(regime = testRegime.toLowerCase))
}
