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
import uk.gov.hmrc.minorentityidentificationfrontend.models.BusinessEntity.{BusinessEntity, Trusts}
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
  val testCharityHMRCReferenceNumber: String = UUID.randomUUID().toString

  val testTrustJourneyConfig: JourneyConfig = testJourneyConfig(Trusts)

  def testJourneyConfig(businessEntity: BusinessEntity): JourneyConfig = JourneyConfig(
    continueUrl = testContinueUrl,
    pageConfig = PageConfig(
      optServiceName = None,
      deskProServiceId = "vrs",
      signOutUrl = testSignOutUrl,
      accessibilityUrl = testAccessibilityUrl
    ),
    businessEntity = businessEntity,
    businessVerificationCheck = true,
    testRegime
  )

  val testOverseasSAUtrAuditEventJson: JsObject = Json.obj(
    "callingService" -> testDefaultServiceName,
    "businessType" -> "Overseas Company",
    "VerificationStatus" -> Json.obj("verificationStatus" -> "UNCHALLENGED"),
    "RegisterApiStatus" ->  "not called",
    "sautrMatch" -> false,
    "userSAUTR" -> testSautr)

  val testOverseasCTUtrAuditEventJson: JsObject = Json.obj(
    "callingService" -> testDefaultServiceName,
    "businessType" -> "Overseas Company",
    "VerificationStatus" -> Json.obj("verificationStatus" -> "UNCHALLENGED"),
    "RegisterApiStatus" ->  "not called",
    "cTUTRMatch" -> false,
    "userCTUTR" -> testCtutr)

  val testUnincorporatedAssociationAuditEventJson: JsObject = Json.obj(
    "callingService" -> testDefaultServiceName,
    "businessType" -> "Unincorporated Association",
    "VerificationStatus" -> Json.obj("verificationStatus" -> "UNCHALLENGED"),
    "RegisterApiStatus" ->  "not called",
    "identifiersMatch" -> false)

  val testTrustsAuditEventJson: JsObject = Json.obj(
    "callingService" -> testDefaultServiceName,
    "businessType" -> "Trusts",
    "VerificationStatus" -> Json.obj("verificationStatus" -> "UNCHALLENGED"),
    "RegisterApiStatus" ->  "not called",
    "identifiersMatch" -> false)

  val testOverseasIdentifiersAuditEventJson: JsObject = Json.obj(
    "overseasTaxIdentifier" -> testOverseas.taxIdentifier,
    "overseasTaxIdentifierCountry" -> testOverseas.country
  )

  val testTrustKnownFactsResponse: TrustKnownFacts = TrustKnownFacts(Some(testSaPostcode), Some(testSaPostcode), isAbroad = false)
  val testTrustKnownFactsAbroadResponse: TrustKnownFacts = TrustKnownFacts(None, None, isAbroad = true)

}
