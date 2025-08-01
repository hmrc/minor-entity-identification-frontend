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

package uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.frontend.connectors

import play.api.http.Status._
import play.api.libs.json.{JsError, JsSuccess, Json, Reads}
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.core.models.FeatureSwitchSetting

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FeatureSwitchApiConnector @Inject()(httpClient: HttpClientV2)(implicit ec: ExecutionContext) {

  def retrieveFeatureSwitches(featureSwitchProviderUrl: String
                             )(implicit reads: Reads[Seq[FeatureSwitchSetting]],
                               hc: HeaderCarrier): Future[Seq[FeatureSwitchSetting]] = {
    httpClient.get(url"$featureSwitchProviderUrl").execute.map(
      response =>
        response.status match {
          case OK =>
            response.json.validate[Seq[FeatureSwitchSetting]] match {
              case JsSuccess(settings, _) => settings
              case JsError(errors) => throw new Exception(errors.head.toString)
            }
          case _ => throw new Exception(s"Could not retrieve feature switches from $featureSwitchProviderUrl")
        }
    )
  }

  def updateFeatureSwitches(featureSwitchProviderUrl: String,
                            featureSwitchSettings: Seq[FeatureSwitchSetting]
                           )(implicit hc: HeaderCarrier): Future[Seq[FeatureSwitchSetting]] = {
    httpClient.post(url"$featureSwitchProviderUrl").withBody(Json.toJson(featureSwitchSettings)).execute.map {
      response =>
        response.status match {
          case OK =>
            response.json.validate[Seq[FeatureSwitchSetting]] match {
              case JsSuccess(settings, _) => settings
              case JsError(errors) => throw new Exception(errors.head.toString)
            }
          case _ => throw new Exception(s"Could not retrieve feature switches from $featureSwitchProviderUrl")
        }
    }
  }

}
