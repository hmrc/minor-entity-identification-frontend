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

package uk.gov.hmrc.minorentityidentificationfrontend.services

import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.minorentityidentificationfrontend.connectors.MinorEntityIdentificationConnector
import uk.gov.hmrc.minorentityidentificationfrontend.models._
import uk.gov.hmrc.minorentityidentificationfrontend.services.MinorEntityIdentificationService.UtrKey

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StorageService @Inject()(connector: MinorEntityIdentificationConnector
                              )(implicit ec: ExecutionContext) {

  def storeUtr(journeyId: String, utr: Utr)(implicit hc: HeaderCarrier): Future[StorageResult] =
    connector.storeDataField(journeyId, UtrKey, utr)

  def retrieveUtr(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[Utr]] =
    connector.retrieveDataField[Utr](journeyId, UtrKey)
}

object MinorEntityIdentificationService {
  val UtrKey = "utr"
}
