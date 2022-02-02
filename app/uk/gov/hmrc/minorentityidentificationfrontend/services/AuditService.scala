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
import uk.gov.hmrc.minorentityidentificationfrontend.models.BusinessEntity._
import uk.gov.hmrc.minorentityidentificationfrontend.models.{Ctutr, Registered, RegistrationFailed, Sautr}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuditService @Inject()(appConfig: AppConfig,
                             auditConnector: AuditConnector,
                             journeyService: JourneyService,
                             storageService: StorageService) {

  def auditJourney(journeyId: String, authInternalId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {
    for {
      journeyConfig <- journeyService.getJourneyConfig(journeyId, authInternalId)
      optUtr <- storageService.retrieveUtr(journeyId)
      optOverseasTaxIdentifiers <- storageService.retrieveOverseasTaxIdentifiers(journeyId)
      optRegistrationStatus <- storageService.retrieveRegistrationStatus(journeyId)
    } yield {
      val registrationStatusBlock =
        optRegistrationStatus match {
          case Some(Registered(_)) => "success"
          case Some(RegistrationFailed) => "fail"
          case _ => "not called"
        }
      val callingService: String = journeyConfig.pageConfig.optServiceName.getOrElse(appConfig.defaultServiceName)
      journeyConfig.businessEntity match {
        case OverseasCompany =>
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
          //TODO Hardcoding the Verification status for now.  Will be updated in a future story
          val auditJson = Json.obj(
            "callingService" -> callingService,
            "businessType" -> "Overseas Company",
            "VerificationStatus" -> Json.obj("verificationStatus" -> "UNCHALLENGED"),
            "RegisterApiStatus" -> registrationStatusBlock
          ) ++ optUtrBlock ++ overseasIdentifiersBlock

          auditConnector.sendExplicitAudit(
            auditType = "OverseasCompanyRegistration",
            detail = auditJson
          )
        case UnincorporatedAssociation =>
          //TODO Hardcoding the Verification status for now.  Will be updated in a future story
          val auditJson = Json.obj(
            "callingService" -> callingService,
            "businessType" -> "Unincorporated Association",
            "identifiersMatch" -> false,
            "VerificationStatus" -> Json.obj("verificationStatus" -> "UNCHALLENGED"),
            "RegisterApiStatus" -> registrationStatusBlock
          )

          auditConnector.sendExplicitAudit(
            auditType = "UnincorporatedAssociationRegistration",
            detail = auditJson
          )
        case Trusts =>
          //TODO Hardcoding the Verification status for now.  Will be updated in a future story
          val auditJson = Json.obj(
            "callingService" -> callingService,
            "businessType" -> "Trusts",
            "identifiersMatch" -> false,
            "VerificationStatus" -> Json.obj("verificationStatus" -> "UNCHALLENGED"),
            "RegisterApiStatus" -> registrationStatusBlock
          )

          auditConnector.sendExplicitAudit(
            auditType = "TrustsRegistration",
            detail = auditJson
          )
        case _ =>
          throw new InternalServerException(s"Not enough information to audit minor entity journey for Journey ID $journeyId")
      }
    }
  }
}