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

package uk.gov.hmrc.minorentityidentificationfrontend.httpparsers

import play.api.http.Status.{NOT_FOUND, OK}
import play.api.libs.json.{JsError, JsResult, JsSuccess, JsValue}
import uk.gov.hmrc.http.{HttpReads, HttpResponse, InternalServerException}
import uk.gov.hmrc.minorentityidentificationfrontend.models._

object RetrieveUADetailsHttpParser {

  implicit object RetrieveUADetailsHttpReads extends HttpReads[Option[UADetails]] {
    override def read(method: String, url: String, response: HttpResponse): Option[UADetails] = {
      response.status match {
        case OK =>
          response.json.validate[UADetails](reads(_)) match {
            case JsSuccess(uaDetails, _) => Some(uaDetails)
            case JsError(errors) =>
              throw new InternalServerException(s"`Failed to read UA Details with the following error/s: $errors")
          }
        case NOT_FOUND =>
          None
        case status =>
          throw new InternalServerException(s"Unexpected status from UA Details retrieval. Status returned - $status")
      }
    }
  }

  def reads(json: JsValue): JsResult[UADetails] = {
    for {
      optUtr <- (json \ "utr" \ "value").validateOpt[String]
      optPostcode <- (json \ "postcode").validateOpt[String]
      optChrn <- (json \ "chrn").validateOpt[String]
      optIdentifiersMatch <- (json \ "identifiersMatch").validateOpt[KnownFactsMatchingResult]
      optBusinessVerificationStatus <- (json \ "businessVerification").validateOpt[BusinessVerificationStatus]
      optRegistrationStatus <- (json \ "registration").validateOpt[RegistrationStatus]
    } yield {
      val optSautr = if (optUtr.nonEmpty) Some(Ctutr(optUtr.get)) else None
      UADetails(optSautr, optPostcode, optChrn, optIdentifiersMatch, optBusinessVerificationStatus, optRegistrationStatus)
    }
  }

}
