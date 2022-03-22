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
import uk.gov.hmrc.minorentityidentificationfrontend.models._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubmissionService @Inject()(storageService: StorageService,
                                  auditService: AuditService,
                                  businessVerificationService: BusinessVerificationService,
                                  registrationOrchestrationService: RegistrationOrchestrationService) {

  def submit(journeyId: String,
             journeyConfig: JourneyConfig,
             matchingResultCalculator: MatchingResultCalculator,
             cannotConfirmErrorPageUrl: String
            )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String] =
    for {
      optUtrString <- storageService.retrieveUtr(journeyId).map(optUtr => optUtr.map(_.value))
      optPostcode <- storageService.retrievePostcode(journeyId)
      matchingResult <- matchingResultCalculator.matchKnownFacts(journeyId, optUtrString, optPostcode)
      (redirectUrl, isJourneyCompleted) <- handleBusinessVerificationCheck(journeyId,
        matchingResult,
        optUtrString,
        journeyConfig,
        cannotConfirmErrorPageUrl
      )
      _ <- if (isJourneyCompleted == JourneyCompleted) {
        storageService
          .storeRegistrationStatus(journeyId, RegistrationNotCalled)
          .map(_ => auditService.auditJourney(journeyId, journeyConfig))
      } else Future.successful(())
    } yield redirectUrl

  private def handleBusinessVerificationCheck(journeyId: String,
                                              matchingResult: KnownFactsMatchingResult,
                                              optUtr: Option[String],
                                              journeyConfig: JourneyConfig,
                                              cannotConfirmErrorPageUrl: String)
                                             (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[(String, JourneyState)] = matchingResult match {
    case SuccessfulMatch =>
      if (journeyConfig.businessVerificationCheck)
        businessVerificationService
          .createBusinessVerificationJourney(journeyId, optUtr.getOrElse(throwASaUtrNotDefinedException), journeyConfig)
          .map({
            case Some(businessVerificationUrl) => (businessVerificationUrl, JourneyNotCompletedYet)
            case None => (journeyConfig.fullContinueUrl(journeyId), JourneyCompleted)
          })
      else
        registrationOrchestrationService
          .register(journeyId, optUtr, journeyConfig)
          .map(_ => (journeyConfig.fullContinueUrl(journeyId), JourneyNotCompletedYet))
    case aMatchingFailure: KnownFactsMatchFailure =>
      for {
        _ <- if (journeyConfig.businessVerificationCheck)
          storageService.storeBusinessVerificationStatus(journeyId, BusinessVerificationNotEnoughInformationToCallBV)
        else
          Future.successful(())
      } yield aMatchingFailure match {
        case UnMatchable =>
          (journeyConfig.fullContinueUrl(journeyId), JourneyCompleted)
        case DetailsNotFound | DetailsMismatch =>
          (cannotConfirmErrorPageUrl, JourneyCompleted)
      }
  }

  private def throwASaUtrNotDefinedException: Nothing =
    throw new IllegalStateException("Error: SA UTR is not defined")

  sealed trait JourneyState

  case object JourneyCompleted extends JourneyState

  case object JourneyNotCompletedYet extends JourneyState
}
