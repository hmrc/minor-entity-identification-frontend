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

package uk.gov.hmrc.minorentityidentificationfrontend.testonly.models

import play.api.libs.json._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.minorentityidentificationfrontend.models.RegistrationStatus

case class Stubs(knownFactsMatch: KnownFactsMatchStub, businessVerificationStub: String, registrationStub: RegistrationStatus)

sealed trait KnownFactsMatchStub

case class GBResponse(postcode: String) extends KnownFactsMatchStub

case object AbroadResponse extends KnownFactsMatchStub

case object KnownFactsNotFound extends KnownFactsMatchStub

object Stubs {
  implicit val format: OFormat[Stubs] = Json.format[Stubs]
}

object KnownFactsMatchStub {
  implicit val format: OFormat[KnownFactsMatchStub] = new OFormat[KnownFactsMatchStub] {
    override def writes(knownFactsMatchStub: KnownFactsMatchStub): JsObject =
      knownFactsMatchStub match {
        case GBResponse(postcode) => Json.obj(
          "knownFactsMatch" -> "GBResponse",
          "postcode" -> postcode
        )
        case AbroadResponse => Json.obj(
          "knownFactsMatch" -> "AbroadResponse")
        case KnownFactsNotFound =>
          Json.obj("knownFactsMatch" -> "NotFound")
        case _ =>
          throw new InternalServerException("Invalid stub status")
      }

    override def reads(json: JsValue): JsResult[KnownFactsMatchStub] =
      (json \ "knownFactsMatch").validate[String] match {
        case JsSuccess("GBResponse", _) =>
          (json \ "postcode").validate[String].map {
            postcode => GBResponse(postcode)
          }
        case JsSuccess("AbroadResponse", _) => JsSuccess(AbroadResponse)
        case JsSuccess("NotFound", _) => JsSuccess(KnownFactsNotFound)
        case _ =>
          throw new InternalServerException("Invalid registration status")
      }
  }
}
