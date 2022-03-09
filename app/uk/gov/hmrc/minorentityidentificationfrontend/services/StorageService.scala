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

  def storeSaPostcode(journeyId: String, saPostcode: String)(implicit hc: HeaderCarrier): Future[SuccessfullyStored.type] =
    connector.storeDataField(journeyId, SaPostcodeKey, saPostcode)

  def removeSaPostcode(journeyId: String)(implicit hc: HeaderCarrier): Future[SuccessfullyRemoved.type] =
    connector.removeDataField(journeyId, SaPostcodeKey)

  def retrieveSaPostcode(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[String]] =
    connector.retrieveDataField[String](journeyId, SaPostcodeKey)

  def storeOfficePostcode(journeyId: String, officePostcode: String)(implicit hc: HeaderCarrier): Future[SuccessfullyStored.type] =
    connector.storeDataField(journeyId, OfficePostcodeKey, officePostcode)

  def removeOfficePostcode(journeyId: String)(implicit hc: HeaderCarrier): Future[SuccessfullyRemoved.type] =
    connector.removeDataField(journeyId, OfficePostcodeKey)

  def retrieveOfficePostcode(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[String]] =
    connector.retrieveDataField[String](journeyId, OfficePostcodeKey)

  def retrieveCHRN(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[String]] =
    connector.retrieveDataField[String](journeyId, ChrnKey)

  def retrieveIdentifiersMatch(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[KnownFactsMatchingResult]] =
    connector.retrieveDataField[KnownFactsMatchingResult](journeyId, IdentifiersMatchKey)

  def storeRegistrationStatus(journeyId: String, registrationStatus: RegistrationStatus)(implicit hc: HeaderCarrier): Future[SuccessfullyStored.type] =
    connector.storeDataField[RegistrationStatus](journeyId, RegistrationKey, registrationStatus)

  def storeCHRN(journeyId: String, chrn: String)(implicit hc: HeaderCarrier): Future[SuccessfullyStored.type] =
    connector.storeDataField[String](journeyId, ChrnKey, chrn)

  def removeCHRN(journeyId: String)(implicit hc: HeaderCarrier): Future[SuccessfullyRemoved.type] =
    connector.removeDataField(journeyId, ChrnKey)

  def storeIdentifiersMatch(journeyId: String, identifiersMatch: KnownFactsMatchingResult)(implicit hc: HeaderCarrier): Future[SuccessfullyStored.type] =
    connector.storeDataField[KnownFactsMatchingResult](journeyId, IdentifiersMatchKey, identifiersMatch)

  def removeAllData(journeyId: String)(implicit hc: HeaderCarrier): Future[SuccessfullyRemoved.type] =
    connector.removeAllData(journeyId)

  def retrieveAllData(journeyId: String, journeyConfig: JourneyConfig)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[JsObject] =
    for {
      optUtr <- retrieveUtr(journeyId)
      optOverseasTaxIdentifiers <- retrieveOverseasTaxIdentifiers(journeyId)
      optSaPostcode <- retrieveSaPostcode(journeyId)
      optCharityHMRCReferenceNumber <- retrieveCHRN(journeyId)
      optIdentifiersMatch <- retrieveIdentifiersMatch(journeyId)
      optOfficePostcode <- retrieveOfficePostcode(journeyId)
      optBVStatus <- retrieveBusinessVerificationStatus(journeyId)
      optRegistrationStatus <- retrieveRegistrationStatus(journeyId)
    } yield {

      val optCharityHMRCReferenceNumberBlock: JsObject = optCharityHMRCReferenceNumber match {
        case Some(charityHMRCReferenceNumber) => Json.obj("chrn" -> charityHMRCReferenceNumber)
        case None => Json.obj()
      }

      val utrSaPostcodeBlock: JsObject = optSaPostcode match {
        case Some(saPostcode) => Json.obj("saPostcode" -> saPostcode)
        case None => Json.obj()
      }

      val registeredOfficePostcodeBlock: JsObject = optOfficePostcode match {
        case Some(officePostcode) => Json.obj("ctPostcode" -> officePostcode)
        case None => Json.obj()
      }

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

      val identifiersMatchBlock: JsObject =
        Json.obj("identifiersMatch" -> optIdentifiersMatch.contains(SuccessfulMatch))

      val businessVerificationStatusBlock: JsObject =
        if (journeyConfig.businessVerificationCheck) {
          val value = optBVStatus match {
            case None => BusinessVerificationStatus.writeForJourneyContinuation(BusinessVerificationNotEnoughInformationToChallenge)
            case Some(bvStatus) => BusinessVerificationStatus.writeForJourneyContinuation(bvStatus)
          }
          Json.obj("businessVerification" -> value)
        }
        else
          JsObject.empty

      val registrationValue: JsValue = {
        optRegistrationStatus match {
          case Some(regStatus) => Json.toJson(regStatus)(regFormat.writes)
          case _ => Json.toJson(RegistrationNotCalled)(regFormat.writes)
        }
      }

      Json.obj("registration" -> registrationValue) ++
        utrBlock ++
        overseasTaxIdentifiersBlock ++
        utrSaPostcodeBlock ++
        optCharityHMRCReferenceNumberBlock ++
        identifiersMatchBlock ++
        registeredOfficePostcodeBlock ++
        businessVerificationStatusBlock
    }

  def retrieveUtr(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[Utr]] =
    connector.retrieveDataField[Utr](journeyId, UtrKey)

  def retrieveOverseasTaxIdentifiers(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[Overseas]] =
    connector.retrieveDataField[Overseas](journeyId, OverseasKey)

  def retrieveRegistrationStatus(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[RegistrationStatus]] =
    connector.retrieveDataField[RegistrationStatus](journeyId, RegistrationKey)

  def storeBusinessVerificationStatus(journeyId: String,
                                      businessVerification: BusinessVerificationStatus
                                     )(implicit hc: HeaderCarrier): Future[SuccessfullyStored.type] =
    connector.storeDataField[BusinessVerificationStatus](journeyId, VerificationStatusKey, businessVerification)

  def retrieveBusinessVerificationStatus(journeyId: String
                                        )(implicit hc: HeaderCarrier): Future[Option[BusinessVerificationStatus]] =
    connector.retrieveDataField[BusinessVerificationStatus](journeyId, VerificationStatusKey)
}

object StorageService {
  val UtrKey = "utr"
  val OverseasKey: String = "overseas"
  val SaPostcodeKey: String = "saPostcode"
  val ChrnKey = "chrn"
  val OfficePostcodeKey: String = "officePostcode"
  val RegistrationKey: String = "registration"
  val IdentifiersMatchKey: String = "identifiersMatch"
  val VerificationStatusKey = "businessVerification"

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
