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

package uk.gov.hmrc.minorentityidentificationfrontend.testonly.service

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.minorentityidentificationfrontend.connectors.StorageConnector
import uk.gov.hmrc.minorentityidentificationfrontend.httpparsers.StorageHttpParser.SuccessfullyStored
import uk.gov.hmrc.minorentityidentificationfrontend.models.Utr
import uk.gov.hmrc.minorentityidentificationfrontend.services.StorageService.UtrKey
import uk.gov.hmrc.minorentityidentificationfrontend.testonly.models.Stubs

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class TestStorageService @Inject()(connector: StorageConnector) {

  private val stubKey = "stubs"

  def storeStubs(journeyId: String, stubs: Stubs)(implicit hc: HeaderCarrier): Future[SuccessfullyStored.type] =
    connector.storeDataField(journeyId, stubKey, stubs)

  def retrieveStubs(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[Stubs]] =
    connector.retrieveDataField[Stubs](journeyId, stubKey)

}
