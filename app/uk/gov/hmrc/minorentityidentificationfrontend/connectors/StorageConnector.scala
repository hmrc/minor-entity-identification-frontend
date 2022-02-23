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

import play.api.libs.json.{JsObject, Reads, Writes}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReadsInstances}
import uk.gov.hmrc.minorentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.minorentityidentificationfrontend.httpparsers.StorageHttpParser.{SuccessfullyRemoved, SuccessfullyStored}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StorageConnector @Inject()(http: HttpClient,
                                 appConfig: AppConfig
                                )(implicit ec: ExecutionContext) extends HttpReadsInstances {

  def retrieveDataField[DataType](journeyId: String,
                                  dataKey: String
                                 )(implicit dataTypeReads: Reads[DataType],
                                   manifest: Manifest[DataType],
                                   hc: HeaderCarrier): Future[Option[DataType]] =
    http.GET[Option[DataType]](s"${appConfig.minorEntityIdentificationUrl(journeyId)}/$dataKey")

  def retrieveAllDataFields(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[JsObject]] =
    http.GET[Option[JsObject]](s"${appConfig.minorEntityIdentificationUrl(journeyId)}")

  def storeDataField[DataType](journeyId: String,
                               dataKey: String,
                               data: DataType
                              )(implicit dataTypeWriter: Writes[DataType],
                                hc: HeaderCarrier): Future[SuccessfullyStored.type] =
    http.PUT[DataType, SuccessfullyStored.type](s"${appConfig.minorEntityIdentificationUrl(journeyId)}/$dataKey", data)

  def removeDataField(journeyId: String, dataKey: String)(implicit hc: HeaderCarrier): Future[SuccessfullyRemoved.type] =
    http.DELETE[SuccessfullyRemoved.type](s"${appConfig.minorEntityIdentificationUrl(journeyId)}/$dataKey")

  def removeAllData(journeyId: String)(implicit hc: HeaderCarrier): Future[SuccessfullyRemoved.type] =
    http.DELETE[SuccessfullyRemoved.type](appConfig.minorEntityIdentificationUrl(journeyId))

}