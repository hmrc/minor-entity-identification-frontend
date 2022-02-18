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

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.minorentityidentificationfrontend.connectors.mocks.MockRetrieveTrustKnownFactsConnector
import uk.gov.hmrc.minorentityidentificationfrontend.helpers.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.httpparsers.StorageHttpParser.SuccessfullyStored
import uk.gov.hmrc.minorentityidentificationfrontend.models.KnownFactsMatching.{DetailsMismatch, SuccessfulMatch}
import uk.gov.hmrc.minorentityidentificationfrontend.services.mocks.MockStorageService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ValidateTrustKnownFactsServiceSpec extends AnyWordSpec with Matchers with MockRetrieveTrustKnownFactsConnector with MockStorageService {

  object TestJourneyService extends ValidateTrustKnownFactsService(mockRetrieveTrustKnownFactsConnector, mockStorageService)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "validateTrustKnownFacts" should {
    "return SuccessfulMatch" when {
      "the user's postcode matches a postcode received from the known facts call" in {
        mockRetrieveTrustKnownFactsConnector.retrieveTrustKnownFacts(testSautr) returns Future.successful(Right(testTrustKnownFactsResponse))
        mockStorageService.storeTrustsKnownFacts(testJourneyId, testTrustKnownFactsResponse) returns Future.successful(SuccessfullyStored)
        mockStorageService.storeIdentifiersMatch(testJourneyId, identifiersMatch = true) returns Future.successful(SuccessfullyStored)

        val result = await(TestJourneyService.validateTrustKnownFacts(testJourneyId, testSautr, Some(testSaPostcode)))

        result mustBe SuccessfulMatch

        mockStorageService.storeTrustsKnownFacts(testJourneyId, testTrustKnownFactsResponse) was called
        mockStorageService.storeIdentifiersMatch(testJourneyId, identifiersMatch = true) was called
      }
      "the user enters no postcode but the abroad indicator received from the known facts call is true" in {
        mockRetrieveTrustKnownFactsConnector.retrieveTrustKnownFacts(testSautr) returns Future.successful(Right(testTrustKnownFactsAbroadResponse))
        mockStorageService.storeTrustsKnownFacts(testJourneyId, testTrustKnownFactsAbroadResponse) returns Future.successful(SuccessfullyStored)
        mockStorageService.storeIdentifiersMatch(testJourneyId, identifiersMatch = true) returns Future.successful(SuccessfullyStored)

        val result = await(TestJourneyService.validateTrustKnownFacts(testJourneyId, testSautr, None))

        result mustBe SuccessfulMatch

        mockStorageService.storeTrustsKnownFacts(testJourneyId, testTrustKnownFactsAbroadResponse) was called
        mockStorageService.storeIdentifiersMatch(testJourneyId, identifiersMatch = true) was called
      }
    }
    "return DetailsMismatch" when {
      "the user's postcode doesn't match what is received from the known facts call" in {
        mockRetrieveTrustKnownFactsConnector.retrieveTrustKnownFacts(testSautr) returns Future.successful(Right(testTrustKnownFactsResponse))
        mockStorageService.storeTrustsKnownFacts(testJourneyId, testTrustKnownFactsResponse) returns Future.successful(SuccessfullyStored)
        mockStorageService.storeIdentifiersMatch(testJourneyId, identifiersMatch = false) returns Future.successful(SuccessfullyStored)

        val result = await(TestJourneyService.validateTrustKnownFacts(testJourneyId, testSautr, Some("AB0 0AA")))

        result mustBe DetailsMismatch

        mockStorageService.storeTrustsKnownFacts(testJourneyId, testTrustKnownFactsResponse) was called
        mockStorageService.storeIdentifiersMatch(testJourneyId, identifiersMatch = false) was called
      }
      "the user provides no postcode but the abroad indicator received from the known facts call is false" in {
        mockRetrieveTrustKnownFactsConnector.retrieveTrustKnownFacts(testSautr) returns Future.successful(Right(testTrustKnownFactsResponse))
        mockStorageService.storeTrustsKnownFacts(testJourneyId, testTrustKnownFactsResponse) returns Future.successful(SuccessfullyStored)
        mockStorageService.storeIdentifiersMatch(testJourneyId, identifiersMatch = false) returns Future.successful(SuccessfullyStored)

        val result = await(TestJourneyService.validateTrustKnownFacts(testJourneyId, testSautr, None))

        result mustBe DetailsMismatch

        mockStorageService.storeTrustsKnownFacts(testJourneyId, testTrustKnownFactsResponse) was called
        mockStorageService.storeIdentifiersMatch(testJourneyId, identifiersMatch = false) was called
      }
    }
  }

}
