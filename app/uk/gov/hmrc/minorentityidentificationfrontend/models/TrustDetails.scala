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

package uk.gov.hmrc.minorentityidentificationfrontend.models

import play.api.libs.json.Format.GenericFormat
import play.api.libs.json._
import uk.gov.hmrc.minorentityidentificationfrontend.utils.AuditHelper._
import uk.gov.hmrc.minorentityidentificationfrontend.utils.WritesForJourneyEnd

case class TrustDetails(optUtr: Option[Utr],
                        optSaPostcode: Option[String],
                        optChrn: Option[String],
                        optIdentifiersMatch: Option[KnownFactsMatchingResult],
                        optBusinessVerificationStatus: Option[BusinessVerificationStatus],
                        optRegistrationStatus: Option[RegistrationStatus])

object TrustDetails {
  def writesForJourneyEnd(trustDetails: TrustDetails, businessVerificationCheck: Boolean): JsObject = {
    val utrBlock: JsObject = trustDetails.optUtr match {
      case Some(utr) => Json.obj(utr.utrType -> utr.value)
      case None => Json.obj()
    }

    val saPostcodeBlock: JsObject = trustDetails.optSaPostcode match {
      case Some(saPostcode) => Json.obj("saPostcode" -> saPostcode)
      case None => Json.obj()
    }

    val chrnBlock: JsObject = trustDetails.optChrn match {
      case Some(charityHMRCReferenceNumber) => Json.obj("chrn" -> charityHMRCReferenceNumber.toUpperCase)
      case None => Json.obj()
    }

    val businessVerificationBlock: JsObject = WritesForJourneyEnd.businessVerificationBlock(trustDetails.optBusinessVerificationStatus, businessVerificationCheck)

    val registrationBlock: JsObject = WritesForJourneyEnd.registrationBlock(trustDetails.optRegistrationStatus)

    val identifiersMatchBlock: JsObject = Json.obj("identifiersMatch" -> trustDetails.optIdentifiersMatch.contains(SuccessfulMatch))

    registrationBlock ++
      utrBlock ++
      saPostcodeBlock ++
      chrnBlock ++
      businessVerificationBlock ++
      identifiersMatchBlock
  }

  def writesForAudit(optTrustDetails: Option[TrustDetails], businessVerificationCheck: Boolean): JsObject = {
    optTrustDetails match {
      case Some(trustDetails) =>
        val optSaUtrBlock = trustDetails.optUtr match {
          case Some(saUtr) => Json.obj("SAUTR" -> saUtr.value)
          case None => Json.obj()
        }

        val optSaPostCodeBlock = trustDetails.optSaPostcode match {
          case Some(saPostCode) => Json.obj("SApostcode" -> saPostCode)
          case None => Json.obj()
        }

        val optCHRNBlock = trustDetails.optChrn match {
          case Some(chrn) => Json.obj("CHRN" -> chrn.toUpperCase)
          case None => Json.obj()
        }

        Json.obj(
          "isMatch" -> defineAuditIdentifiersMatch(trustDetails.optIdentifiersMatch),
          "VerificationStatus" -> defineAuditBusinessVerificationStatus(trustDetails.optBusinessVerificationStatus, businessVerificationCheck),
          "RegisterApiStatus" -> defineAuditRegistrationStatus(trustDetails.optRegistrationStatus)
        ) ++ optSaUtrBlock ++ optSaPostCodeBlock ++ optCHRNBlock
      case None =>
        Json.obj(
          "isMatch" -> defineAuditIdentifiersMatch(None),
          "VerificationStatus" -> defineAuditBusinessVerificationStatus(None, businessVerificationCheck),
          "RegisterApiStatus" -> defineAuditRegistrationStatus(None)
        )
    }
  }
}
