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

package uk.gov.hmrc.minorentityidentificationfrontend.helpers

import uk.gov.hmrc.minorentityidentificationfrontend.models.{Ctutr, JourneyConfig, PageConfig, Sautr, Utr}

import java.util.UUID
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.minorentityidentificationfrontend.models.BusinessEntity.{BusinessEntity, OverseasCompany}

object TestConstants {

  val testJourneyId: String = UUID.randomUUID().toString
  val testInternalId: String = UUID.randomUUID().toString
  val testContinueUrl: String = "/test"
  val testSignOutUrl: String = "/sign-out"
  val testAccessibilityUrl: String = "/accessibility"

  def testJourneyConfig(businessEntity: BusinessEntity): JourneyConfig = JourneyConfig(
    continueUrl = testContinueUrl,
    pageConfig = PageConfig(
      optServiceName = None,
      deskProServiceId = "vrs",
      signOutUrl = testSignOutUrl,
      accessibilityUrl = testAccessibilityUrl
    ),
    businessEntity = businessEntity
  )

  val saUtr = "1234599999"
  val ctUtr = "1234500000"

  val testSaUtr = Sautr(saUtr)
  val testCtUtr = Ctutr(ctUtr)

  val testOverseasSAUtrAuditEventJson: JsObject = Json.obj(
    "businessType" -> "Overseas Company",
    "etmpPartyType" -> "55",
    "VerificationStatus" -> Json.obj("verificationStatus" -> "UNCHALLENGED"),
    "RegisterApiStatus" -> Json.obj("registrationStatus" -> "REGISTRATION_NOT_CALLED"),
    "sautrMatch" -> false,
    "userSAUTR" -> saUtr)

  val testOverseasCTUtrAuditEventJson: JsObject = Json.obj(
    "businessType" -> "Overseas Company",
    "etmpPartyType" -> "55",
    "VerificationStatus" -> Json.obj("verificationStatus" -> "UNCHALLENGED"),
    "RegisterApiStatus" -> Json.obj("registrationStatus" -> "REGISTRATION_NOT_CALLED"),
    "cTUTRMatch" -> false,
    "userCTUTR" -> ctUtr)

  val testUnincorporatedAssociationAuditEventJson: JsObject = Json.obj(
    "businessType" -> "Unincorporated Association",
    "VerificationStatus" -> Json.obj("verificationStatus" -> "UNCHALLENGED"),
    "RegisterApiStatus" -> Json.obj("registrationStatus" -> "REGISTRATION_NOT_CALLED"),
    "identifiersMatch" -> false)

  val testTrustsAuditEventJson: JsObject = Json.obj(
    "businessType" -> "Trusts",
    "VerificationStatus" -> Json.obj("verificationStatus" -> "UNCHALLENGED"),
    "RegisterApiStatus" -> Json.obj("registrationStatus" -> "REGISTRATION_NOT_CALLED"),
    "identifiersMatch" -> false)
}
