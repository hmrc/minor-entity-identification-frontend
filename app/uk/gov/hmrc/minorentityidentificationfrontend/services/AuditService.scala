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
import uk.gov.hmrc.minorentityidentificationfrontend.models._
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
      journeyConfig => auditByBusinessType(journeyId, journeyConfig)
    }

  def auditJourney(journeyId: String, journeyConfig: JourneyConfig)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {
    auditByBusinessType(journeyId, journeyConfig)
  }

  private def auditByBusinessType(journeyId: String, journeyConfig: JourneyConfig)
                                 (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {

    val callingService: String = journeyConfig.pageConfig.optServiceName.getOrElse(appConfig.defaultServiceName)

    journeyConfig.businessEntity match {
      case OverseasCompany => auditOverseasCompanyJourney(journeyId, callingService)
      case Trusts => auditTrustsJourney(journeyId, callingService, journeyConfig)
      case UnincorporatedAssociation => auditUnincorporatedAssociationJourney(journeyId, callingService, journeyConfig)
      case _ =>
        throw new InternalServerException(s"Unexpected business entity type encountered auditing minor entity journey for Journey ID $journeyId")
    }
  }

  private def auditOverseasCompanyJourney(journeyId: String, callingService: String)
                                         (implicit headerCarrier: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {
    for {
      overseasDataJson <- storageService.retrieveOverseasAuditDetails(journeyId)
    } yield {
      val auditJson = Json.obj(
        "callingService" -> callingService,
        "businessType" -> "Overseas Company") ++ overseasDataJson

      auditConnector.sendExplicitAudit(
        auditType = "OverseasCompanyRegistration",
        detail = auditJson
      )
    }
  }

  private def auditTrustsJourney(journeyId: String, callingService: String, journeyConfig: JourneyConfig)
                                (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {
    for {
      trustDetails <- storageService.retrieveTrustsAuditDetails(journeyId, journeyConfig)
    } yield {
      val auditJson = Json.obj(
        "callingService" -> callingService,
        "businessType" -> "Trusts"
      ) ++ trustDetails

      auditConnector.sendExplicitAudit(
        auditType = "TrustsRegistration",
        detail = auditJson
      )
    }
  }

  private def auditUnincorporatedAssociationJourney(journeyId: String, callingService: String, journeyConfig: JourneyConfig)
                                                   (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {
    for {
      uaDetails <- storageService.retrieveUAAuditDetails(journeyId, journeyConfig)
    } yield {
      val auditJson = Json.obj(
        "callingService" -> callingService,
        "businessType" -> "Unincorporated Association"
      ) ++ uaDetails

      auditConnector.sendExplicitAudit(
        auditType = "UnincorporatedAssociationRegistration",
        detail = auditJson
      )
    }
  }
}
