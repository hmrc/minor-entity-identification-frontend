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
import uk.gov.hmrc.minorentityidentificationfrontend.helpers.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.httpparsers.StorageHttpParser.SuccessfullyStored
import uk.gov.hmrc.minorentityidentificationfrontend.models._
import uk.gov.hmrc.minorentityidentificationfrontend.services.mocks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubmissionServiceSpec extends AnyWordSpec
  with Matchers
  with MockStorageService
  with MockBusinessVerificationService
  with MockAuditService
  with MockRegistrationOrchestrationService
  with MockMatchingResultCalculator {

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
        mockStorageService.retrieveUtr(testJourneyId) returns Future.successful(Some(Sautr(testSautr)))
        mockStorageService.retrievePostcode(testJourneyId) returns Future.successful(Some(testSaPostcode))

        mockMatchingResultCalculator.matchKnownFacts(
          journeyId = testJourneyId,
          optUtr = Some(testSautr),
          optPostcode = Some(testSaPostcode)) returns Future.successful(SuccessfulMatch)

        mockBusinessVerificationService.createBusinessVerificationJourney(testJourneyId,
          testSautr,
          testTrustJourneyConfig()) returns Future.successful(Some(testBusinessVerificationRedirectUrl))

        val result = await(
          TestSubmissionService.submit(journeyId = testJourneyId,
            journeyConfig = testTrustJourneyConfig(),
            matchingResultCalculator = mockMatchingResultCalculator,
            cannotConfirmErrorPageUrl = testCannotConfirmErrorPageUrl)
        )

        result mustBe testBusinessVerificationRedirectUrl

        mockStorageService.storeRegistrationStatus(testJourneyId, RegistrationNotCalled) wasNever called

      }
    }
    "create a BusinessVerificationJourney and return the full journey continue url" when {
      "TrustKnownFacts is SuccessfulMatch but BV somehow fails." in {
        mockStorageService.retrieveUtr(testJourneyId) returns Future.successful(Some(Sautr(testSautr)))
        mockStorageService.retrievePostcode(testJourneyId) returns Future.successful(Some(testSaPostcode))

        mockMatchingResultCalculator.matchKnownFacts(
          journeyId = testJourneyId,
          optUtr = Some(testSautr),
          optPostcode = Some(testSaPostcode)) returns Future.successful(SuccessfulMatch)

        mockBusinessVerificationService.createBusinessVerificationJourney(testJourneyId,
          testSautr,
          testTrustJourneyConfig()) returns Future.successful(None)

        mockStorageService.storeRegistrationStatus(testJourneyId, RegistrationNotCalled) returns Future.successful(SuccessfullyStored)

        mockAuditService.auditJourney(testJourneyId, testTrustJourneyConfig()) returns Future.successful(())

        val result = await(
          TestSubmissionService.submit(journeyId = testJourneyId,
            journeyConfig = testTrustJourneyConfig(),
            matchingResultCalculator = mockMatchingResultCalculator,
            cannotConfirmErrorPageUrl = testCannotConfirmErrorPageUrl)
        )

        result mustBe testTrustJourneyConfig().fullContinueUrl(testJourneyId)

        mockStorageService.storeRegistrationStatus(testJourneyId, RegistrationNotCalled) was called
        mockAuditService.auditJourney(testJourneyId, testTrustJourneyConfig()) was called

      }
      "TrustKnownFacts is UnMatchable" in {

        mockStorageService.retrieveUtr(testJourneyId) returns Future.successful(Some(Sautr(testSautr)))
        mockStorageService.retrievePostcode(testJourneyId) returns Future.successful(Some(testSaPostcode))

        mockMatchingResultCalculator.matchKnownFacts(journeyId = testJourneyId,
          optUtr = Some(testSautr),
          optPostcode = Some(testSaPostcode)) returns Future.successful(UnMatchable)

        mockStorageService.storeBusinessVerificationStatus(testJourneyId, BusinessVerificationNotEnoughInformationToCallBV) returns Future.successful(SuccessfullyStored)

        mockStorageService.storeRegistrationStatus(testJourneyId, RegistrationNotCalled) returns Future.successful(SuccessfullyStored)

        mockAuditService.auditJourney(testJourneyId, testTrustJourneyConfig()) returns Future.successful(())

        val result = await(
          TestSubmissionService.submit(journeyId = testJourneyId,
            journeyConfig = testTrustJourneyConfig(),
            matchingResultCalculator = mockMatchingResultCalculator,
            cannotConfirmErrorPageUrl = testCannotConfirmErrorPageUrl)
        )

        result mustBe testTrustJourneyConfig().fullContinueUrl(testJourneyId)

        mockAuditService.auditJourney(testJourneyId, testTrustJourneyConfig()) was called
        mockStorageService.storeRegistrationStatus(testJourneyId, RegistrationNotCalled) was called
        mockBusinessVerificationService wasNever called
      }
    }
    "not create a BusinessVerificationJourney and return Cannot Confirm ErrorPage url" when {
      "TrustKnownFacts is one of DetailsNotFound, DetailsMismatch" in {

        List(DetailsNotFound, DetailsMismatch).foreach(knownFactsMatchFailure => {

          mockStorageService.retrieveUtr(testJourneyId) returns Future.successful(Some(Sautr(testSautr)))
          mockStorageService.retrievePostcode(testJourneyId) returns Future.successful(Some(testSaPostcode))

          mockMatchingResultCalculator.matchKnownFacts(journeyId = testJourneyId,
            optUtr = Some(testSautr),
            optPostcode = Some(testSaPostcode)) returns Future.successful(knownFactsMatchFailure)

          mockStorageService.storeBusinessVerificationStatus(testJourneyId, BusinessVerificationNotEnoughInformationToCallBV) returns Future.successful(SuccessfullyStored)

          mockStorageService.storeRegistrationStatus(testJourneyId, RegistrationNotCalled) returns Future.successful(SuccessfullyStored)

          mockAuditService.auditJourney(testJourneyId, testTrustJourneyConfig()) returns Future.successful(())

          val result = await(
            TestSubmissionService.submit(journeyId = testJourneyId,
              journeyConfig = testTrustJourneyConfig(),
              matchingResultCalculator = mockMatchingResultCalculator,
              cannotConfirmErrorPageUrl = testCannotConfirmErrorPageUrl)
          )

          result mustBe testCannotConfirmErrorPageUrl

          mockStorageService.storeRegistrationStatus(testJourneyId, RegistrationNotCalled) was called
          mockAuditService.auditJourney(testJourneyId, testTrustJourneyConfig()) was called
          mockBusinessVerificationService wasNever called

          reset(mockStorageService, mockBusinessVerificationService, mockAuditService)
        })

      }
    }
    "throw an exception" when {
      "SuccessfulMatch but SaUtr is not defined" in {
        mockStorageService.retrieveUtr(testJourneyId) returns Future.successful(None)
        mockStorageService.retrievePostcode(testJourneyId) returns Future.successful(Some(testSaPostcode))

        mockMatchingResultCalculator.matchKnownFacts(journeyId = testJourneyId,
          optUtr = None,
          optPostcode = Some(testSaPostcode)) returns Future.successful(SuccessfulMatch)

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
        mockStorageService.retrieveUtr(testJourneyId) returns Future.successful(Some(Sautr(testSautr)))
        mockStorageService.retrievePostcode(testJourneyId) returns Future.successful(Some(testSaPostcode))

        mockMatchingResultCalculator.matchKnownFacts(journeyId = testJourneyId,
          optUtr = Some(testSautr),
          optPostcode = Some(testSaPostcode)) returns Future.successful(SuccessfulMatch)

        mockRegistrationOrchestrationService.register(testJourneyId, Some(testSautr), trustJourneyConfigWithoutBVCheck) returns Future.successful(Registered(testSafeId))

        val result = await(
          TestSubmissionService.submit(journeyId = testJourneyId,
            journeyConfig = trustJourneyConfigWithoutBVCheck,
            matchingResultCalculator = mockMatchingResultCalculator,
            cannotConfirmErrorPageUrl = testCannotConfirmErrorPageUrl
          )
        )

        result mustBe testTrustJourneyConfig().fullContinueUrl(testJourneyId)

        mockStorageService.retrieveUtr(testJourneyId) was called
        mockStorageService.retrievePostcode(testJourneyId) was called

        mockRegistrationOrchestrationService.register(testJourneyId, Some(testSautr), trustJourneyConfigWithoutBVCheck) was called

        mockAuditService.auditJourney(testJourneyId, trustJourneyConfigWithoutBVCheck) wasNever called

        mockBusinessVerificationService wasNever called
        mockStorageService wasNever calledAgain
      }
      "TrustKnownFacts is UnMatchable" in {
        mockStorageService.retrieveUtr(testJourneyId) returns Future.successful(Some(Sautr(testSautr)))
        mockStorageService.retrievePostcode(testJourneyId) returns Future.successful(Some(testSaPostcode))

        mockMatchingResultCalculator.matchKnownFacts(journeyId = testJourneyId,
          optUtr = Some(testSautr),
          optPostcode = Some(testSaPostcode)) returns Future.successful(UnMatchable)

        mockStorageService.storeRegistrationStatus(testJourneyId, RegistrationNotCalled) returns Future.successful(SuccessfullyStored)

        mockAuditService.auditJourney(testJourneyId, trustJourneyConfigWithoutBVCheck) returns Future.successful(())

        val result = await(
          TestSubmissionService.submit(journeyId = testJourneyId,
            journeyConfig = trustJourneyConfigWithoutBVCheck,
            matchingResultCalculator = mockMatchingResultCalculator,
            cannotConfirmErrorPageUrl = testCannotConfirmErrorPageUrl
          )
        )

        result mustBe testTrustJourneyConfig().fullContinueUrl(testJourneyId)

        mockStorageService.retrieveUtr(testJourneyId) was called
        mockStorageService.retrievePostcode(testJourneyId) was called
        mockStorageService.storeRegistrationStatus(testJourneyId, RegistrationNotCalled) was called

        mockAuditService.auditJourney(testJourneyId, trustJourneyConfigWithoutBVCheck) was called

        mockStorageService wasNever calledAgain
        mockBusinessVerificationService wasNever called
      }
    }
    "not create a BusinessVerificationJourney, not store BusinessVerificationStatus and return Cannot Confirm ErrorPage url" when {
      "TrustKnownFacts is one of DetailsNotFound, DetailsMismatch" in {
        List(DetailsNotFound, DetailsMismatch).foreach(knownFactsMatchFailure => {

          mockStorageService.retrieveUtr(testJourneyId) returns Future.successful(Some(Sautr(testSautr)))
          mockStorageService.retrievePostcode(testJourneyId) returns Future.successful(Some(testSaPostcode))

          mockMatchingResultCalculator.matchKnownFacts(journeyId = testJourneyId,
            optUtr = Some(testSautr),
            optPostcode = Some(testSaPostcode)) returns Future.successful(knownFactsMatchFailure)

          mockStorageService.storeRegistrationStatus(testJourneyId, RegistrationNotCalled) returns Future.successful(SuccessfullyStored)

          mockAuditService.auditJourney(testJourneyId, trustJourneyConfigWithoutBVCheck) returns Future.successful(())

          val result = await(
            TestSubmissionService.submit(journeyId = testJourneyId,
              journeyConfig = trustJourneyConfigWithoutBVCheck,
              matchingResultCalculator = mockMatchingResultCalculator,
              cannotConfirmErrorPageUrl = testCannotConfirmErrorPageUrl)
          )

          result mustBe testCannotConfirmErrorPageUrl

          mockStorageService.retrieveUtr(testJourneyId) was called
          mockStorageService.retrievePostcode(testJourneyId) was called
          mockStorageService.storeRegistrationStatus(testJourneyId, RegistrationNotCalled) was called

          mockStorageService wasNever calledAgain
          mockBusinessVerificationService wasNever called

          mockAuditService.auditJourney(testJourneyId, trustJourneyConfigWithoutBVCheck) was called

          reset(mockStorageService, mockMatchingResultCalculator, mockBusinessVerificationService, mockAuditService, mockStorageService)
        })

      }
    }
  }

}

