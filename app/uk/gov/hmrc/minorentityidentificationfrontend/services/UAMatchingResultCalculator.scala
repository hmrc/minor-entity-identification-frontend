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

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.minorentityidentificationfrontend.connectors.ValidateUnincorporatedAssociationDetailsConnector
import uk.gov.hmrc.minorentityidentificationfrontend.models.{KnownFactsMatchingResult, UnMatchable}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UAMatchingResultCalculator @Inject()(validateUnincorporatedAssociationDetailsConnector: ValidateUnincorporatedAssociationDetailsConnector,
                                           storageService: StorageService) extends MatchingResultCalculator {

  def matchKnownFacts(journeyId: String,
                      optCtUtr: Option[String],
                      optPostcode: Option[String])
                     (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[KnownFactsMatchingResult] = (optCtUtr, optPostcode) match {
    case (Some(ctUtr), Some(postcode)) => for {
      validationResult <- validateUnincorporatedAssociationDetailsConnector.validateUnincorporatedAssociationDetails(ctUtr, postcode)
      _ <- storageService.storeIdentifiersMatch(journeyId, validationResult)
    } yield validationResult
    case (Some(_), None) => Future.failed(new IllegalStateException("Error : The post code for the unincorporated association is not defined"))
    case (None, _) => storageService.storeIdentifiersMatch(journeyId, UnMatchable).map(_ => UnMatchable)
  }

}
