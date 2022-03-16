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

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.minorentityidentificationfrontend.models.{JourneyConfig, SuccessfulMatch, UnMatchable}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UnincorporatedAssociationSubmissionService @Inject()(auditService: AuditService,
                                                           storageService: StorageService,
                                                           validateUnincorporatedAssociationDetailsService: ValidateUnincorporatedAssociationDetailsService) {

  def submit(journeyId: String, journeyConfig: JourneyConfig, retryUrl: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String] = {
    for {
      optCtUtr <-  storageService.retrieveUtr(journeyId)
      optOfficePostcode <- storageService.retrievePostcode(journeyId)
      matchingResult <- validateUnincorporatedAssociationDetailsService.validateUnincorporatedAssociationDetails(
        journeyId,
        optCtUtr.map(_.value),
        optOfficePostcode
      )
    } yield {
      auditService.auditJourney(journeyId, journeyConfig)
      matchingResult match {
        case SuccessfulMatch | UnMatchable => journeyConfig.fullContinueUrl(journeyId)
        case _ => retryUrl
      }
    }
  }

}
