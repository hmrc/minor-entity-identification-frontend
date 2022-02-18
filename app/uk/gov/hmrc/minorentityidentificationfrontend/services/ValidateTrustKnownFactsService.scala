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
import uk.gov.hmrc.minorentityidentificationfrontend.connectors.RetrieveTrustKnownFactsConnector
import uk.gov.hmrc.minorentityidentificationfrontend.models.KnownFactsMatching.{DetailsMismatch, DetailsNotFound, KnownFactsMatchingResult, SuccessfulMatch}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ValidateTrustKnownFactsService @Inject()(retrieveTrustKnownFactsConnector: RetrieveTrustKnownFactsConnector,
                                               storageService: StorageService) {

  def validateTrustKnownFacts(journeyId: String,
                              sautr: String,
                              saPostcode: Option[String]
                             )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[KnownFactsMatchingResult] = {
    for {
      knownFactsMatchResult <- retrieveTrustKnownFactsConnector.retrieveTrustKnownFacts(sautr).flatMap {
        case Right(knownFacts) =>
          storageService.storeTrustsKnownFacts(journeyId, knownFacts).map {
            _ =>
              if (saPostcode.isEmpty && knownFacts.isAbroad) {
                SuccessfulMatch
              }
              else if (saPostcode == knownFacts.declarationPostcode | saPostcode == knownFacts.correspondencePostcode) SuccessfulMatch
              else DetailsMismatch
          }
        case Left(DetailsNotFound) => Future.successful(DetailsNotFound)
        case _ => throw new InternalServerException("Unexpected status returned from RetrieveTrustKnownFactsConnector")
      }
      _ <- knownFactsMatchResult match {
        case SuccessfulMatch => storageService.storeIdentifiersMatch(journeyId, identifiersMatch = true)
        case _ => storageService.storeIdentifiersMatch(journeyId, identifiersMatch = false)
      }
    } yield knownFactsMatchResult
  }

}
