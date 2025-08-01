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
  val testOverseasTaxIdentifier: String = "134124532"
  val testOverseasTaxIdentifierCountry: String = "AL"
  val testSaPostcode = "AA1 1AA"
  val testOfficePostcode = "AA2 2AA"
  val testCHRN: String = UUID.randomUUID().toString
  val testSafeId: String = UUID.randomUUID().toString

  val testRegistrationStatusRegistered: Registered = Registered(UUID.randomUUID().toString)

  def testTrustJourneyConfig(businessVerificationCheck: Boolean = true): JourneyConfig = testJourneyConfig(Trusts, businessVerificationCheck)
  def testUAJourneyConfig(businessVerificationCheck: Boolean = true): JourneyConfig = testJourneyConfig(UnincorporatedAssociation, businessVerificationCheck)

  def testUnincorporatedAssociationJourneyConfig(businessVerificationCheck: Boolean = true): JourneyConfig =
    testJourneyConfig(UnincorporatedAssociation, businessVerificationCheck)

  def testOverseasJourneyConfig(businessVerificationCheck: Boolean = true): JourneyConfig = testJourneyConfig(OverseasCompany, businessVerificationCheck)

  def testJourneyConfig(businessEntity: BusinessEntity, businessVerificationCheck: Boolean = true): JourneyConfig = JourneyConfig(
    continueUrl = testContinueUrl,
    pageConfig = testDefaultPageConfig,
    businessEntity = businessEntity,
    businessVerificationCheck = businessVerificationCheck,
    testRegime
  )

  val testDefaultPageConfig: PageConfig = PageConfig(None, "vrs", testSignOutUrl, testAccessibilityUrl, optLabels = None)

  val testOverseasSAUtrAuditEventJson: JsObject = Json.obj(
    "callingService" -> testDefaultServiceName,
    "businessType" -> "Overseas Company",
    "VerificationStatus" -> "Not Enough Information to call BV",
    "RegisterApiStatus" -> "not called",
    "isMatch" -> "unmatchable",
    "userSAUTR" -> testSautr)

  val testOverseasTaxIdentifierAuditEventJson: JsObject = Json.obj(
    "callingService" -> testDefaultServiceName,
    "businessType" -> "Overseas Company",
    "VerificationStatus" -> "Not Enough Information to call BV",
    "RegisterApiStatus" -> "not called",
    "isMatch" -> "unmatchable",
    "overseasTaxIdentifier" -> testOverseasTaxIdentifier,
    "overseasTaxIdentifierCountry" -> testOverseasTaxIdentifierCountry)

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
    "overseasTaxIdentifier" -> testOverseasTaxIdentifier,
    "overseasTaxIdentifierCountry" -> testOverseasTaxIdentifierCountry
  )

  val testOverseasJson: JsObject = Json.obj(
    "overseas" -> Json.obj(
      "taxIdentifier" -> testOverseasTaxIdentifier,
      "country" -> testOverseasTaxIdentifierCountry
    )
  )

  val testTrustKnownFactsResponse: TrustKnownFacts = TrustKnownFacts(Some(testSaPostcode), Some(testSaPostcode), isAbroad = false)
  val testTrustKnownFactsAbroadResponse: TrustKnownFacts = TrustKnownFacts(None, None, isAbroad = true)
  val testBusinessVerificationRedirectUrl = "/business-verification-start"
  val testCannotConfirmErrorPageUrl = "/bla/bla/someErrorPageUrl"

  val testOverseasSautrAuditDataJson: JsObject = Json.obj(
    "VerificationStatus" -> "Not Enough Information to call BV",
    "RegisterApiStatus" -> "not called",
    "isMatch" -> "unmatchable",
    "userSAUTR" -> testSautr
  )

  def testOverseasSautrDataJson(businessVerificationStatus: Option[String] = None): JsObject = {
    val businessVerificationBlock: JsObject = businessVerificationStatus match {
      case Some(status) => Json.obj("businessVerification" -> Json.obj(
        "verificationStatus" -> status
      ))
      case None => Json.obj()
    }

    val sautrBlock: JsObject = Json.obj(
      "registration" -> Json.obj(
        "registrationStatus" -> "REGISTRATION_NOT_CALLED"
      ),
      "identifiersMatch" -> false,
      "sautr" -> testSautr
    )

    sautrBlock ++ businessVerificationBlock
  }

  val testOverseasTaxIdentifierDataJson: JsObject = Json.obj(
    "VerificationStatus" -> "Not Enough Information to call BV",
    "RegisterApiStatus" -> "not called",
    "isMatch" -> "unmatchable",
    "overseasTaxIdentifier" -> testOverseasTaxIdentifier,
    "overseasTaxIdentifierCountry" -> testOverseasTaxIdentifierCountry
  )

  val testOverseasCtutrDataJson: JsObject = Json.obj(
    "VerificationStatus" -> "Not Enough Information to call BV",
    "RegisterApiStatus" -> "not called",
    "isMatch" -> "unmatchable",
    "userCTUTR" -> testCtutr
  )

  val testOverseasTaxIdentifiersDataJson: JsObject = Json.obj(
    "VerificationStatus" -> "Not Enough Information to call BV",
    "RegisterApiStatus" -> "not called",
    "isMatch" -> "unmatchable"
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
    "CHRN" -> testCHRN.toUpperCase,
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
    "VerificationStatus" -> "Not Enough Information to call BV",
    "RegisterApiStatus" -> "not called"
  )

  def testRegistrationStatusJson(value: String): JsObject = Json.obj("registrationStatus" -> value)

  val testRegistrationFailure: Array[Failure] = Array(Failure(code = "PARTY_TYPE_MISMATCH", reason ="The remote endpoint has indicated there is Party Type mismatch"))

}
