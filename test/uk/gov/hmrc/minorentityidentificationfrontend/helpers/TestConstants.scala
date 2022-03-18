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

package uk.gov.hmrc.minorentityidentificationfrontend.helpers

import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.minorentityidentificationfrontend.models.BusinessEntity.{BusinessEntity, OverseasCompany, Trusts, UnincorporatedAssociation}
import uk.gov.hmrc.minorentityidentificationfrontend.models._

import java.util.UUID

object TestConstants {

  val testJourneyId: String = UUID.randomUUID().toString
  val testInternalId: String = UUID.randomUUID().toString
  val testContinueUrl: String = "/test"
  val testSignOutUrl: String = "/sign-out"
  val testAccessibilityUrl: String = "/accessibility"
  val testDefaultServiceName: String = "Entity Validation Service"
  val testRegime: String = "VATC"

  val testSautr: String = "1234599999"
  val testCtutr: String = "1234500000"
  val testOverseas: Overseas = Overseas("134124532", "AL")
  val testSaPostcode = "AA1 1AA"
  val testOfficePostcode = "AA2 2AA"
  val testCHRN: String = UUID.randomUUID().toString
  val testSafeId: String = UUID.randomUUID().toString

  def testTrustJourneyConfig(businessVerificationCheck: Boolean = true): JourneyConfig = testJourneyConfig(Trusts, businessVerificationCheck)
  def testUAJourneyConfig(businessVerificationCheck: Boolean = true): JourneyConfig = testJourneyConfig(UnincorporatedAssociation, businessVerificationCheck)

  def testUnincorporatedAssociationJourneyConfig(businessVerificationCheck: Boolean = true): JourneyConfig =
    testJourneyConfig(UnincorporatedAssociation, businessVerificationCheck)

  def testOverseasJourneyConfig(businessVerificationCheck: Boolean = true): JourneyConfig = testJourneyConfig(OverseasCompany, businessVerificationCheck)

  def testJourneyConfig(businessEntity: BusinessEntity, businessVerificationCheck: Boolean = true): JourneyConfig = JourneyConfig(
    continueUrl = testContinueUrl,
    pageConfig = PageConfig(
      optServiceName = None,
      deskProServiceId = "vrs",
      signOutUrl = testSignOutUrl,
      accessibilityUrl = testAccessibilityUrl
    ),
    businessEntity = businessEntity,
    businessVerificationCheck = businessVerificationCheck,
    testRegime
  )

  val testOverseasSAUtrAuditEventJson: JsObject = Json.obj(
    "callingService" -> testDefaultServiceName,
    "businessType" -> "Overseas Company",
    "VerificationStatus" -> Json.obj("verificationStatus" -> "UNCHALLENGED"),
    "RegisterApiStatus" -> "not called",
    "isMatch" -> "false",
    "userSAUTR" -> testSautr)

  val testUnincorporatedAssociationAuditEventJson: JsObject = Json.obj(
    "callingService" -> testDefaultServiceName,
    "businessType" -> "Unincorporated Association",
    "VerificationStatus" -> "Not enough information to call BV",
    "RegisterApiStatus" -> "not called",
    "isMatch" -> "unmatchable")

  def testSaUtrAndPostcodeTrustsAuditEventJson(saUtr: String,
                                               saPostCode: String,
                                               identifiersMatch: String,
                                               bvStatus: String,
                                               regStatus: String): JsObject = Json.obj(
    "callingService" -> testDefaultServiceName,
    "businessType" -> "Trusts",
    "SAUTR" -> saUtr,
    "SApostcode" -> saPostCode,
    "isMatch" -> identifiersMatch,
    "VerificationStatus" -> bvStatus,
    "RegisterApiStatus" -> regStatus
  )

  def testSaUtrOnlyTrustsAuditEventJson(identifiersMatch: String,
                                        bvStatus: String,
                                        regStatus: String): JsObject = Json.obj(
    "callingService" -> testDefaultServiceName,
    "businessType" -> "Trusts",
    "SAUTR" -> testSautr,
    "isMatch" -> identifiersMatch,
    "VerificationStatus" -> bvStatus,
    "RegisterApiStatus" -> regStatus
  )

  def testCHRNOnlyTrustsAuditEventJson(identifiersMatch: String,
                                       bvStatus: String,
                                       regStatus: String): JsObject = Json.obj(
    "callingService" -> testDefaultServiceName,
    "businessType" -> "Trusts",
    "CHRN" -> testCHRN,
    "isMatch" -> identifiersMatch,
    "VerificationStatus" -> bvStatus,
    "RegisterApiStatus" -> regStatus
  )

  def testNoIdentifiersTrustsAuditEventJson(identifiersMatch: String,
                                            bvStatus: String,
                                            regStatus: String): JsObject = Json.obj(
    "callingService" -> testDefaultServiceName,
    "businessType" -> "Trusts",
    "isMatch" -> identifiersMatch,
    "VerificationStatus" -> bvStatus,
    "RegisterApiStatus" -> regStatus
  )

  val testOverseasTaxIdentifiersJson: JsObject = Json.obj(
    "overseasTaxIdentifier" -> testOverseas.taxIdentifier,
    "overseasTaxIdentifierCountry" -> testOverseas.country
  )

  val testTrustKnownFactsResponse: TrustKnownFacts = TrustKnownFacts(Some(testSaPostcode), Some(testSaPostcode), isAbroad = false)
  val testTrustKnownFactsAbroadResponse: TrustKnownFacts = TrustKnownFacts(None, None, isAbroad = true)
  val testBusinessVerificationRedirectUrl = "/business-verification-start"

  val testOverseasSautrDataJson: JsObject = Json.obj(
    "VerificationStatus" -> Json.obj("verificationStatus" -> "UNCHALLENGED"),
    "RegisterApiStatus" -> "not called",
    "isMatch" -> "false",
    "userSAUTR" -> testSautr
  )

  val testOverseasCtutrDataJson: JsObject = Json.obj(
    "VerificationStatus" -> Json.obj("verificationStatus" -> "UNCHALLENGED"),
    "RegisterApiStatus" -> "not called",
    "isMatch" -> "false",
    "userCTUTR" -> testCtutr
  )

  val testOverseasTaxIdentifiersDataJson: JsObject = Json.obj(
    "VerificationStatus" -> Json.obj("verificationStatus" -> "UNCHALLENGED"),
    "RegisterApiStatus" -> "not called",
    "isMatch" -> "false"
  )

  val testLegacyDataJson: JsObject = Json.obj(
    "isMatch" -> "false",
    "VerificationStatus" -> "Not enough information to call BV",
    "RegisterApiStatus" -> "not called"
  )

  val testUADataJson: JsObject = Json.obj(
    "CTUTR" -> testCtutr,
    "CTpostcode" -> testOfficePostcode,
    "isMatch" -> "true",
    "VerificationStatus" -> "success",
    "RegisterApiStatus" -> "success"
  )

  def testUABvFailedDataJson(bvStatus: String): JsObject = Json.obj(
    "CTUTR" -> testCtutr,
    "CTpostcode" -> testOfficePostcode,
    "isMatch" -> "true",
    "VerificationStatus" -> bvStatus,
    "RegisterApiStatus" -> "not called"
  )

  val testUARegistrationFailedDataJson: JsObject = Json.obj(
    "CTUTR" -> testCtutr,
    "CTpostcode" -> testOfficePostcode,
    "isMatch" -> "true",
    "VerificationStatus" -> "success",
    "RegisterApiStatus" -> "fail"
  )

  val testUABvNotRequestedDataJson: JsObject = Json.obj(
    "CTUTR" -> testCtutr,
    "CTpostcode" -> testOfficePostcode,
    "isMatch" -> "true",
    "VerificationStatus" -> "not requested",
    "RegisterApiStatus" -> "success"
  )

  val testUADataJsonNoPostcode: JsObject = Json.obj(
    "CTUTR" -> testCtutr,
    "isMatch" -> "true",
    "VerificationStatus" -> "success",
    "RegisterApiStatus" -> "success"
  )

  val testTrustsDataJson: JsObject = Json.obj(
    "SAUTR" -> testSautr,
    "SApostcode" -> testSaPostcode,
    "isMatch" -> "true",
    "VerificationStatus" -> "success",
    "RegisterApiStatus" -> "success"
  )

  val testTrustsDataJsonNoPostcode: JsObject = Json.obj(
    "SAUTR" -> testSautr,
    "isMatch" -> "true",
    "VerificationStatus" -> "success",
    "RegisterApiStatus" -> "success"
  )

  val testTrustsRegistrationFailedDataJson: JsObject = Json.obj(
    "SAUTR" -> testSautr,
    "SApostcode" -> testSaPostcode,
    "isMatch" -> "true",
    "VerificationStatus" -> "success",
    "RegisterApiStatus" -> "fail"
  )

  def testTrustsBvFailedDataJson(bvStatus: String): JsObject = Json.obj(
    "SAUTR" -> testSautr,
    "SApostcode" -> testSaPostcode,
    "isMatch" -> "true",
    "VerificationStatus" -> bvStatus,
    "RegisterApiStatus" -> "not called"
  )

  val testTrustsIdMatchFailedDataJson: JsObject = Json.obj(
    "SAUTR" -> testSautr,
    "SApostcode" -> testSaPostcode,
    "isMatch" -> "false",
    "VerificationStatus" -> "Not enough information to call BV",
    "RegisterApiStatus" -> "not called"
  )

  val testTrustsBvNotRequestedDataJson: JsObject = Json.obj(
    "SAUTR" -> testSautr,
    "SApostcode" -> testSaPostcode,
    "isMatch" -> "true",
    "VerificationStatus" -> "not requested",
    "RegisterApiStatus" -> "success"
  )

  val testOnlyCHRNDataJson: JsObject = Json.obj(
    "CHRN" -> testCHRN,
    "isMatch" -> "unmatchable",
    "VerificationStatus" -> "Not enough information to call BV",
    "RegisterApiStatus" -> "not called"
  )

  val testNoIdentifiersDataJson: JsObject = Json.obj(
    "isMatch" -> "unmatchable",
    "VerificationStatus" -> "Not enough information to call BV",
    "RegisterApiStatus" -> "not called"
  )

  val testOverseasNoIdentifiersDataJson: JsObject = Json.obj(
    "isMatch" -> "unmatchable",
    "VerificationStatus" -> Json.obj("verificationStatus" -> "UNCHALLENGED"),
    "RegisterApiStatus" -> "not called"
  )
}
