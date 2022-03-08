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
import uk.gov.hmrc.minorentityidentificationfrontend.connectors.mocks.MockRegistrationConnector
import uk.gov.hmrc.minorentityidentificationfrontend.helpers.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.httpparsers.StorageHttpParser.SuccessfullyStored
import uk.gov.hmrc.minorentityidentificationfrontend.models._
import uk.gov.hmrc.minorentityidentificationfrontend.services.mocks.{MockAuditService, MockStorageService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RegistrationOrchestrationServiceSpec extends AnyWordSpec with Matchers with MockRegistrationConnector with MockStorageService with MockAuditService {

  object TestRegistrationOrchestrationService extends RegistrationOrchestrationService(mockStorageService, mockRegistrationConnector, mockAuditService)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "register" should {
    "return Registered" when {
      "the user has successfully passed BV check" in {
        mockStorageService.retrieveBusinessVerificationStatus(testJourneyId) returns Future.successful(Some(BusinessVerificationPass))
        mockRegistrationConnector.register(testSautr, testRegime) returns Future.successful(Registered(testSafeId))
        mockStorageService.storeRegistrationStatus(testJourneyId, Registered(testSafeId)) returns Future.successful(SuccessfullyStored)

        mockAuditService.auditJourney(testJourneyId, testTrustJourneyConfig()) returns Future.successful(())

        val result = await(TestRegistrationOrchestrationService.register(testJourneyId, Some(testSautr), testTrustJourneyConfig()))

        result mustBe Registered(testSafeId)

        mockAuditService.auditJourney(testJourneyId, testTrustJourneyConfig()) was called
      }
      "the Business Verification Check is disabled" in {
        mockStorageService.retrieveBusinessVerificationStatus(testJourneyId) returns Future.successful(None)
        mockRegistrationConnector.register(testSautr, testRegime) returns Future.successful(Registered(testSafeId))
        mockStorageService.storeRegistrationStatus(testJourneyId, Registered(testSafeId)) returns Future.successful(SuccessfullyStored)

        mockAuditService.auditJourney(testJourneyId, testTrustJourneyConfig(false)) returns Future.successful(())

        val result = await(TestRegistrationOrchestrationService.register(testJourneyId, Some(testSautr), testTrustJourneyConfig(false)))

        result mustBe Registered(testSafeId)

        mockAuditService.auditJourney(testJourneyId, testTrustJourneyConfig(false)) was called
      }
    }
    "return RegistrationNotCalled" when {
      "the user did not pass BV checks" in {
        mockStorageService.retrieveBusinessVerificationStatus(testJourneyId) returns Future.successful(Some(BusinessVerificationFail))
        mockRegistrationConnector.register(testSautr, testRegime) returns Future.successful(RegistrationNotCalled)
        mockStorageService.storeRegistrationStatus(testJourneyId,RegistrationNotCalled) returns Future.successful(SuccessfullyStored)

        mockAuditService.auditJourney(testJourneyId, testTrustJourneyConfig()) returns Future.successful(())

        val result = await(TestRegistrationOrchestrationService.register(testJourneyId, Some(testSautr), testTrustJourneyConfig()))

        result mustBe RegistrationNotCalled

        mockAuditService.auditJourney(testJourneyId, testTrustJourneyConfig()) was called
      }
    }
  }

}
