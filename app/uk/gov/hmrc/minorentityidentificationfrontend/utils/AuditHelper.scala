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

package uk.gov.hmrc.minorentityidentificationfrontend.utils

import uk.gov.hmrc.minorentityidentificationfrontend.models._

object AuditHelper {

  def defineAuditIdentifiersMatch(optIdentifiersMatch: Option[KnownFactsMatchingResult]): String =
    optIdentifiersMatch match {
      case Some(SuccessfulMatch) => "true"
      case Some(DetailsMismatch) | Some(DetailsNotFound) => "false"
      case Some(UnMatchable) => "unmatchable"
      case None => "false"
    }

  def defineAuditRegistrationStatus(optRegistrationStatus: Option[RegistrationStatus]): String =
    optRegistrationStatus match {
      case Some(Registered(_)) => "success"
      case Some(RegistrationFailed) => "fail"
      case _ => "not called"
    }

  def defineAuditBusinessVerificationStatus(optBusinessVerificationStatus: Option[BusinessVerificationStatus],
                                            businessVerificationCheck: Boolean): String = {
    if (!businessVerificationCheck) "not requested"
    else {
      optBusinessVerificationStatus match {
        case Some(BusinessVerificationPass) => "success"
        case Some(BusinessVerificationFail) => "fail"
        case Some(BusinessVerificationNotEnoughInformationToCallBV) | None => "Not enough information to call BV"
        case Some(BusinessVerificationNotEnoughInformationToChallenge) => "Not Enough Information to challenge"
      }
    }
  }
}
