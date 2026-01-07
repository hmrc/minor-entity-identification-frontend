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
import uk.gov.hmrc.minorentityidentificationfrontend.connectors.mocks.MockRegistrationConnector
import uk.gov.hmrc.minorentityidentificationfrontend.helpers.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.httpparsers.StorageHttpParser.SuccessfullyStored
import uk.gov.hmrc.minorentityidentificationfrontend.models._
import uk.gov.hmrc.minorentityidentificationfrontend.services.mocks.{MockAuditService, MockStorageService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RegistrationOrchestrationServiceSpec
  extends AnyWordSpec
    with Matchers
    with MockRegistrationConnector
    with MockStorageService
    with MockAuditService
    with BeforeAndAfterEach {

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    reset(mockRegistrationConnector)
    reset(mockAuditService)
    reset(mockStorageService)
  }

  object TestRegistrationOrchestrationService extends RegistrationOrchestrationService(mockStorageService, mockRegistrationConnector, mockAuditService)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "register trust" should {
    "return Registered" when {
      "the user has successfully passed BV check" in {
        when(mockStorageService.retrieveBusinessVerificationStatus(testJourneyId)).thenReturn(Future.successful(Some(BusinessVerificationPass)))
        when(mockRegistrationConnector.registerTrust(testSautr, testRegime)).thenReturn(Future.successful(Registered(testSafeId)))
        when(mockStorageService.storeRegistrationStatus(testJourneyId, Registered(testSafeId))).thenReturn(Future.successful(SuccessfullyStored))

        when(mockAuditService.auditJourney(testJourneyId, testTrustJourneyConfig())).thenReturn(Future.successful(()))

        val result = await(TestRegistrationOrchestrationService.register(testJourneyId, Some(testSautr), testTrustJourneyConfig()))

        result mustBe Registered(testSafeId)

        verify(mockAuditService).auditJourney(testJourneyId, testTrustJourneyConfig())
      }
      "the Business Verification Check is disabled" in {
        when(mockStorageService.retrieveBusinessVerificationStatus(testJourneyId)).thenReturn(Future.successful(None))
        when(mockRegistrationConnector.registerTrust(testSautr, testRegime)).thenReturn(Future.successful(Registered(testSafeId)))
        when(mockStorageService.storeRegistrationStatus(testJourneyId, Registered(testSafeId))).thenReturn(Future.successful(SuccessfullyStored))

        when(mockAuditService.auditJourney(testJourneyId, testTrustJourneyConfig(false))).thenReturn(Future.successful(()))

        val result = await(TestRegistrationOrchestrationService.register(testJourneyId, Some(testSautr), testTrustJourneyConfig(false)))

        result mustBe Registered(testSafeId)

        verify(mockAuditService).auditJourney(testJourneyId, testTrustJourneyConfig(false))
      }
    }
    "return RegistrationNotCalled" when {
      "the user did not pass BV checks" in {
        when(mockStorageService.retrieveBusinessVerificationStatus(testJourneyId)).thenReturn(Future.successful(Some(BusinessVerificationFail)))
        when(mockRegistrationConnector.registerTrust(testSautr, testRegime)).thenReturn(Future.successful(RegistrationNotCalled))
        when(mockStorageService.storeRegistrationStatus(testJourneyId,RegistrationNotCalled)).thenReturn(Future.successful(SuccessfullyStored))

        when(mockAuditService.auditJourney(testJourneyId, testTrustJourneyConfig())).thenReturn(Future.successful(()))

        val result = await(TestRegistrationOrchestrationService.register(testJourneyId, Some(testSautr), testTrustJourneyConfig()))

        result mustBe RegistrationNotCalled

        verify(mockAuditService).auditJourney(testJourneyId, testTrustJourneyConfig())
      }
    }
  }

  "register Unincorporated association" should {
    "return Registered" when {
      "the user has successfully passed BV check" in {
        when(mockStorageService.retrieveBusinessVerificationStatus(testJourneyId)).thenReturn(Future.successful(Some(BusinessVerificationPass)))
        when(mockRegistrationConnector.registerUA(testCtutr, testRegime)).thenReturn(Future.successful(Registered(testSafeId)))
        when(mockStorageService.storeRegistrationStatus(testJourneyId, Registered(testSafeId))).thenReturn(Future.successful(SuccessfullyStored))

        when(mockAuditService.auditJourney(testJourneyId, testUAJourneyConfig())).thenReturn(Future.successful(()))

        val result = await(TestRegistrationOrchestrationService.register(testJourneyId, Some(testCtutr), testUAJourneyConfig()))

        result mustBe Registered(testSafeId)

        verify(mockAuditService).auditJourney(testJourneyId, testUAJourneyConfig())
      }
      "the Business Verification Check is disabled" in {
        when(mockStorageService.retrieveBusinessVerificationStatus(testJourneyId)).thenReturn(Future.successful(None))
        when(mockRegistrationConnector.registerUA(testCtutr, testRegime)).thenReturn(Future.successful(Registered(testSafeId)))
        when(mockStorageService.storeRegistrationStatus(testJourneyId, Registered(testSafeId))).thenReturn(Future.successful(SuccessfullyStored))

        when(mockAuditService.auditJourney(journeyId = testJourneyId, journeyConfig = testUAJourneyConfig(false))).thenReturn(Future.successful(()))

        val result = await(TestRegistrationOrchestrationService.register(testJourneyId, Some(testCtutr), testUAJourneyConfig(false)))

        result mustBe Registered(testSafeId)

        verify(mockAuditService).auditJourney(journeyId = testJourneyId, journeyConfig = testUAJourneyConfig(false))
      }
    }
    "return RegistrationNotCalled" when {
      "the user did not pass BV checks" in {
        when(mockStorageService.retrieveBusinessVerificationStatus(testJourneyId)).thenReturn(Future.successful(Some(BusinessVerificationFail)))
        when(mockRegistrationConnector.registerUA(testCtutr, testRegime)).thenReturn(Future.successful(RegistrationNotCalled))
        when(mockStorageService.storeRegistrationStatus(testJourneyId,RegistrationNotCalled)).thenReturn(Future.successful(SuccessfullyStored))

        when(mockAuditService.auditJourney(testJourneyId, testUAJourneyConfig())).thenReturn(Future.successful(()))

        val result = await(TestRegistrationOrchestrationService.register(testJourneyId, Some(testCtutr), testUAJourneyConfig()))

        result mustBe RegistrationNotCalled

        verify(mockAuditService).auditJourney(testJourneyId, testUAJourneyConfig())
      }
    }
  }

  "register overseas company" should {
    "throw an exception for registration" in {
      val actualException = intercept[IllegalArgumentException] {
        await(TestRegistrationOrchestrationService.register(testJourneyId, Some(testCtutr), testOverseasJourneyConfig()))
      }
      actualException.getMessage mustBe "Overseas Company is not supported for registration."
    }
  }

}
