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

import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
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

class TrustMatchingResultCalculatorSpec
  extends AnyWordSpec
    with Matchers
    with MockRetrieveTrustKnownFactsConnector
    with MockStorageService
    with BeforeAndAfterEach {

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    reset(mockRetrieveTrustKnownFactsConnector)
    reset(mockStorageService)
  }

  object TestValidateTrustKnownFactsService extends TrustMatchingResultCalculator(mockRetrieveTrustKnownFactsConnector, mockStorageService)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "validateTrustKnownFacts" should {
    "return SuccessfulMatch" when {
      "the user's postcode matches a postcode received from the known facts call" when {
        "the postcode is lower case" in {
          when(mockRetrieveTrustKnownFactsConnector.retrieveTrustKnownFacts(testSautr)).thenReturn(Future.successful(Some(testTrustKnownFactsResponse)))
          when(mockStorageService.storeIdentifiersMatch(testJourneyId, SuccessfulMatch)).thenReturn(Future.successful(SuccessfullyStored))

          val result = await(TestValidateTrustKnownFactsService.matchKnownFacts(testJourneyId, Some(testSautr), Some("aa11aa")))

          result mustBe SuccessfulMatch

          verify(mockStorageService).storeIdentifiersMatch(testJourneyId, SuccessfulMatch)
        }
        "the postcode is uppercase" in {
          when(mockRetrieveTrustKnownFactsConnector.retrieveTrustKnownFacts(testSautr)).thenReturn(Future.successful(Some(testTrustKnownFactsResponse)))
          when(mockStorageService.storeIdentifiersMatch(testJourneyId, SuccessfulMatch)).thenReturn(Future.successful(SuccessfullyStored))

          val result = await(TestValidateTrustKnownFactsService.matchKnownFacts(testJourneyId, Some(testSautr), Some(testSaPostcode)))

          result mustBe SuccessfulMatch

          verify(mockStorageService).storeIdentifiersMatch(testJourneyId, SuccessfulMatch)
        }
      }
      "the user enters no postcode but the abroad indicator received from the known facts call is true" in {
        when(mockRetrieveTrustKnownFactsConnector.retrieveTrustKnownFacts(testSautr)).thenReturn(Future.successful(Some(testTrustKnownFactsAbroadResponse)))
        when(mockStorageService.storeIdentifiersMatch(testJourneyId, SuccessfulMatch)).thenReturn(Future.successful(SuccessfullyStored))

        val result = await(TestValidateTrustKnownFactsService.matchKnownFacts(testJourneyId, Some(testSautr), None))

        result mustBe SuccessfulMatch

        verify(mockStorageService).storeIdentifiersMatch(testJourneyId, SuccessfulMatch)
      }
    }
    "return DetailsMismatch" when {
      "the user's postcode doesn't match what is received from the known facts call" in {
        when(mockRetrieveTrustKnownFactsConnector.retrieveTrustKnownFacts(testSautr)).thenReturn(Future.successful(Some(testTrustKnownFactsResponse)))
        when(mockStorageService.storeIdentifiersMatch(testJourneyId, DetailsMismatch)).thenReturn(Future.successful(SuccessfullyStored))

        val result = await(TestValidateTrustKnownFactsService.matchKnownFacts(testJourneyId, Some(testSautr), Some("AB0 0AA")))

        result mustBe DetailsMismatch

        verify(mockStorageService).storeIdentifiersMatch(testJourneyId, DetailsMismatch)
      }
      "the user provides no postcode but the abroad indicator received from the known facts call is false" in {
        when(mockRetrieveTrustKnownFactsConnector.retrieveTrustKnownFacts(testSautr)).thenReturn(Future.successful(Some(testTrustKnownFactsResponse)))
        when(mockStorageService.storeIdentifiersMatch(testJourneyId, DetailsMismatch)).thenReturn(Future.successful(SuccessfullyStored))

        val result = await(TestValidateTrustKnownFactsService.matchKnownFacts(testJourneyId, Some(testSautr), None))

        result mustBe DetailsMismatch

        verify(mockStorageService).storeIdentifiersMatch(testJourneyId, DetailsMismatch)
      }
    }
    "return DetailsNotFound" when {
      "the trusts proxy call returns not found" in {
        when(mockRetrieveTrustKnownFactsConnector.retrieveTrustKnownFacts(testSautr)).thenReturn(Future.successful(None))
        when(mockStorageService.storeIdentifiersMatch(testJourneyId, DetailsNotFound)).thenReturn(Future.successful(SuccessfullyStored))

        val result = await(TestValidateTrustKnownFactsService.matchKnownFacts(journeyId = testJourneyId,
          optSaUtr = Some(testSautr),
          optSaPostcode = Some("AB0 0AA")
        )
        )

        result mustBe DetailsNotFound

        verify(mockStorageService).storeIdentifiersMatch(testJourneyId, DetailsNotFound)
      }
    }
    "return UnMatchable" when {
      "the user provides no Sautr" in {
        when(mockStorageService.storeIdentifiersMatch(testJourneyId, UnMatchable)).thenReturn(Future.successful(SuccessfullyStored))

        val result = await(TestValidateTrustKnownFactsService.matchKnownFacts(journeyId = testJourneyId,
          optSaUtr = None,
          optSaPostcode = None
        )
        )

        result mustBe UnMatchable

        verify(mockStorageService).storeIdentifiersMatch(testJourneyId, UnMatchable)
      }

      "the user provides no Sautr and no CHRN" in {
        when(mockStorageService.storeIdentifiersMatch(testJourneyId, UnMatchable)).thenReturn(Future.successful(SuccessfullyStored))

        val result = await(TestValidateTrustKnownFactsService.matchKnownFacts(journeyId = testJourneyId,
          optSaUtr = None,
          optSaPostcode = None
        )
        )

        result mustBe UnMatchable

        verify(mockStorageService).storeIdentifiersMatch(testJourneyId, UnMatchable)
      }
    }

  }
}
