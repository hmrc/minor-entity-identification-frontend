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

package uk.gov.hmrc.minorentityidentificationfrontend.connectors

import play.api.http.Status.OK
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.minorentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.minorentityidentificationfrontend.models.RegistrationStatus

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegistrationConnector @Inject()(httpClient: HttpClientV2,
                                      appConfig: AppConfig
                                     )(implicit ec: ExecutionContext) {

  import RegistrationHttpParser.RegistrationHttpReads

  private def register(jsonBody: JsObject, postUrl: String)(implicit hc: HeaderCarrier): Future[RegistrationStatus] = {
    httpClient.post(url"$postUrl").withBody(Json.toJson(jsonBody)).execute[RegistrationStatus](RegistrationHttpReads, ec)
  }

  def registerTrust(sautr: String, regime: String)(implicit hc: HeaderCarrier): Future[RegistrationStatus] =
    register(
      jsonBody = Json.obj(
        "sautr" -> sautr.toUpperCase,
        "regime" -> regime
      ),
      postUrl = appConfig.registerTrustUrl)


  def registerUA(ctutr: String, regime: String)(implicit hc: HeaderCarrier): Future[RegistrationStatus] =
    register(
      jsonBody = Json.obj(
        "ctutr" -> ctutr.toUpperCase,
        "regime" -> regime
      ),
      postUrl = appConfig.registerUAUrl)
}

object RegistrationHttpParser {
  val registrationKey = "registration"

  implicit object RegistrationHttpReads extends HttpReads[RegistrationStatus] {
    override def read(method: String, url: String, response: HttpResponse): RegistrationStatus = {
      response.status match {
        case OK =>
          (response.json \ registrationKey).as[RegistrationStatus]
        case _ =>
          throw new InternalServerException(s"Unexpected response from Register API - status = ${response.status}, body = ${response.body}")
      }
    }
  }

}
