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

import org.mockito.Mockito.{never, reset, verify, verifyNoInteractions, verifyNoMoreInteractions, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.minorentityidentificationfrontend.helpers.TestConstants.*
import uk.gov.hmrc.minorentityidentificationfrontend.httpparsers.StorageHttpParser.SuccessfullyStored
import uk.gov.hmrc.minorentityidentificationfrontend.models.*
import uk.gov.hmrc.minorentityidentificationfrontend.services.mocks.*

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubmissionServiceSpec extends AnyWordSpec
  with Matchers
  with MockStorageService
  with MockBusinessVerificationService
  with MockAuditService
  with MockRegistrationOrchestrationService
  with MockMatchingResultCalculator
  with BeforeAndAfterEach {

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    reset(mockStorageService)
    reset(mockBusinessVerificationService)
    reset(mockAuditService)
    reset(mockRegistrationOrchestrationService)
    reset(mockMatchingResultCalculator)
  }

  implicit val hc: HeaderCarrier = HeaderCarrier()

  object TestSubmissionService extends SubmissionService(
    mockStorageService,
    mockAuditService,
    mockBusinessVerificationService,
    mockRegistrationOrchestrationService
  )

  "given businessVerificationCheck is true, submit" should {
    "create a BusinessVerificationJourney and return the businessVerificationUrl" when {
      "TrustKnownFacts is SuccessfulMatch and BV creates a businessVerificationUrl" in {
        when(mockStorageService.retrieveUtr(testJourneyId)).thenReturn(Future.successful(Some(Sautr(testSautr))))
        when(mockStorageService.retrievePostcode(testJourneyId)).thenReturn(Future.successful(Some(testSaPostcode)))

        when(mockMatchingResultCalculator.matchKnownFacts(
          journeyId = testJourneyId,
          optUtr = Some(testSautr),
          optPostcode = Some(testSaPostcode))).thenReturn(Future.successful(SuccessfulMatch))

        when(mockBusinessVerificationService.createBusinessVerificationJourney(testJourneyId,
          testSautr,
          testTrustJourneyConfig())).thenReturn(Future.successful(Some(testBusinessVerificationRedirectUrl)))

        val result = await(
          TestSubmissionService.submit(journeyId = testJourneyId,
            journeyConfig = testTrustJourneyConfig(),
            matchingResultCalculator = mockMatchingResultCalculator,
            cannotConfirmErrorPageUrl = testCannotConfirmErrorPageUrl)
        )

        result mustBe testBusinessVerificationRedirectUrl

        verify(mockStorageService, never()).storeRegistrationStatus(testJourneyId, RegistrationNotCalled)

      }
    }
    "create a BusinessVerificationJourney and return the full journey continue url" when {
      "TrustKnownFacts is SuccessfulMatch but BV somehow fails." in {
        when(mockStorageService.retrieveUtr(testJourneyId)).thenReturn(Future.successful(Some(Sautr(testSautr))))
        when(mockStorageService.retrievePostcode(testJourneyId)).thenReturn(Future.successful(Some(testSaPostcode)))

        when(mockMatchingResultCalculator.matchKnownFacts(
          journeyId = testJourneyId,
          optUtr = Some(testSautr),
          optPostcode = Some(testSaPostcode))).thenReturn(Future.successful(SuccessfulMatch))

        when(mockBusinessVerificationService.createBusinessVerificationJourney(testJourneyId,
          testSautr,
          testTrustJourneyConfig())).thenReturn(Future.successful(None))

        when(mockStorageService.storeRegistrationStatus(testJourneyId, RegistrationNotCalled)).thenReturn(Future.successful(SuccessfullyStored))

        when(mockAuditService.auditJourney(testJourneyId, testTrustJourneyConfig())).thenReturn(Future.successful(()))

        val result = await(
          TestSubmissionService.submit(journeyId = testJourneyId,
            journeyConfig = testTrustJourneyConfig(),
            matchingResultCalculator = mockMatchingResultCalculator,
            cannotConfirmErrorPageUrl = testCannotConfirmErrorPageUrl)
        )

        result mustBe testTrustJourneyConfig().fullContinueUrl(testJourneyId)

        verify(mockStorageService).storeRegistrationStatus(testJourneyId, RegistrationNotCalled)
        verify(mockAuditService).auditJourney(testJourneyId, testTrustJourneyConfig())

      }
      "TrustKnownFacts is UnMatchable" in {

        when(mockStorageService.retrieveUtr(testJourneyId)).thenReturn(Future.successful(Some(Sautr(testSautr))))
        when(mockStorageService.retrievePostcode(testJourneyId)).thenReturn(Future.successful(Some(testSaPostcode)))

        when(mockMatchingResultCalculator.matchKnownFacts(journeyId = testJourneyId,
          optUtr = Some(testSautr),
          optPostcode = Some(testSaPostcode))).thenReturn(Future.successful(UnMatchable))

        when(mockStorageService.storeBusinessVerificationStatus(testJourneyId, BusinessVerificationNotEnoughInformationToCallBV))
        .thenReturn(Future.successful(SuccessfullyStored))

        when(mockStorageService.storeRegistrationStatus(testJourneyId, RegistrationNotCalled)).thenReturn(Future.successful(SuccessfullyStored))

        when(mockAuditService.auditJourney(testJourneyId, testTrustJourneyConfig())).thenReturn(Future.successful(()))

        val result = await(
          TestSubmissionService.submit(journeyId = testJourneyId,
            journeyConfig = testTrustJourneyConfig(),
            matchingResultCalculator = mockMatchingResultCalculator,
            cannotConfirmErrorPageUrl = testCannotConfirmErrorPageUrl)
        )

        result mustBe testTrustJourneyConfig().fullContinueUrl(testJourneyId)

        verify(mockAuditService).auditJourney(testJourneyId, testTrustJourneyConfig())
        verify(mockStorageService).storeRegistrationStatus(testJourneyId, RegistrationNotCalled)
        verifyNoInteractions(mockBusinessVerificationService)
      }
    }
    "not create a BusinessVerificationJourney and return Cannot Confirm ErrorPage url" when {
      "TrustKnownFacts is one of DetailsNotFound, DetailsMismatch" in {

        List(DetailsNotFound, DetailsMismatch).foreach(knownFactsMatchFailure => {

          when(mockStorageService.retrieveUtr(testJourneyId)).thenReturn(Future.successful(Some(Sautr(testSautr))))
          when(mockStorageService.retrievePostcode(testJourneyId)).thenReturn(Future.successful(Some(testSaPostcode)))

          when(mockMatchingResultCalculator.matchKnownFacts(journeyId = testJourneyId,
            optUtr = Some(testSautr),
            optPostcode = Some(testSaPostcode))).thenReturn(Future.successful(knownFactsMatchFailure))

          when(mockStorageService.storeBusinessVerificationStatus(testJourneyId, BusinessVerificationNotEnoughInformationToCallBV))
          .thenReturn(Future.successful(SuccessfullyStored))

          when(mockStorageService.storeRegistrationStatus(testJourneyId, RegistrationNotCalled)).thenReturn(Future.successful(SuccessfullyStored))

          when(mockAuditService.auditJourney(testJourneyId, testTrustJourneyConfig())).thenReturn(Future.successful(()))

          val result = await(
            TestSubmissionService.submit(journeyId = testJourneyId,
              journeyConfig = testTrustJourneyConfig(),
              matchingResultCalculator = mockMatchingResultCalculator,
              cannotConfirmErrorPageUrl = testCannotConfirmErrorPageUrl)
          )

          result mustBe testCannotConfirmErrorPageUrl

          verify(mockStorageService).retrieveUtr(testJourneyId)
          verify(mockStorageService).retrievePostcode(testJourneyId)
          verify(mockStorageService).storeBusinessVerificationStatus(testJourneyId, BusinessVerificationNotEnoughInformationToCallBV)
          verify(mockStorageService).storeRegistrationStatus(testJourneyId, RegistrationNotCalled)

          verifyNoMoreInteractions(mockStorageService)

          verify(mockAuditService).auditJourney(testJourneyId, testTrustJourneyConfig())

          verifyNoInteractions(mockBusinessVerificationService)

          reset(mockStorageService, mockBusinessVerificationService, mockAuditService)
        })

      }
    }
    "throw an exception" when {
      "SuccessfulMatch but SaUtr is not defined" in {
        when(mockStorageService.retrieveUtr(testJourneyId)).thenReturn(Future.successful(None))
        when(mockStorageService.retrievePostcode(testJourneyId)).thenReturn(Future.successful(Some(testSaPostcode)))

        when(mockMatchingResultCalculator.matchKnownFacts(journeyId = testJourneyId,
          optUtr = None,
          optPostcode = Some(testSaPostcode))).thenReturn(Future.successful(SuccessfulMatch))

        val theActualException: IllegalStateException = intercept[IllegalStateException] {
          await(
            TestSubmissionService.submit(journeyId = testJourneyId,
              journeyConfig = testTrustJourneyConfig(),
              matchingResultCalculator = mockMatchingResultCalculator,
              cannotConfirmErrorPageUrl = testCannotConfirmErrorPageUrl)
          )
        }

        theActualException.getMessage mustBe "Error: SA UTR is not defined"
      }
    }
  }

  "given businessVerificationCheck is false, submit" should {
    val trustJourneyConfigWithoutBVCheck = testTrustJourneyConfig().copy(businessVerificationCheck = false)
    "not create a BusinessVerificationJourney, not store BusinessVerificationStatus and return the full journey continue url" when {
      "TrustKnownFacts is SuccessfulMatch" in {
        when(mockStorageService.retrieveUtr(testJourneyId)).thenReturn(Future.successful(Some(Sautr(testSautr))))
        when(mockStorageService.retrievePostcode(testJourneyId)).thenReturn(Future.successful(Some(testSaPostcode)))

        when(mockMatchingResultCalculator.matchKnownFacts(journeyId = testJourneyId,
          optUtr = Some(testSautr),
          optPostcode = Some(testSaPostcode))).thenReturn(Future.successful(SuccessfulMatch))

        when(mockRegistrationOrchestrationService.register(testJourneyId, Some(testSautr), trustJourneyConfigWithoutBVCheck))
          .thenReturn(Future.successful(Registered(testSafeId)))

        val result = await(
          TestSubmissionService.submit(journeyId = testJourneyId,
            journeyConfig = trustJourneyConfigWithoutBVCheck,
            matchingResultCalculator = mockMatchingResultCalculator,
            cannotConfirmErrorPageUrl = testCannotConfirmErrorPageUrl
          )
        )

        result mustBe testTrustJourneyConfig().fullContinueUrl(testJourneyId)

        verify(mockStorageService).retrieveUtr(testJourneyId)
        verify(mockStorageService).retrievePostcode(testJourneyId)

        verifyNoMoreInteractions(mockStorageService)

        verify(mockRegistrationOrchestrationService).register(testJourneyId, Some(testSautr), trustJourneyConfigWithoutBVCheck)

        verify(mockAuditService, never()).auditJourney(testJourneyId, trustJourneyConfigWithoutBVCheck)

        verifyNoInteractions(mockBusinessVerificationService)
      }
      "TrustKnownFacts is UnMatchable" in {
        when(mockStorageService.retrieveUtr(testJourneyId)).thenReturn(Future.successful(Some(Sautr(testSautr))))
        when(mockStorageService.retrievePostcode(testJourneyId)).thenReturn(Future.successful(Some(testSaPostcode)))

        when(mockMatchingResultCalculator.matchKnownFacts(journeyId = testJourneyId,
          optUtr = Some(testSautr),
          optPostcode = Some(testSaPostcode))).thenReturn(Future.successful(UnMatchable))

        when(mockStorageService.storeRegistrationStatus(testJourneyId, RegistrationNotCalled)).thenReturn(Future.successful(SuccessfullyStored))

        when(mockAuditService.auditJourney(testJourneyId, trustJourneyConfigWithoutBVCheck)).thenReturn(Future.successful(()))

        val result = await(
          TestSubmissionService.submit(journeyId = testJourneyId,
            journeyConfig = trustJourneyConfigWithoutBVCheck,
            matchingResultCalculator = mockMatchingResultCalculator,
            cannotConfirmErrorPageUrl = testCannotConfirmErrorPageUrl
          )
        )

        result mustBe testTrustJourneyConfig().fullContinueUrl(testJourneyId)

        verify(mockStorageService).retrieveUtr(testJourneyId)
        verify(mockStorageService).retrievePostcode(testJourneyId)
        verify(mockStorageService).storeRegistrationStatus(testJourneyId, RegistrationNotCalled)

        verifyNoMoreInteractions(mockStorageService)

        verify(mockAuditService).auditJourney(testJourneyId, trustJourneyConfigWithoutBVCheck)
        
        verifyNoInteractions(mockBusinessVerificationService)
      }
    }
    "not create a BusinessVerificationJourney, not store BusinessVerificationStatus and return Cannot Confirm ErrorPage url" when {
      "TrustKnownFacts is one of DetailsNotFound, DetailsMismatch" in {
        List(DetailsNotFound, DetailsMismatch).foreach(knownFactsMatchFailure => {

          when(mockStorageService.retrieveUtr(testJourneyId)).thenReturn(Future.successful(Some(Sautr(testSautr))))
          when(mockStorageService.retrievePostcode(testJourneyId)).thenReturn(Future.successful(Some(testSaPostcode)))

          when(mockMatchingResultCalculator.matchKnownFacts(journeyId = testJourneyId,
            optUtr = Some(testSautr),
            optPostcode = Some(testSaPostcode))).thenReturn(Future.successful(knownFactsMatchFailure))

          when(mockStorageService.storeRegistrationStatus(testJourneyId, RegistrationNotCalled)).thenReturn(Future.successful(SuccessfullyStored))

          when(mockAuditService.auditJourney(testJourneyId, trustJourneyConfigWithoutBVCheck)).thenReturn(Future.successful(()))

          val result = await(
            TestSubmissionService.submit(journeyId = testJourneyId,
              journeyConfig = trustJourneyConfigWithoutBVCheck,
              matchingResultCalculator = mockMatchingResultCalculator,
              cannotConfirmErrorPageUrl = testCannotConfirmErrorPageUrl)
          )

          result mustBe testCannotConfirmErrorPageUrl

          verify(mockStorageService).retrieveUtr(testJourneyId)
          verify(mockStorageService).retrievePostcode(testJourneyId)
          verify(mockStorageService).storeRegistrationStatus(testJourneyId, RegistrationNotCalled)
          
          verifyNoMoreInteractions(mockStorageService)


          verifyNoInteractions(mockBusinessVerificationService)

          verify(mockAuditService).auditJourney(testJourneyId, trustJourneyConfigWithoutBVCheck)

          reset(mockStorageService, mockMatchingResultCalculator, mockBusinessVerificationService, mockAuditService, mockStorageService)
        })

      }
    }
  }

}

