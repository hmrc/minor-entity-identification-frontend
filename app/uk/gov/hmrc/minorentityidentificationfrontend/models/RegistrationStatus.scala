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
import uk.gov.hmrc.http.InternalServerException

sealed trait RegistrationStatus

case class Registered(registeredBusinessPartnerId: String) extends RegistrationStatus

case class RegistrationFailed(registrationFailures: Array[Failure]) extends RegistrationStatus

case object RegistrationNotCalled extends RegistrationStatus

case class Failure(code: String, reason: String)

object RegistrationStatus {
  val registrationStatusKey = "registrationStatus"
  val registeredBusinessPartnerIdKey = "registeredBusinessPartnerId"
  val RegisteredKey = "REGISTERED"
  val RegistrationFailedKey = "REGISTRATION_FAILED"
  val RegistrationNotCalledKey = "REGISTRATION_NOT_CALLED"
  val registrationFailuresKey = "failures"

  implicit val failuresFormat: OFormat[Failure] = Json.format[Failure]

  implicit val format: OFormat[RegistrationStatus] = new OFormat[RegistrationStatus] {
    override def writes(registrationStatus: RegistrationStatus): JsObject =
      registrationStatus match {
        case Registered(businessPartnerId) => Json.obj(
          registrationStatusKey -> RegisteredKey,
          registeredBusinessPartnerIdKey -> businessPartnerId
        )
        case RegistrationFailed(failures) => Json.obj(
          registrationStatusKey -> RegistrationFailedKey,
          registrationFailuresKey -> failures)
        case RegistrationNotCalled =>
          Json.obj(registrationStatusKey -> RegistrationNotCalledKey)
        case _ =>
          throw new InternalServerException("Invalid registration status")
      }

    override def reads(json: JsValue): JsResult[RegistrationStatus] =
      (json \ registrationStatusKey).validate[String] match {
        case JsSuccess(RegisteredKey, _) =>
          (json \ registeredBusinessPartnerIdKey).validate[String].map {
            businessPartnerId => Registered(businessPartnerId)
          }
        case JsSuccess(RegistrationFailedKey, path) =>
          (json \ registrationFailuresKey).validate[Array[Failure]].map {
            failures => RegistrationFailed(failures)
          }
        case JsSuccess(RegistrationNotCalledKey, path) =>
          JsSuccess(RegistrationNotCalled, path)
        case _ =>
          throw new InternalServerException("Invalid registration status")
      }
  }
}
