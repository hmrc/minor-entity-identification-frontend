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
import uk.gov.hmrc.minorentityidentificationfrontend.models._
import uk.gov.hmrc.minorentityidentificationfrontend.services.mocks.MockStorageService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ValidateTrustKnownFactsServiceSpec extends AnyWordSpec with Matchers with MockRetrieveTrustKnownFactsConnector with MockStorageService {

  object TestJourneyService extends ValidateTrustKnownFactsService(mockRetrieveTrustKnownFactsConnector, mockStorageService)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "validateTrustKnownFacts" should {
    "return SuccessfulMatch" when {
      "the user's postcode matches a postcode received from the known facts call" in {
        mockRetrieveTrustKnownFactsConnector.retrieveTrustKnownFacts(testSautr) returns Future.successful(Some(testTrustKnownFactsResponse))
        mockStorageService.storeTrustsKnownFacts(testJourneyId, testTrustKnownFactsResponse) returns Future.successful(SuccessfullyStored)
        mockStorageService.storeIdentifiersMatch(testJourneyId, SuccessfulMatch) returns Future.successful(SuccessfullyStored)

        val result = await(TestJourneyService.validateTrustKnownFacts(testJourneyId, Some(testSautr), Some(testSaPostcode), optCHRN = None))

        result mustBe SuccessfulMatch

        mockStorageService.storeTrustsKnownFacts(testJourneyId, testTrustKnownFactsResponse) was called
        mockStorageService.storeIdentifiersMatch(testJourneyId, SuccessfulMatch) was called
      }
      "the user enters no postcode but the abroad indicator received from the known facts call is true" in {
        mockRetrieveTrustKnownFactsConnector.retrieveTrustKnownFacts(testSautr) returns Future.successful(Some(testTrustKnownFactsAbroadResponse))
        mockStorageService.storeTrustsKnownFacts(testJourneyId, testTrustKnownFactsAbroadResponse) returns Future.successful(SuccessfullyStored)
        mockStorageService.storeIdentifiersMatch(testJourneyId, SuccessfulMatch) returns Future.successful(SuccessfullyStored)

        val result = await(TestJourneyService.validateTrustKnownFacts(testJourneyId, Some(testSautr), None,  optCHRN = None))

        result mustBe SuccessfulMatch

        mockStorageService.storeTrustsKnownFacts(testJourneyId, testTrustKnownFactsAbroadResponse) was called
        mockStorageService.storeIdentifiersMatch(testJourneyId, SuccessfulMatch) was called
      }
    }
    "return DetailsMismatch" when {
      "the user's postcode doesn't match what is received from the known facts call" in {
        mockRetrieveTrustKnownFactsConnector.retrieveTrustKnownFacts(testSautr) returns Future.successful(Some(testTrustKnownFactsResponse))
        mockStorageService.storeTrustsKnownFacts(testJourneyId, testTrustKnownFactsResponse) returns Future.successful(SuccessfullyStored)
        mockStorageService.storeIdentifiersMatch(testJourneyId, DetailsMismatch) returns Future.successful(SuccessfullyStored)

        val result = await(TestJourneyService.validateTrustKnownFacts(testJourneyId, Some(testSautr), Some("AB0 0AA"), optCHRN = None))

        result mustBe DetailsMismatch

        mockStorageService.storeTrustsKnownFacts(testJourneyId, testTrustKnownFactsResponse) was called
        mockStorageService.storeIdentifiersMatch(testJourneyId, DetailsMismatch) was called
      }
      "the user provides no postcode but the abroad indicator received from the known facts call is false" in {
        mockRetrieveTrustKnownFactsConnector.retrieveTrustKnownFacts(testSautr) returns Future.successful(Some(testTrustKnownFactsResponse))
        mockStorageService.storeTrustsKnownFacts(testJourneyId, testTrustKnownFactsResponse) returns Future.successful(SuccessfullyStored)
        mockStorageService.storeIdentifiersMatch(testJourneyId, DetailsMismatch) returns Future.successful(SuccessfullyStored)

        val result = await(TestJourneyService.validateTrustKnownFacts(testJourneyId, Some(testSautr), None, optCHRN = None))

        result mustBe DetailsMismatch

        mockStorageService.storeTrustsKnownFacts(testJourneyId, testTrustKnownFactsResponse) was called
        mockStorageService.storeIdentifiersMatch(testJourneyId, DetailsMismatch) was called
      }
    }
    "return DetailsNotFound" when {
      "the trusts proxy call returns not found" in {
        mockRetrieveTrustKnownFactsConnector.retrieveTrustKnownFacts(testSautr) returns Future.successful(None)
        mockStorageService.storeIdentifiersMatch(testJourneyId, DetailsNotFound) returns Future.successful(SuccessfullyStored)

        val result = await(TestJourneyService.validateTrustKnownFacts(journeyId = testJourneyId,
          optSaUtr = Some(testSautr),
          optSaPostcode = Some("AB0 0AA"),
          optCHRN = None)
        )

        result mustBe DetailsNotFound

        mockStorageService.storeIdentifiersMatch(testJourneyId, DetailsNotFound) was called
      }
    }

    "return UnMatchableWithoutRetry" when {
      "the user provides no Sautr but provides CHRN" in {

          mockStorageService.storeIdentifiersMatch(testJourneyId, UnMatchableWithoutRetry) returns Future.successful(SuccessfullyStored)

          val result = await(TestJourneyService.validateTrustKnownFacts(journeyId = testJourneyId,
            optSaUtr = None,
            optSaPostcode = None,
            optCHRN = Some(testCharityHMRCReferenceNumber))
          )

          result mustBe UnMatchableWithoutRetry

          mockStorageService.storeIdentifiersMatch(testJourneyId, UnMatchableWithoutRetry) was called

          mockStorageService.storeTrustsKnownFacts(testJourneyId, testTrustKnownFactsResponse) wasNever called

      }
    }

    "return UnMatchableWithRetry" when {
      "the user provides no Sautr and no CHRN" in {
          mockStorageService.storeIdentifiersMatch(testJourneyId, UnMatchableWithRetry) returns Future.successful(SuccessfullyStored)

          val result = await(TestJourneyService.validateTrustKnownFacts(journeyId = testJourneyId,
            optSaUtr = None,
            optSaPostcode = None,
            optCHRN = None)
          )

          result mustBe UnMatchableWithRetry

          mockStorageService.storeIdentifiersMatch(testJourneyId, UnMatchableWithRetry) was called

          mockStorageService.storeTrustsKnownFacts(testJourneyId, testTrustKnownFactsResponse) wasNever called

      }
    }
  }

}
