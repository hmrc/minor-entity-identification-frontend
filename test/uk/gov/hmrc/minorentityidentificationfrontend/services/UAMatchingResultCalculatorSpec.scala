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
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.minorentityidentificationfrontend.connectors.mocks.MockValidateUnincorporatedAssociationDetailsConnector
import uk.gov.hmrc.minorentityidentificationfrontend.helpers.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.httpparsers.StorageHttpParser.SuccessfullyStored
import uk.gov.hmrc.minorentityidentificationfrontend.models._
import uk.gov.hmrc.minorentityidentificationfrontend.services.mocks.MockStorageService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UAMatchingResultCalculatorSpec extends AnyWordSpec
  with Matchers
  with ScalaFutures
  with MockValidateUnincorporatedAssociationDetailsConnector
  with MockStorageService
  with BeforeAndAfterEach {

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    reset(mockValidateUnincorporatedAssociationDetailsConnector)
    reset(mockStorageService)
  }

  object TestUAMatchingResultCalculator extends
    UAMatchingResultCalculator(mockValidateUnincorporatedAssociationDetailsConnector, mockStorageService)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "MatchKnownFacts of an unincorporated association's details" should {

    "return SuccessfulMatch" when {

      "the user's utr and postcode are matched by the connector" in {

        when(mockValidateUnincorporatedAssociationDetailsConnector.validateUnincorporatedAssociationDetails(testCtutr, testOfficePostcode))
          .thenReturn(Future.successful(SuccessfulMatch))
        when(mockStorageService.storeIdentifiersMatch(testJourneyId, SuccessfulMatch)).thenReturn(Future.successful(SuccessfullyStored))

        val result = await(TestUAMatchingResultCalculator.matchKnownFacts(
          testJourneyId, Some(testCtutr), Some(testOfficePostcode))
        )

        result mustBe SuccessfulMatch

        verify(mockStorageService).storeIdentifiersMatch(testJourneyId, SuccessfulMatch)
      }
    }

    "return DetailsMismatch" when {

      "the user's utr and postcode are not matched by the connector" in {

        when(mockValidateUnincorporatedAssociationDetailsConnector.validateUnincorporatedAssociationDetails(testCtutr, testOfficePostcode))
          .thenReturn(Future.successful(DetailsMismatch))
        when(mockStorageService.storeIdentifiersMatch(testJourneyId, DetailsMismatch)).thenReturn(Future.successful(SuccessfullyStored))

        val result = await(TestUAMatchingResultCalculator.matchKnownFacts(
          testJourneyId, Some(testCtutr), Some(testOfficePostcode))
        )

        result mustBe DetailsMismatch

        verify(mockStorageService).storeIdentifiersMatch(testJourneyId, DetailsMismatch)
      }

    }

    "return DetailsNotFound" when {

      "the user's details cannot be found by the connector" in {

        when(mockValidateUnincorporatedAssociationDetailsConnector.validateUnincorporatedAssociationDetails(testCtutr, testOfficePostcode))
          .thenReturn(Future.successful(DetailsNotFound))
        when(mockStorageService.storeIdentifiersMatch(testJourneyId, DetailsNotFound)).thenReturn(Future.successful(SuccessfullyStored))

        val result = await(TestUAMatchingResultCalculator.matchKnownFacts(
          testJourneyId, Some(testCtutr), Some(testOfficePostcode))
        )

        result mustBe DetailsNotFound

        verify(mockStorageService).storeIdentifiersMatch(testJourneyId, DetailsNotFound)
      }

    }

    "raise an Illegal state exception" when {

      "a Ct Utr is defined but a post code is not (Such a state should not occur)" in {

        val theActualException: IllegalStateException = intercept[IllegalStateException] {
          await(TestUAMatchingResultCalculator.matchKnownFacts(testJourneyId, Some(testCtutr), optPostcode = None))
        }

        theActualException.getMessage mustBe "Error : The post code for the unincorporated association is not defined"

      }

    }

    "return Unmatchable" when {

      "the unincorporated association does not have a CT Utr" in {

        when(mockStorageService.storeIdentifiersMatch(testJourneyId, UnMatchable)).thenReturn(Future.successful(SuccessfullyStored))

        val result = await(TestUAMatchingResultCalculator.matchKnownFacts(testJourneyId, optCtUtr = None, optPostcode = None))

        result mustBe UnMatchable

        verify(mockStorageService).storeIdentifiersMatch(testJourneyId, UnMatchable)
      }
    }

  }

}
