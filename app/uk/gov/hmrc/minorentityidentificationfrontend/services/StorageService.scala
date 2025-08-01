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

package uk.gov.hmrc.minorentityidentificationfrontend.services

import play.api.libs.json._
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.minorentityidentificationfrontend.connectors.StorageConnector
import uk.gov.hmrc.minorentityidentificationfrontend.httpparsers.StorageHttpParser.{SuccessfullyRemoved, SuccessfullyStored}
import uk.gov.hmrc.minorentityidentificationfrontend.models.BusinessEntity.{OverseasCompany, Trusts, UnincorporatedAssociation}
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

  def storeOverseasTaxIdentifiersCountry(journeyId: String, taxIdentifiersCountry: String)(implicit hc: HeaderCarrier): Future[SuccessfullyStored.type] =
    connector.storeDataField (journeyId, OverseasCountryKey, taxIdentifiersCountry)

  def storeOverseasTaxIdentifier(journeyId: String, overseasTaxIdentifier: String)(implicit hc: HeaderCarrier): Future[SuccessfullyStored.type] =
    connector.storeDataField(journeyId, OverseasTaxIdentifierKey, overseasTaxIdentifier)

  def removeOverseasTaxIdentifier(journeyId: String)(implicit hc: HeaderCarrier): Future[SuccessfullyRemoved.type] =
    connector.removeDataField(journeyId, OverseasTaxIdentifierKey)

  def removeOverseasTaxIdentifiersCountry(journeyId: String)(implicit hc: HeaderCarrier): Future[SuccessfullyRemoved.type] =
    connector.removeDataField(journeyId, OverseasCountryKey)

  def storePostcode(journeyId: String, postcode: String)(implicit hc: HeaderCarrier): Future[SuccessfullyStored.type] =
    connector.storeDataField(journeyId, postcodeKey, postcode)

  def removePostcode(journeyId: String)(implicit hc: HeaderCarrier): Future[SuccessfullyRemoved.type] =
    connector.removeDataField(journeyId, postcodeKey)

  def retrievePostcode(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[String]] =
    connector.retrieveDataField[String](journeyId, postcodeKey)

  def retrieveCHRN(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[String]] =
    connector.retrieveDataField[String](journeyId, ChrnKey)

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

  def retrieveOverseasCompanyDetails(journeyId: String, journeyConfig: JourneyConfig)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[JsObject] =
    connector.retrieveOverseasDetails(journeyId).map {
      case Some(overseasDetails) => OverseasCompanyDetails.writesForJourneyEnd(overseasDetails, journeyConfig)
      case None => throw new InternalServerException("No Overseas Company data stored for journeyId: " + journeyId)
    }

  def retrieveOverseasAuditDetails(journeyId: String, journeyConfig: JourneyConfig)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[JsObject] =
    connector.retrieveOverseasDetails(journeyId).map {
      optOverseasDetails => OverseasCompanyDetails.writesForAudit(optOverseasDetails, journeyConfig.businessVerificationCheck)
    }

  def retrieveTrustsDetails(journeyId: String, journeyConfig: JourneyConfig)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[JsObject] =
    connector.retrieveTrustsDetails(journeyId).map {
      case Some(trustDetails) => TrustDetails.writesForJourneyEnd(trustDetails, journeyConfig.businessVerificationCheck)
      case None => throw new InternalServerException("No Trusts journey data stored for journeyId: " + journeyId)
    }

  def retrieveTrustsAuditDetails(journeyId: String, journeyConfig: JourneyConfig)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[JsObject] =
    connector.retrieveTrustsDetails(journeyId).map {
      optTrustDetails => TrustDetails.writesForAudit(optTrustDetails, journeyConfig.businessVerificationCheck)
    }

  def retrieveUADetails(journeyId: String, journeyConfig: JourneyConfig)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[JsObject] =
    connector.retrieveUADetails(journeyId).map {
      case Some(uaDetails) => UADetails.writesForJourneyEnd(uaDetails, journeyConfig.businessVerificationCheck)
      case None => throw new InternalServerException("No UA journey data stored for journeyId: " + journeyId)
    }

  def retrieveUAAuditDetails(journeyId: String, journeyConfig: JourneyConfig)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[JsObject] =
    connector.retrieveUADetails(journeyId).map {
      optUADetails => UADetails.writesForAudit(optUADetails, journeyConfig.businessVerificationCheck)
    }

  def retrieveAllData(journeyId: String, journeyConfig: JourneyConfig)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[JsObject] = {
    journeyConfig.businessEntity match {
      case OverseasCompany => retrieveOverseasCompanyDetails(journeyId, journeyConfig)
      case Trusts => retrieveTrustsDetails(journeyId, journeyConfig)
      case UnincorporatedAssociation => retrieveUADetails(journeyId, journeyConfig)
    }
  }

  def retrieveUtr(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[Utr]] =
    connector.retrieveDataField[Utr](journeyId, UtrKey)

  def retrieveOverseasTaxIdentifier(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[String]] =
    connector.retrieveDataField[String](journeyId, OverseasTaxIdentifierKey)

  def retrieveOverseasTaxIdentifiersCountry(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[String]] =
    connector.retrieveDataField[String](journeyId, OverseasCountryKey)

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
  val OverseasTaxIdentifierKey: String = "overseasTaxIdentifier"
  val OverseasCountryKey: String = "country"
  val postcodeKey: String = "postcode"
  val ChrnKey = "chrn"
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
