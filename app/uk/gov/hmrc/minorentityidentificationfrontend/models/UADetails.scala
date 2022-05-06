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

package uk.gov.hmrc.minorentityidentificationfrontend.models

import play.api.libs.json._
import uk.gov.hmrc.minorentityidentificationfrontend.utils.AuditHelper._
import uk.gov.hmrc.minorentityidentificationfrontend.utils.WritesForJourneyEnd

case class UADetails(optUtr: Option[Utr],
                     optCtPostcode: Option[String],
                     optChrn: Option[String],
                     optIdentifiersMatch: Option[KnownFactsMatchingResult],
                     optBusinessVerificationStatus: Option[BusinessVerificationStatus],
                     optRegistrationStatus: Option[RegistrationStatus])

object UADetails {
  def writesForJourneyEnd(uaDetails: UADetails, businessVerificationCheck: Boolean): JsObject = {
    val utrBlock: JsObject = uaDetails.optUtr match {
      case Some(utr) => Json.obj(utr.utrType -> utr.value)
      case None => Json.obj()
    }

    val saPostcodeBlock: JsObject = uaDetails.optCtPostcode match {
      case Some(saPostcode) => Json.obj("ctPostcode" -> saPostcode)
      case None => Json.obj()
    }

    val chrnBlock: JsObject = uaDetails.optChrn match {
      case Some(charityHMRCReferenceNumber) => Json.obj("chrn" -> charityHMRCReferenceNumber.toUpperCase)
      case None => Json.obj()
    }

    val businessVerificationBlock: JsObject = WritesForJourneyEnd.businessVerificationBlock(uaDetails.optBusinessVerificationStatus, businessVerificationCheck)

    val registrationBlock: JsObject = WritesForJourneyEnd.registrationBlock(uaDetails.optRegistrationStatus)

    val identifiersMatchBlock: JsObject = Json.obj("identifiersMatch" -> uaDetails.optIdentifiersMatch.contains(SuccessfulMatch))

    identifiersMatchBlock ++
      registrationBlock ++
      utrBlock ++
      saPostcodeBlock ++
      chrnBlock ++
      businessVerificationBlock
  }

  def writesForAudit(optUADetails: Option[UADetails], businessVerificationCheck: Boolean): JsObject = {
    optUADetails match {
      case Some(uaDetails) =>
        val optCtutrBlock = uaDetails.optUtr match {
          case Some(ctutr) => Json.obj("CTUTR" -> ctutr.value)
          case None => Json.obj()
        }

        val optPostCodeBlock = uaDetails.optCtPostcode match {
          case Some(postCode) => Json.obj("CTpostcode" -> postCode)
          case None => Json.obj()
        }

        val optCHRNBlock = uaDetails.optChrn match {
          case Some(chrn) => Json.obj("CHRN" -> chrn.toUpperCase)
          case None => Json.obj()
        }

        Json.obj(
          "isMatch" -> defineAuditIdentifiersMatch(uaDetails.optIdentifiersMatch),
          "VerificationStatus" -> defineAuditBusinessVerificationStatus(uaDetails.optBusinessVerificationStatus, businessVerificationCheck),
          "RegisterApiStatus" -> defineAuditRegistrationStatus(uaDetails.optRegistrationStatus)
        ) ++ optCtutrBlock ++ optPostCodeBlock ++ optCHRNBlock
      case None =>
        Json.obj(
          "isMatch" -> defineAuditIdentifiersMatch(None),
          "VerificationStatus" -> defineAuditBusinessVerificationStatus(None, businessVerificationCheck),
          "RegisterApiStatus" -> defineAuditRegistrationStatus(None)
        )
    }
  }
}
