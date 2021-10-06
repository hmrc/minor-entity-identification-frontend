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

package uk.gov.hmrc.minorentityidentificationfrontend.connectors

import play.api.libs.json.{Reads, Writes}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReadsInstances}
import uk.gov.hmrc.minorentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.minorentityidentificationfrontend.httpParsers.MinorEntityIdentificationStorageHttpParser.SoleTraderIdentificationStorageHttpReads
import uk.gov.hmrc.minorentityidentificationfrontend.models.{StorageResult, Utr}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MinorEntityIdentificationConnector @Inject()(http: HttpClient,
                                                   appConfig: AppConfig
                                                  )(implicit ec: ExecutionContext) extends HttpReadsInstances {

  def retrieveDataField[DataType](journeyId: String,
                                  dataKey: String
                                 )(implicit dataTypeReads: Reads[DataType],
                                   manifest: Manifest[DataType],
                                   hc: HeaderCarrier): Future[Option[DataType]] =
    http.GET[Option[DataType]](s"${appConfig.minorEntityIdentificationUrl(journeyId)}/$dataKey")

  def storeDataField[DataType](journeyId: String,
                               dataKey: String,
                               data: DataType
                              )(implicit dataTypeWriter: Writes[DataType],
                                hc: HeaderCarrier): Future[StorageResult] =
    http.PUT[DataType, StorageResult](s"${appConfig.minorEntityIdentificationUrl(journeyId)}/$dataKey", data)

}