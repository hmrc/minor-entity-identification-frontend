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

import play.api.http.Status.CREATED
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, InternalServerException, StringContextOps}
import uk.gov.hmrc.minorentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.minorentityidentificationfrontend.connectors.CreateJourneyHttpParser.CreateJourneyHttpReads

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateJourneyConnector @Inject()(httpClient: HttpClientV2,
                                       appConfig: AppConfig
                                      )(implicit ec: ExecutionContext) {

  def createJourney()(implicit hc: HeaderCarrier): Future[String] = {
    httpClient.post(url"${appConfig.createJourneyUrl}").execute[String](CreateJourneyHttpReads, ec)
  }

}

object CreateJourneyHttpParser {

  implicit object CreateJourneyHttpReads extends HttpReads[String] {
    override def read(method: String, url: String, response: HttpResponse): String = {
      response.status match {
        case CREATED =>
          (response.json \ "journeyId").as[String]
        case _ =>
          throw new InternalServerException("Invalid response returned from create journey API")
      }
    }
  }

}

