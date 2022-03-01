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
import uk.gov.hmrc.minorentityidentificationfrontend.connectors.RetrieveTrustKnownFactsConnector
import uk.gov.hmrc.minorentityidentificationfrontend.models._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ValidateTrustKnownFactsService @Inject()(retrieveTrustKnownFactsConnector: RetrieveTrustKnownFactsConnector,
                                               storageService: StorageService) {

  def validateTrustKnownFacts(journeyId: String,
                              optSaUtr: Option[String],
                              optSaPostcode: Option[String],
                              optCHRN: Option[String])
                             (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[KnownFactsMatchingResult] =
    optSaUtr match {
      case None =>
        val identifiersMatchFailure: KnownFactsMatchFailure = if (optCHRN.isEmpty) UnMatchableWithRetry else UnMatchableWithoutRetry
        storageService.storeIdentifiersMatch(journeyId, identifiersMatchFailure).map(_ => identifiersMatchFailure)
      case Some(saUtr) =>
        for {
          knownFactsMatchResult <- retrieveTrustKnownFactsConnector.retrieveTrustKnownFacts(saUtr).flatMap {
            case Some(knownFacts) =>
              if (knownFacts.isAbroad && optSaPostcode.isEmpty |
                postcodeMatches(optSaPostcode, knownFacts.correspondencePostcode, knownFacts.declarationPostcode)) Future.successful(SuccessfulMatch)
              else Future.successful(DetailsMismatch)
            case None => Future.successful(DetailsNotFound)
          }
          _ <- storageService.storeIdentifiersMatch(journeyId, knownFactsMatchResult)
        } yield knownFactsMatchResult
    }

  def postcodeMatches(userPostcode: Option[String], optCorrespondencePostcode: Option[String], optDeclarationPostcode: Option[String]): Boolean = {
    if (userPostcode.isDefined) {
      (optCorrespondencePostcode, optDeclarationPostcode) match {
        case (Some(correspondencePostcode), Some(declarationPostcode)) =>
          (userPostcode.get.filterNot(_.isWhitespace) equalsIgnoreCase (correspondencePostcode filterNot (_.isWhitespace))) |
            (userPostcode.get.filterNot(_.isWhitespace) equalsIgnoreCase (declarationPostcode filterNot (_.isWhitespace)))
        case (Some(correspondencePostcode), _) =>
          userPostcode.get.filterNot(_.isWhitespace) equalsIgnoreCase (correspondencePostcode filterNot (_.isWhitespace))
        case (_, Some(declarationPostcode)) =>
          userPostcode.get.filterNot(_.isWhitespace) equalsIgnoreCase (declarationPostcode filterNot (_.isWhitespace))
      }
    }
    else false
  }

}
