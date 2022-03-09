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
import uk.gov.hmrc.minorentityidentificationfrontend.models.BusinessVerificationStatus.writeForJourneyContinuation
import uk.gov.hmrc.minorentityidentificationfrontend.models.RegistrationStatus.{format => regFormat}

case class UADetails(optUtr: Option[Utr],
                     optCtPostcode: Option[String],
                     optChrn: Option[String],
                     optIdentifiersMatch: Option[KnownFactsMatchingResult],
                     businessVerificationStatus: Option[BusinessVerificationStatus],
                     registrationStatus: Option[RegistrationStatus])

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
      case Some(charityHMRCReferenceNumber) => Json.obj("chrn" -> charityHMRCReferenceNumber)
      case None => Json.obj()
    }

    val businessVerificationBlock: JsObject =
      if (!businessVerificationCheck) Json.obj()
      else Json.obj("businessVerification" -> Json.toJson(Json.toJson(writeForJourneyContinuation(BusinessVerificationNotEnoughInformationToChallenge))))

    Json.obj("identifiersMatch" -> uaDetails.optIdentifiersMatch.contains(SuccessfulMatch),
      "registration" -> Json.toJson(RegistrationNotCalled)(regFormat.writes)
    ) ++ utrBlock ++ saPostcodeBlock ++ chrnBlock ++ businessVerificationBlock
  }
}
