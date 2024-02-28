/*
 * Copyright 2024 HM Revenue & Customs
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

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.minorentityidentificationfrontend.connectors.RegistrationConnector
import uk.gov.hmrc.minorentityidentificationfrontend.models.BusinessEntity.{OverseasCompany, Trusts, UnincorporatedAssociation}
import uk.gov.hmrc.minorentityidentificationfrontend.models._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegistrationOrchestrationService @Inject()(storageService: StorageService,
                                                 registrationConnector: RegistrationConnector,
                                                 auditService: AuditService) {

  private def internalRegister(utr: String, journeyConfig: JourneyConfig)
                              (implicit hc: HeaderCarrier): Future[RegistrationStatus] = {
    journeyConfig.businessEntity match {
      case Trusts => registrationConnector.registerTrust(utr, journeyConfig.regime)
      case UnincorporatedAssociation => registrationConnector.registerUA(utr, journeyConfig.regime)
      case OverseasCompany => throw new IllegalArgumentException("Overseas Company is not supported for registration.")
    }
  }

  def register(journeyId: String,
               optUtr: Option[String],
               journeyConfig: JourneyConfig
              )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[RegistrationStatus] = {
    journeyConfig.businessEntity match {
      case Trusts | UnincorporatedAssociation =>
        for {
          registrationStatus <- storageService.retrieveBusinessVerificationStatus(journeyId).flatMap {
            case Some(BusinessVerificationPass) => internalRegister(optUtr.get, journeyConfig)
            case None if !journeyConfig.businessVerificationCheck && optUtr.isDefined => internalRegister(optUtr.get, journeyConfig)
            case _ => Future.successful(RegistrationNotCalled)
          }
          _ <- storageService.storeRegistrationStatus(journeyId, registrationStatus)
          _ <- auditService.auditJourney(journeyId, journeyConfig)
        } yield registrationStatus
      case OverseasCompany => throw new IllegalArgumentException("Overseas Company is not supported for registration.")
    }
  }
}
