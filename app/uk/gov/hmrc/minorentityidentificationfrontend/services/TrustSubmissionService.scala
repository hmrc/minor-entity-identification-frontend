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
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.trustControllers.errorControllers
import uk.gov.hmrc.minorentityidentificationfrontend.models._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TrustSubmissionService @Inject()(validateTrustKnownFactsService: ValidateTrustKnownFactsService,
                                       storageService: StorageService,
                                       auditService: AuditService,
                                       businessVerificationService: BusinessVerificationService) {

  def submit(journeyId: String, journeyConfig: JourneyConfig)
            (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String] =
    for {
      optSaUtr <- storageService.retrieveUtr(journeyId)
      optSaPostcode <- storageService.retrieveSaPostcode(journeyId)
      optCHRN <- storageService.retrieveCHRN(journeyId)
      matchingResult <- validateTrustKnownFactsService.validateTrustKnownFacts(journeyId, optSaUtr.map(_.value), optSaPostcode, optCHRN)
      nextJourneyUrl <- handleBusinessVerificationCheck(journeyId, matchingResult, optSaUtr.map(_.value), journeyConfig)
      _ <- auditService.auditJourney(journeyId, journeyConfig)
    } yield nextJourneyUrl

  private def handleBusinessVerificationCheck(journeyId: String,
                                              matchingResult: KnownFactsMatchingResult,
                                              optSaUtr: Option[String],
                                              journeyConfig: JourneyConfig)
                                             (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String] = matchingResult match {
    case SuccessfulMatch                          =>
      if (journeyConfig.businessVerificationCheck)
        businessVerificationService
          .createBusinessVerificationJourney(journeyId, optSaUtr.getOrElse(throwASaUtrNotDefinedException), journeyConfig.pageConfig.accessibilityUrl, journeyConfig.regime)
          .flatMap({
            case Some(businessVerificationUrl) => Future.successful(businessVerificationUrl)
            case None                          => Future.successful(journeyConfig.fullContinueUrl(journeyId))
          })
      else
        Future.successful(journeyConfig.fullContinueUrl(journeyId))
    case aMatchingFailure: KnownFactsMatchFailure =>
      for {
        _ <- if (journeyConfig.businessVerificationCheck)
          storageService.storeBusinessVerificationStatus(journeyId, BusinessVerificationNotEnoughInformationToCallBV)
        else
          Future.successful(())
        journeyNextUrl <- Future.successful(calculateJourneyNextUrl(aMatchingFailure, journeyConfig, journeyId))
      } yield journeyNextUrl
  }

  private def calculateJourneyNextUrl(aMatchingFailure: KnownFactsMatchFailure, journeyConfig: JourneyConfig, journeyId: String): String = aMatchingFailure match {
    case UnMatchableWithoutRetry => journeyConfig.fullContinueUrl(journeyId)
    case DetailsNotFound |
         DetailsMismatch |
         UnMatchableWithRetry    => errorControllers.routes.CannotConfirmBusinessController.show(journeyId).url
  }

  private def throwASaUtrNotDefinedException: Nothing =
    throw new IllegalStateException("Error: SA UTR is not defined")

}
