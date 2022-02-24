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

import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.minorentityidentificationfrontend.connectors.CreateBusinessVerificationJourneyConnector.{BusinessVerificationJourneyCreated, BusinessVerificationJourneyCreationResponse, NotEnoughEvidence, UserLockedOut}
import uk.gov.hmrc.minorentityidentificationfrontend.connectors.{CreateBusinessVerificationJourneyConnector, RetrieveBusinessVerificationStatusConnector}
import uk.gov.hmrc.minorentityidentificationfrontend.models._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessVerificationService @Inject()(createBusinessVerificationJourneyConnector: CreateBusinessVerificationJourneyConnector,
                                            retrieveBusinessVerificationResultConnector: RetrieveBusinessVerificationStatusConnector,
                                            storageService: StorageService
                                           )(implicit val executionContext: ExecutionContext) {

  def createBusinessVerificationJourney(journeyId: String,
                                        sautr: String,
                                        accessibilityUrl: String,
                                        regime: String
                                       )(implicit hc: HeaderCarrier): Future[BusinessVerificationJourneyCreationResponse] =
    createBusinessVerificationJourneyConnector.createBusinessVerificationJourney(journeyId, sautr, accessibilityUrl, regime).flatMap {
      case success@Right(BusinessVerificationJourneyCreated(_)) =>
        Future.successful(success)
      case Left(NotEnoughEvidence) =>
        storageService.storeBusinessVerificationStatus(journeyId, BusinessVerificationNotEnoughInformationToChallenge).map {
          _ => Left(NotEnoughEvidence)
        }
      case Left(UserLockedOut) =>
        storageService.storeBusinessVerificationStatus(journeyId, BusinessVerificationFail).map {
          _ => Left(UserLockedOut)
        }
      case _ =>
        throw new InternalServerException(s"createBusinessVerificationJourney service failed with invalid BV status")
    }

  def retrieveBusinessVerificationStatus(businessVerificationJourneyId: String)(implicit hc: HeaderCarrier): Future[BusinessVerificationStatus] =
    retrieveBusinessVerificationResultConnector.retrieveBusinessVerificationStatus(businessVerificationJourneyId)

}
