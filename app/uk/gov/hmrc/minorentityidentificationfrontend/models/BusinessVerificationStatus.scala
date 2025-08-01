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

import play.api.libs.json._

sealed trait BusinessVerificationStatus

case object BusinessVerificationPass extends BusinessVerificationStatus

case object BusinessVerificationFail extends BusinessVerificationStatus

case object BusinessVerificationNotEnoughInformationToChallenge extends BusinessVerificationStatus

case object BusinessVerificationNotEnoughInformationToCallBV extends BusinessVerificationStatus

object BusinessVerificationStatus {
  val BusinessVerificationPassKey = "PASS"
  val BusinessVerificationFailKey = "FAIL"
  val BusinessVerificationUnchallengedKey = "UNCHALLENGED"
  val BusinessVerificationNotEnoughInfoToChallengeKey = "NOT_ENOUGH_INFORMATION_TO_CHALLENGE"
  val BusinessVerificationNotEnoughInfoToCallKey = "NOT_ENOUGH_INFORMATION_TO_CALL_BV"
  val BusinessVerificationStatusKey = "verificationStatus"

  implicit val format: Format[BusinessVerificationStatus] = new Format[BusinessVerificationStatus] {
    override def writes(businessVerificationStatus: BusinessVerificationStatus): JsObject = {
      val businessVerificationStatusString = businessVerificationStatus match {
        case BusinessVerificationPass => BusinessVerificationPassKey
        case BusinessVerificationFail => BusinessVerificationFailKey
        case BusinessVerificationNotEnoughInformationToChallenge => BusinessVerificationNotEnoughInfoToChallengeKey
        case BusinessVerificationNotEnoughInformationToCallBV => BusinessVerificationNotEnoughInfoToCallKey
      }
      Json.obj(BusinessVerificationStatusKey -> businessVerificationStatusString)
    }

    override def reads(json: JsValue): JsResult[BusinessVerificationStatus] =
      (json \ BusinessVerificationStatusKey).validate[String].collect(JsonValidationError("Invalid business validation state")) {
        case BusinessVerificationPassKey => BusinessVerificationPass
        case BusinessVerificationFailKey => BusinessVerificationFail
        case BusinessVerificationNotEnoughInfoToChallengeKey => BusinessVerificationNotEnoughInformationToChallenge
        case BusinessVerificationNotEnoughInfoToCallKey => BusinessVerificationNotEnoughInformationToCallBV
      }
  }

  def writeForJourneyContinuation(businessVerificationStatus: BusinessVerificationStatus): JsObject = {
    val businessVerificationStatusString = businessVerificationStatus match {
      case BusinessVerificationPass => BusinessVerificationPassKey
      case BusinessVerificationFail => BusinessVerificationFailKey
      case BusinessVerificationNotEnoughInformationToChallenge | BusinessVerificationNotEnoughInformationToCallBV => BusinessVerificationUnchallengedKey
    }
    Json.obj(BusinessVerificationStatusKey -> businessVerificationStatusString)
  }
}
