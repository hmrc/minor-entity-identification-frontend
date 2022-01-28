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

package uk.gov.hmrc.minorentityidentificationfrontend.services

import play.api.libs.json._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.minorentityidentificationfrontend.connectors.StorageConnector
import uk.gov.hmrc.minorentityidentificationfrontend.httpparsers.StorageHttpParser.{SuccessfullyRemoved, SuccessfullyStored}
import uk.gov.hmrc.minorentityidentificationfrontend.models.BusinessVerificationStatus.{format => bvFormat}
import uk.gov.hmrc.minorentityidentificationfrontend.models.RegistrationStatus.{format => regFormat}
import uk.gov.hmrc.minorentityidentificationfrontend.models._
import uk.gov.hmrc.minorentityidentificationfrontend.services.StorageService._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StorageService @Inject()(connector: StorageConnector) {

  def storeUtr(journeyId: String, utr: Utr)(implicit hc: HeaderCarrier): Future[SuccessfullyStored.type] =
    connector.storeDataField(journeyId, UtrKey, utr)

  def removeUtr(journeyId: String)(implicit hc: HeaderCarrier): Future[SuccessfullyRemoved.type] =
    connector.removeDataField(journeyId, UtrKey)

  def storeOverseasTaxIdentifiers(journeyId: String, taxIdentifiers: Overseas)(implicit hc: HeaderCarrier): Future[SuccessfullyStored.type] =
    connector.storeDataField[Overseas](journeyId, OverseasKey, taxIdentifiers)

  def removeOverseasTaxIdentifiers(journeyId: String)(implicit hc: HeaderCarrier): Future[SuccessfullyRemoved.type] =
    connector.removeDataField(journeyId, OverseasKey)

  def retrieveAllData(journeyId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[JsObject] =
    for {
      optUtr <- retrieveUtr(journeyId)
      optOverseasTaxIdentifiers <- retrieveOverseasTaxIdentifiers(journeyId)
    } yield {

      val utrBlock: JsObject = optUtr match {
        case Some(utr) => Json.obj(utr.utrType -> utr.value)
        case None => Json.obj()
      }

      val overseasTaxIdentifiersBlock: JsObject = optOverseasTaxIdentifiers match {
        case Some(overseasTaxIdentifiers) => Json.obj(
          "overseas" -> Json.obj(
            "taxIdentifier" -> overseasTaxIdentifiers.taxIdentifier,
            "country" -> overseasTaxIdentifiers.country
          ))
        case None => Json.obj()
      }

      Json.obj(
        "identifiersMatch" -> false,
        "businessVerification" -> Json.toJson(BusinessVerificationUnchallenged)(bvFormat.writes),
        "registration" -> Json.toJson(RegistrationNotCalled)(regFormat.writes)
      ) ++
        utrBlock ++
        overseasTaxIdentifiersBlock
    }

  def retrieveUtr(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[Utr]] =
    connector.retrieveDataField[Utr](journeyId, UtrKey)

  def retrieveOverseasTaxIdentifiers(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[Overseas]] =
    connector.retrieveDataField[Overseas](journeyId, OverseasKey)

}

object StorageService {
  val UtrKey = "utr"
  val OverseasKey: String = "overseas"

  implicit val utrStorageFormat: OFormat[Utr] = new OFormat[Utr] {

    val ValueKey = "value"
    val TypeKey = "type"
    val CtutrKey = "ctutr"
    val SautrKey = "sautr"

    override def reads(json: JsValue): JsResult[Utr] = {
      for {
        utrValue <- (json \ ValueKey).validate[String]
        utrType <- (json \ TypeKey).validate[String].collect(JsonValidationError("Invalid UTR type")) {
          case SautrKey => Sautr(utrValue)
          case CtutrKey => Ctutr(utrValue)
        }
      } yield utrType
    }

    override def writes(o: Utr): JsObject =
      Json.obj(
        TypeKey -> o.utrType,
        ValueKey -> o.value
      )
  }
}
