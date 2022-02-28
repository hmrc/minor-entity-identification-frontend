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
class SubmissionService @Inject()(validateTrustKnownFactsService: ValidateTrustKnownFactsService,
                                  storageService: StorageService) {

  def submit(journeyId: String, journeyConfig: JourneyConfig)
            (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String] =
    for {
      optSaUtr <- storageService.retrieveUtr(journeyId)
      optSaPostcode <- storageService.retrieveSaPostcode(journeyId)
      optCHRN <- storageService.retrieveCHRN(journeyId)
      matchingResult <- validateTrustKnownFactsService.validateTrustKnownFacts(journeyId, optSaUtr.map(_.value), optSaPostcode, optCHRN)
    } yield {
      matchingResult match {
        case SuccessfulMatch |
             UnMatchableWithoutRetry => journeyConfig.continueUrl(journeyId)
        case DetailsNotFound |
             DetailsMismatch |
             UnMatchableWithRetry    => errorControllers.routes.CannotConfirmBusinessController.show(journeyId).url
      }
    }

}
