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

package uk.gov.hmrc.minorentityidentificationfrontend.models

import play.api.libs.json.{JsObject, JsResult, JsValue, Json, JsonValidationError, OFormat}

sealed trait UtrType

case object Sautr extends UtrType

case object Ctutr extends UtrType

case class Utr(utrType: UtrType, value: String)

object Utr {
  private val ValueKey = "value"
  private val TypeKey = "type"
  private val CtutrKey = "Ctutr"
  private val SautrKey = "Sautr"

  implicit val format: OFormat[Utr] = new OFormat[Utr] {
    override def reads(json: JsValue): JsResult[Utr] = {
      for {
        utrValue <- (json \ ValueKey).validate[String]
        utrType <- (json \ TypeKey).validate[String].collect(JsonValidationError("Invalid UTR type")) {
          case SautrKey => Sautr
          case CtutrKey => Ctutr
        }
      } yield Utr(utrType, utrValue)
    }

    override def writes(o: Utr): JsObject =
      Json.obj(
        TypeKey -> o.utrType.toString,
        ValueKey -> o.value
      )
  }

}
