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

import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.minorentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.minorentityidentificationfrontend.models.{BusinessVerificationFail, BusinessVerificationNotEnoughInformationToCallBV, BusinessVerificationNotEnoughInformationToChallenge, BusinessVerificationPass, BusinessVerificationStatus, Ctutr, DetailsMismatch, DetailsNotFound, JourneyConfig, KnownFactsMatchingResult, Registered, RegistrationFailed, RegistrationStatus, Sautr, SuccessfulMatch, UnMatchableWithRetry, UnMatchableWithoutRetry}
import uk.gov.hmrc.minorentityidentificationfrontend.models.BusinessEntity._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuditService @Inject()(appConfig: AppConfig,
                             auditConnector: AuditConnector,
                             journeyService: JourneyService,
                             storageService: StorageService) {

  def auditJourney(journeyId: String, authInternalId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] =
    journeyService.getJourneyConfig(journeyId, authInternalId).flatMap {
      journeyConfig =>auditByBusinessType(journeyId, journeyConfig)
    }

  def auditJourney(journeyId: String, journeyConfig: JourneyConfig)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {
    auditByBusinessType(journeyId, journeyConfig)
  }

  private def auditByBusinessType(journeyId: String, journeyConfig: JourneyConfig)
                                 (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {

    val callingService: String = journeyConfig.pageConfig.optServiceName.getOrElse(appConfig.defaultServiceName)

    journeyConfig.businessEntity match {
      case OverseasCompany => auditOverseasCompanyJourney(journeyId, callingService)
      case Trusts => auditTrustsJourney(journeyId, callingService)
      case UnincorporatedAssociation => auditUnincorporatedAssociationJourney(journeyId, callingService)
      case _ =>
        throw new InternalServerException(s"Unexpected business entity type encountered auditing minor entity journey for Journey ID $journeyId")
    }

  }

  private def auditOverseasCompanyJourney(journeyId: String, callingService: String)
                                         (implicit headerCarrier: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {

    val retrieveUtr = storageService.retrieveUtr(journeyId)
    val retrieveOverseasTaxIdentifiers = storageService.retrieveOverseasTaxIdentifiers(journeyId)

    for {
      optUtr <- retrieveUtr
      optOverseasTaxIdentifiers <- retrieveOverseasTaxIdentifiers
    } yield {
      val optUtrBlock = optUtr match {
        case Some(utr: Ctutr) => Json.obj("userCTUTR" -> utr.value, "cTUTRMatch" -> false)
        case Some(utr: Sautr) => Json.obj("userSAUTR" -> utr.value, "sautrMatch" -> false)
        case None => Json.obj()
      }
      val overseasIdentifiersBlock =
        optOverseasTaxIdentifiers match {
          case Some(overseas) => Json.obj(
            "overseasTaxIdentifier" -> overseas.taxIdentifier,
            "overseasTaxIdentifierCountry" -> overseas.country)
          case _ => Json.obj()
        }

      val auditJson = Json.obj(
        "callingService" -> callingService,
        "businessType" -> "Overseas Company",
        "VerificationStatus" -> Json.obj("verificationStatus" -> "UNCHALLENGED"),
        "RegisterApiStatus" -> "not called"
      ) ++ optUtrBlock ++ overseasIdentifiersBlock

      auditConnector.sendExplicitAudit(
        auditType = "OverseasCompanyRegistration",
        detail = auditJson
      )
    }
  }

  private def auditTrustsJourney(journeyId: String, callingService: String)
                                (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {

    val retrieveSaUtr = storageService.retrieveUtr(journeyId)
    val retrieveSaPostCode = storageService.retrieveSaPostcode(journeyId)
    val retrieveCHRN = storageService.retrieveCHRN(journeyId)
    val retrieveIdentifiersMatch = storageService.retrieveIdentifiersMatch(journeyId)
    val retrieveBusinessVerificationStatus = storageService.retrieveBusinessVerificationStatus(journeyId)
    val retrieveRegistrationStatus = storageService.retrieveRegistrationStatus(journeyId)

    for {
      optSaUtr <- retrieveSaUtr
      optSaPostCode <- retrieveSaPostCode
      optCHRN <- retrieveCHRN
      optIdentifiersMatch <- retrieveIdentifiersMatch
      optBusinessVerificationStatus <- retrieveBusinessVerificationStatus
      optRegistrationStatus <- retrieveRegistrationStatus
    } yield {

      val optSaUtrBlock = optSaUtr match {
        case Some(saUtr) => Json.obj("SAUTR" -> saUtr.value)
        case None => Json.obj()
      }

      val optSaPostCodeBlock = optSaPostCode match {
        case Some(saPostCode) => Json.obj("SApostcode" -> saPostCode)
        case None => Json.obj()
      }

      val optCHRNBlock = optCHRN match {
        case Some(chrn) => Json.obj("CHRN" -> chrn)
        case None => Json.obj()
      }

      val auditJson = Json.obj(
        "callingService" -> callingService,
        "businessType" -> "Trusts",
        "isMatch" -> defineAuditIdentifiersMatch(optIdentifiersMatch),
        "VerificationStatus" -> defineAuditBusinessVerificationStatus(optBusinessVerificationStatus),
        "RegisterApiStatus" -> defineAuditRegistrationStatus(optRegistrationStatus)
      ) ++ optSaUtrBlock ++ optSaPostCodeBlock ++ optCHRNBlock

      auditConnector.sendExplicitAudit(
        auditType = "TrustsRegistration",
        detail = auditJson
      )
    }
  }

  private def auditUnincorporatedAssociationJourney(journeyId: String, callingService: String)
                               (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {

    for {
      optRegistrationStatus <- storageService.retrieveRegistrationStatus(journeyId)
    } yield {
      val auditJson = Json.obj(
        "callingService" -> callingService,
        "businessType" -> "Unincorporated Association",
        "identifiersMatch" -> false,
        "VerificationStatus" -> Json.obj("verificationStatus" -> "UNCHALLENGED"),
        "RegisterApiStatus" -> defineAuditRegistrationStatus(optRegistrationStatus)
      )

      auditConnector.sendExplicitAudit(
        auditType = "UnincorporatedAssociationRegistration",
        detail = auditJson
      )
    }
  }

  private def defineAuditIdentifiersMatch(optIdentifiersMatch: Option[KnownFactsMatchingResult]): String =
    optIdentifiersMatch match {
      case Some(SuccessfulMatch) => "true"
      case Some(DetailsMismatch) | Some(DetailsNotFound) => "false"
      case Some(UnMatchableWithRetry) | Some(UnMatchableWithoutRetry) => "unmatchable"
      case None => "false"
    }

  private def defineAuditRegistrationStatus(optRegistrationStatus: Option[RegistrationStatus]): String =
    optRegistrationStatus match {
      case Some(Registered(_)) => "success"
      case Some(RegistrationFailed) => "fail"
      case _ => "not called"
    }

  private def defineAuditBusinessVerificationStatus(optBusinessVerificationStatus: Option[BusinessVerificationStatus]): String =
    optBusinessVerificationStatus match {
      case Some(BusinessVerificationPass) => "success"
      case Some(BusinessVerificationFail) => "fail"
      case Some(BusinessVerificationNotEnoughInformationToCallBV) => "Not enough information to call BV"
      case Some(BusinessVerificationNotEnoughInformationToChallenge) => "Not Enough Information to challenge"
      case None => "not requested"
    }
}