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

package uk.gov.hmrc.minorentityidentificationfrontend.testonly.connectors

import play.api.http.Status.CREATED
import play.api.libs.json.{JsObject, Json, Writes}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import uk.gov.hmrc.minorentityidentificationfrontend.api.controllers.JourneyController._
import uk.gov.hmrc.minorentityidentificationfrontend.api.controllers.{routes => apiRoutes}
import uk.gov.hmrc.minorentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.minorentityidentificationfrontend.models.{JourneyConfig, JourneyLabels}
import uk.gov.hmrc.minorentityidentificationfrontend.testonly.connectors.TestCreateJourneyConnector.journeyConfigWriter
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.client.HttpClientV2

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TestCreateJourneyConnector @Inject()(httpClient: HttpClientV2,
                                           appConfig: AppConfig
                                          )(implicit ec: ExecutionContext) {

  def createOverseasCompanyJourney(journeyConfig: JourneyConfig)(implicit hc: HeaderCarrier): Future[String] = {
    val url = appConfig.selfBaseUrl + apiRoutes.JourneyController.createOverseasCompanyJourney().url

    httpClient.post(url"$url").withBody(Json.toJson(journeyConfig)).execute.map {
      case response@HttpResponse(CREATED, _, _) => (response.json \ "journeyStartUrl").as[String]
    }
  }

  def createTrustsJourney(journeyConfig: JourneyConfig)(implicit hc: HeaderCarrier): Future[String] = {
    val url = appConfig.selfBaseUrl + apiRoutes.JourneyController.createTrustsJourney().url

    httpClient.post(url"$url").withBody(Json.toJson(journeyConfig)).execute.map {
      case response@HttpResponse(CREATED, _, _) => (response.json \ "journeyStartUrl").as[String]
    }
  }

  def createUnincorporatedAssociationJourney(journeyConfig: JourneyConfig)(implicit hc: HeaderCarrier): Future[String] = {
    val url = appConfig.selfBaseUrl + apiRoutes.JourneyController.createUnincorporatedAssociationJourney().url

    httpClient.post(url"$url").withBody(Json.toJson(journeyConfig)).execute.map {
      case response@HttpResponse(CREATED, _, _) => (response.json \ "journeyStartUrl").as[String]
    }
  }

}

object TestCreateJourneyConnector {
  implicit val journeyConfigWriter: Writes[JourneyConfig] = (journeyConfig: JourneyConfig) => Json.obj(
    continueUrlKey -> journeyConfig.continueUrl,
    deskProServiceIdKey -> journeyConfig.pageConfig.deskProServiceId,
    signOutUrlKey -> journeyConfig.pageConfig.signOutUrl,
    accessibilityUrlKey -> journeyConfig.pageConfig.accessibilityUrl,
    businessVerificationCheckKey -> journeyConfig.businessVerificationCheck,
    regimeKey -> journeyConfig.regime
  ) ++ labelsAsOptJsObject(journeyConfig.pageConfig.optLabels)

  private def labelsAsOptJsObject(optJourneyLabels: Option[JourneyLabels]): JsObject = {

    optJourneyLabels match {
      case Some(journeyLabels) => Json.obj(labelsKey -> Json.toJsObject(journeyLabels))
      case _ => Json.obj()
    }

  }
}
