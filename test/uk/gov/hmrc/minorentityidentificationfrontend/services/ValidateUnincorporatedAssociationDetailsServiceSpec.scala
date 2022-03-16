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

class ValidateUnincorporatedAssociationDetailsServiceSpec extends AnyWordSpec
  with Matchers
  with ScalaFutures
  with MockValidateUnincorporatedAssociationDetailsConnector
  with MockStorageService {

  object TestValidateUnincorporatedAssociationDetailsService extends
    ValidateUnincorporatedAssociationDetailsService(mockValidateUnincorporatedAssociationDetailsConnector, mockStorageService)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "Validation of an unincorporated association's details" should {

    "return SuccessfulMatch" when {

      "the user's utr and postcode are matched by the connector" in {

        mockValidateUnincorporatedAssociationDetailsConnector.validateUnincorporatedAssociationDetails(testCtutr, testOfficePostcode).
          returns(Future.successful(SuccessfulMatch))
        mockStorageService.storeIdentifiersMatch(testJourneyId, SuccessfulMatch).returns(Future.successful(SuccessfullyStored))

        val result = await(TestValidateUnincorporatedAssociationDetailsService.validateUnincorporatedAssociationDetails(
          testJourneyId, Some(testCtutr), Some(testOfficePostcode)))

        result mustBe SuccessfulMatch

        mockStorageService.storeIdentifiersMatch(testJourneyId, SuccessfulMatch) was called
      }
    }

    "return DetailsMismatch" when {

      "the user's utr and postcode are not matched by the connector" in {

        mockValidateUnincorporatedAssociationDetailsConnector.validateUnincorporatedAssociationDetails(testCtutr, testOfficePostcode).
          returns(Future.successful(DetailsMismatch))
        mockStorageService.storeIdentifiersMatch(testJourneyId, DetailsMismatch).returns(Future.successful(SuccessfullyStored))

        val result = await(TestValidateUnincorporatedAssociationDetailsService.validateUnincorporatedAssociationDetails(
          testJourneyId, Some(testCtutr), Some(testOfficePostcode)))

        result mustBe DetailsMismatch

        mockStorageService.storeIdentifiersMatch(testJourneyId, DetailsMismatch) was called
      }

    }

    "return DetailsNotFound" when {

      "the user's details cannot be found by the connector" in {

        mockValidateUnincorporatedAssociationDetailsConnector.validateUnincorporatedAssociationDetails(testCtutr, testOfficePostcode).
          returns(Future.successful(DetailsNotFound))
        mockStorageService.storeIdentifiersMatch(testJourneyId, DetailsNotFound).returns(Future.successful(SuccessfullyStored))

        val result = await(TestValidateUnincorporatedAssociationDetailsService.validateUnincorporatedAssociationDetails(
          testJourneyId, Some(testCtutr), Some(testOfficePostcode)))

        result mustBe DetailsNotFound

        mockStorageService.storeIdentifiersMatch(testJourneyId, DetailsNotFound) was called
      }

    }

    "raise an Illegal state exception" when {

      "a Ct Utr is defined but a post code is not" in { // In practice such a state should not occur

        try {
          TestValidateUnincorporatedAssociationDetailsService.validateUnincorporatedAssociationDetails(
            testJourneyId, Some(testCtutr), optPostcode = None)

          fail("Call to validate unincorporated association's details should have raised an IllegalStateException")

        } catch {
          case ise: IllegalStateException => ise.getMessage mustBe "Error : The post code for the unincorporated association is not defined"
          case t: Throwable => fail(s"Unexpected exception encountered : ${t.getMessage}")
        }

      }

    }

    "return Unmatchable" when {

      "the unincorporated association does not have a CT Utr" in {

        mockStorageService.storeIdentifiersMatch(testJourneyId, UnMatchable).returns(Future.successful(SuccessfullyStored))

        val result = await(TestValidateUnincorporatedAssociationDetailsService.validateUnincorporatedAssociationDetails(
          testJourneyId, optCtUtr = None, optPostcode = None))

        result mustBe UnMatchable

        mockStorageService.storeIdentifiersMatch(testJourneyId, UnMatchable) was called
      }
    }

  }

}
