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

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.minorentityidentificationfrontend.connectors.RegistrationConnector
import uk.gov.hmrc.minorentityidentificationfrontend.models._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegistrationOrchestrationService @Inject()(storageService: StorageService,
                                                 registrationConnector: RegistrationConnector,
                                                 auditService: AuditService) {

  def register(journeyId: String,
               optSautr: Option[String],
               journeyConfig: JourneyConfig
              )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[RegistrationStatus] = for {
    registrationStatus <- storageService.retrieveBusinessVerificationStatus(journeyId).flatMap {
      case Some(BusinessVerificationPass) => registrationConnector.register(optSautr.get, journeyConfig.regime)
      case None if !journeyConfig.businessVerificationCheck && optSautr.isDefined =>
        registrationConnector.register(optSautr.get, journeyConfig.regime)
      case _ => Future.successful(RegistrationNotCalled)
    }
    _ <- storageService.storeRegistrationStatus(journeyId, registrationStatus)
    _ <- auditService.auditJourney(journeyId, journeyConfig)
  } yield registrationStatus

}
