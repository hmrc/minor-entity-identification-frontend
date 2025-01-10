/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.minorentityidentificationfrontend.utils

import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.minorentityidentificationfrontend.models.BusinessVerificationStatus.writeForJourneyContinuation
import uk.gov.hmrc.minorentityidentificationfrontend.models._

object WritesForJourneyEnd {

  def registrationBlock(optRegistrationStatus: Option[RegistrationStatus]): JsObject = {
    val regValue = optRegistrationStatus match {
      case Some(regStatus) => Json.toJson(regStatus)(RegistrationStatus.format.writes(_))
      case None => Json.toJson(RegistrationNotCalled)(RegistrationStatus.format.writes(_))
    }
    Json.obj("registration" -> regValue)
  }

  def businessVerificationBlock(optBusinessVerificationStatus: Option[BusinessVerificationStatus], businessVerificationCheck: Boolean): JsObject =
    if (!businessVerificationCheck) Json.obj()
    else {
      val businessVerificationValue = optBusinessVerificationStatus match {
        case Some(status) => writeForJourneyContinuation(status)
        case None => writeForJourneyContinuation(BusinessVerificationNotEnoughInformationToChallenge)
      }
      Json.obj("businessVerification" -> businessVerificationValue)
    }

}