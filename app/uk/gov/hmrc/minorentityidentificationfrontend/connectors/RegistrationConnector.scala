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

package uk.gov.hmrc.minorentityidentificationfrontend.connectors

import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.{JsObject, Json, Writes}
import uk.gov.hmrc.http._
import uk.gov.hmrc.minorentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.minorentityidentificationfrontend.connectors.RegistrationHttpParser.RegistrationHttpReads
import uk.gov.hmrc.minorentityidentificationfrontend.models.{RegistrationFailed, RegistrationStatus}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegistrationConnector @Inject()(httpClient: HttpClient,
                                      appConfig: AppConfig
                                     )(implicit ec: ExecutionContext) {

  def register(sautr: String, regime: String)(implicit hc: HeaderCarrier): Future[RegistrationStatus] = {

    val jsonBody = Json.obj(
      "sautr" -> sautr.toUpperCase,
      "regime" -> regime
    )

    httpClient.POST[JsObject, RegistrationStatus](appConfig.registerUrl, jsonBody)(
      implicitly[Writes[JsObject]],
      RegistrationHttpReads,
      hc,
      ec
    )
  }
}

object RegistrationHttpParser {
  val registrationKey = "registration"

  implicit object RegistrationHttpReads extends HttpReads[RegistrationStatus] {
    override def read(method: String, url: String, response: HttpResponse): RegistrationStatus = {
      response.status match {
        case OK =>
          (response.json \ registrationKey).as[RegistrationStatus]
        case INTERNAL_SERVER_ERROR =>
          RegistrationFailed
        case _ =>
          throw new InternalServerException(s"Unexpected response from Register API - status = ${response.status}, body = ${response.body}")
      }
    }
  }
}