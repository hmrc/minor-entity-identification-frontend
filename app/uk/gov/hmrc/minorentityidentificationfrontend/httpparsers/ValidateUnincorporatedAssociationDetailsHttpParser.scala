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

package uk.gov.hmrc.minorentityidentificationfrontend.httpparsers

import play.api.http.Status.{BAD_REQUEST, OK}
import play.api.libs.json.JsSuccess
import uk.gov.hmrc.http.{HttpReads, HttpResponse, InternalServerException}
import uk.gov.hmrc.minorentityidentificationfrontend.models.{DetailsMismatch, DetailsNotFound, KnownFactsMatchingResult, SuccessfulMatch}

object ValidateUnincorporatedAssociationDetailsHttpParser {

  implicit object ValidateUnincorporatedAssociationDetailsHttpReads extends HttpReads[KnownFactsMatchingResult] {

    override def read(method: String, url: String, response: HttpResponse): KnownFactsMatchingResult = {

      def createInternalServerException(response: HttpResponse): InternalServerException =
        new InternalServerException(s"Invalid response from validate unincorporated association details: ${response.status}: ${response.body}")

      response.status match {
        case OK =>
          (response.json \ "matched").validate[Boolean] match {
            case JsSuccess(true, _) => SuccessfulMatch
            case JsSuccess(false, _) => DetailsMismatch
            case _ => throw createInternalServerException(response)
          }
        case BAD_REQUEST =>
          (response.json \ "code").validate[String] match {
            case JsSuccess("NOT_FOUND", _) => DetailsNotFound
            case _ => throw createInternalServerException(response)
          }
        case _ => throw createInternalServerException(response)
      }

    }
  }

}
