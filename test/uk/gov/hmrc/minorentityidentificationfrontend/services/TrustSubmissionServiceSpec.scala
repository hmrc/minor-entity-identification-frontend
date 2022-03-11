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
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.trustControllers.errorControllers.{routes => errorRoutes}
import uk.gov.hmrc.minorentityidentificationfrontend.helpers.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.httpparsers.StorageHttpParser.SuccessfullyStored
import uk.gov.hmrc.minorentityidentificationfrontend.models._
import uk.gov.hmrc.minorentityidentificationfrontend.services.mocks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TrustSubmissionServiceSpec
  extends AnyWordSpec
    with Matchers
    with MockValidateTrustKnownFactsService
    with MockStorageService
    with MockBusinessVerificationService
    with MockAuditService
    with MockRegistrationOrchestrationService {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  object TestSubmissionService extends TrustSubmissionService(mockValidateTrustKnownFactsService,
    mockStorageService,
    mockAuditService,
    mockBusinessVerificationService,
    mockRegistrationOrchestrationService)

  "given businessVerificationCheck is true, submit" should {
    "create a BusinessVerificationJourney and return the businessVerificationUrl" when {
      "TrustKnownFacts is SuccessfulMatch and BV creates a businessVerificationUrl" in {
        mockStorageService.retrieveUtr(testJourneyId) returns Future.successful(Some(Sautr(testSautr)))
        mockStorageService.retrievePostcode(testJourneyId) returns Future.successful(Some(testSaPostcode))
        mockStorageService.retrieveCHRN(testJourneyId) returns Future.successful(Some(testCHRN))

        mockValidateTrustKnownFactsService.validateTrustKnownFacts(journeyId = testJourneyId,
          optSaUtr = Some(testSautr),
          optSaPostcode = Some(testSaPostcode),
          optCHRN = Some(testCHRN)) returns Future.successful(SuccessfulMatch)

        mockBusinessVerificationService.createBusinessVerificationJourney(testJourneyId,
          testSautr,
          testTrustJourneyConfig()) returns Future.successful(Some(testBusinessVerificationRedirectUrl))

        val result = await(TestSubmissionService.submit(testJourneyId, testTrustJourneyConfig()))

        result mustBe testBusinessVerificationRedirectUrl
      }
    }
    "create a BusinessVerificationJourney and return the full journey continue url" when {
      "TrustKnownFacts is SuccessfulMatch but BV somehow fails" in {
        mockStorageService.retrieveUtr(testJourneyId) returns Future.successful(Some(Sautr(testSautr)))
        mockStorageService.retrievePostcode(testJourneyId) returns Future.successful(Some(testSaPostcode))
        mockStorageService.retrieveCHRN(testJourneyId) returns Future.successful(Some(testCHRN))

        mockValidateTrustKnownFactsService.validateTrustKnownFacts(journeyId = testJourneyId,
          optSaUtr = Some(testSautr),
          optSaPostcode = Some(testSaPostcode),
          optCHRN = Some(testCHRN)) returns Future.successful(SuccessfulMatch)

        mockBusinessVerificationService.createBusinessVerificationJourney(testJourneyId,
          testSautr,
          testTrustJourneyConfig()) returns Future.successful(None)

        mockAuditService.auditJourney(testJourneyId, testTrustJourneyConfig()) returns Future.successful(())

        val result = await(TestSubmissionService.submit(testJourneyId, testTrustJourneyConfig()))

        result mustBe testTrustJourneyConfig().fullContinueUrl(testJourneyId)

        mockAuditService.auditJourney(testJourneyId, testTrustJourneyConfig()) was called
      }
      "TrustKnownFacts is UnMatchable" in {

        mockStorageService.retrieveUtr(testJourneyId) returns Future.successful(Some(Sautr(testSautr)))
        mockStorageService.retrievePostcode(testJourneyId) returns Future.successful(Some(testSaPostcode))
        mockStorageService.retrieveCHRN(testJourneyId) returns Future.successful(Some(testCHRN))

        mockValidateTrustKnownFactsService.validateTrustKnownFacts(journeyId = testJourneyId,
          optSaUtr = Some(testSautr),
          optSaPostcode = Some(testSaPostcode),
          optCHRN = Some(testCHRN)) returns Future.successful(UnMatchable)

        mockStorageService.storeBusinessVerificationStatus(testJourneyId, BusinessVerificationNotEnoughInformationToCallBV) returns Future.successful(SuccessfullyStored)

        mockAuditService.auditJourney(testJourneyId, testTrustJourneyConfig()) returns Future.successful(())

        val result = await(TestSubmissionService.submit(testJourneyId, testTrustJourneyConfig()))

        result mustBe testTrustJourneyConfig().fullContinueUrl(testJourneyId)

        mockAuditService.auditJourney(testJourneyId, testTrustJourneyConfig()) was called
        mockBusinessVerificationService wasNever called
      }
    }
    "not create a BusinessVerificationJourney and return Cannot ConfirmBusiness Controller url" when {
      "TrustKnownFacts is one of DetailsNotFound, DetailsMismatch" in {

        List(DetailsNotFound, DetailsMismatch).foreach(knownFactsMatchFailure => {

          mockStorageService.retrieveUtr(testJourneyId) returns Future.successful(Some(Sautr(testSautr)))
          mockStorageService.retrievePostcode(testJourneyId) returns Future.successful(Some(testSaPostcode))
          mockStorageService.retrieveCHRN(testJourneyId) returns Future.successful(Some(testCHRN))

          mockValidateTrustKnownFactsService.validateTrustKnownFacts(journeyId = testJourneyId,
            optSaUtr = Some(testSautr),
            optSaPostcode = Some(testSaPostcode),
            optCHRN = Some(testCHRN)) returns Future.successful(knownFactsMatchFailure)

          mockStorageService.storeBusinessVerificationStatus(testJourneyId, BusinessVerificationNotEnoughInformationToCallBV) returns Future.successful(SuccessfullyStored)

          mockAuditService.auditJourney(testJourneyId, testTrustJourneyConfig()) returns Future.successful(())

          val result = await(TestSubmissionService.submit(testJourneyId, testTrustJourneyConfig()))

          result mustBe errorRoutes.CannotConfirmBusinessController.show(testJourneyId).url

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
        mockStorageService.retrieveCHRN(testJourneyId) returns Future.successful(Some(testCHRN))

        mockValidateTrustKnownFactsService.validateTrustKnownFacts(journeyId = testJourneyId,
          optSaUtr = None,
          optSaPostcode = Some(testSaPostcode),
          optCHRN = Some(testCHRN)) returns Future.successful(SuccessfulMatch)

        val theActualException: IllegalStateException = intercept[IllegalStateException] {
          await(TestSubmissionService.submit(testJourneyId, testTrustJourneyConfig()))
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
        mockStorageService.retrieveCHRN(testJourneyId) returns Future.successful(Some(testCHRN))

        mockValidateTrustKnownFactsService.validateTrustKnownFacts(journeyId = testJourneyId,
          optSaUtr = Some(testSautr),
          optSaPostcode = Some(testSaPostcode),
          optCHRN = Some(testCHRN)) returns Future.successful(SuccessfulMatch)

        mockRegistrationOrchestrationService.register(testJourneyId, Some(testSautr), trustJourneyConfigWithoutBVCheck) returns Future.successful(Registered(testSafeId))

        mockAuditService.auditJourney(testJourneyId, trustJourneyConfigWithoutBVCheck) returns Future.successful(())

        val result = await(TestSubmissionService.submit(testJourneyId, trustJourneyConfigWithoutBVCheck))

        result mustBe testTrustJourneyConfig().fullContinueUrl(testJourneyId)

        mockStorageService.retrieveUtr(testJourneyId) was called
        mockStorageService.retrievePostcode(testJourneyId) was called
        mockStorageService.retrieveCHRN(testJourneyId) was called

        mockRegistrationOrchestrationService.register(testJourneyId, Some(testSautr), trustJourneyConfigWithoutBVCheck) was called

        mockAuditService.auditJourney(testJourneyId, trustJourneyConfigWithoutBVCheck) was called

        mockBusinessVerificationService wasNever called
        mockStorageService wasNever calledAgain
      }
      "TrustKnownFacts is UnMatchable" in {
        mockStorageService.retrieveUtr(testJourneyId) returns Future.successful(Some(Sautr(testSautr)))
        mockStorageService.retrievePostcode(testJourneyId) returns Future.successful(Some(testSaPostcode))
        mockStorageService.retrieveCHRN(testJourneyId) returns Future.successful(Some(testCHRN))

        mockValidateTrustKnownFactsService.validateTrustKnownFacts(journeyId = testJourneyId,
          optSaUtr = Some(testSautr),
          optSaPostcode = Some(testSaPostcode),
          optCHRN = Some(testCHRN)) returns Future.successful(UnMatchable)

        mockAuditService.auditJourney(testJourneyId, trustJourneyConfigWithoutBVCheck) returns Future.successful(())

        val result = await(TestSubmissionService.submit(testJourneyId, trustJourneyConfigWithoutBVCheck))

        result mustBe testTrustJourneyConfig().fullContinueUrl(testJourneyId)

        mockStorageService.retrieveUtr(testJourneyId) was called
        mockStorageService.retrievePostcode(testJourneyId) was called
        mockStorageService.retrieveCHRN(testJourneyId) was called

        mockAuditService.auditJourney(testJourneyId, trustJourneyConfigWithoutBVCheck) was called
        mockStorageService wasNever calledAgain
        mockBusinessVerificationService wasNever called
      }
    }
    "not create a BusinessVerificationJourney, not store BusinessVerificationStatus and return Cannot ConfirmBusiness Controller url" when {
      "TrustKnownFacts is one of DetailsNotFound, DetailsMismatch" in {
        List(DetailsNotFound, DetailsMismatch).foreach(knownFactsMatchFailure => {

          mockStorageService.retrieveUtr(testJourneyId) returns Future.successful(Some(Sautr(testSautr)))
          mockStorageService.retrievePostcode(testJourneyId) returns Future.successful(Some(testSaPostcode))
          mockStorageService.retrieveCHRN(testJourneyId) returns Future.successful(Some(testCHRN))

          mockValidateTrustKnownFactsService.validateTrustKnownFacts(journeyId = testJourneyId,
            optSaUtr = Some(testSautr),
            optSaPostcode = Some(testSaPostcode),
            optCHRN = Some(testCHRN)) returns Future.successful(knownFactsMatchFailure)

          mockAuditService.auditJourney(testJourneyId, trustJourneyConfigWithoutBVCheck) returns Future.successful(())

          val result = await(TestSubmissionService.submit(testJourneyId, trustJourneyConfigWithoutBVCheck))

          result mustBe errorRoutes.CannotConfirmBusinessController.show(testJourneyId).url

          mockStorageService.retrieveUtr(testJourneyId) was called
          mockStorageService.retrievePostcode(testJourneyId) was called
          mockStorageService.retrieveCHRN(testJourneyId) was called

          mockStorageService wasNever calledAgain
          mockBusinessVerificationService wasNever called
          mockAuditService.auditJourney(testJourneyId, trustJourneyConfigWithoutBVCheck) was called

          reset(mockStorageService, mockBusinessVerificationService, mockAuditService)
        })

      }
    }
    "not create a BusinessVerificationJourney, not store BusinessVerificationStatus and return the full journey continue url" when {
      "TrustKnownFacts is Unmatchable" in {
        mockStorageService.retrieveUtr(testJourneyId) returns Future.successful(Some(Sautr(testSautr)))
        mockStorageService.retrievePostcode(testJourneyId) returns Future.successful(Some(testSaPostcode))
        mockStorageService.retrieveCHRN(testJourneyId) returns Future.successful(Some(testCHRN))

        mockValidateTrustKnownFactsService.validateTrustKnownFacts(journeyId = testJourneyId,
          optSaUtr = Some(testSautr),
          optSaPostcode = Some(testSaPostcode),
          optCHRN = Some(testCHRN)) returns Future.successful(UnMatchable)

        mockAuditService.auditJourney(testJourneyId, trustJourneyConfigWithoutBVCheck) returns Future.successful(())

        val result = await(TestSubmissionService.submit(testJourneyId, trustJourneyConfigWithoutBVCheck))

        result mustBe testTrustJourneyConfig().fullContinueUrl(testJourneyId)

        mockStorageService.retrieveUtr(testJourneyId) was called
        mockStorageService.retrievePostcode(testJourneyId) was called
        mockStorageService.retrieveCHRN(testJourneyId) was called

        mockStorageService wasNever calledAgain
        mockBusinessVerificationService wasNever called
        mockAuditService.auditJourney(testJourneyId, trustJourneyConfigWithoutBVCheck) was called

        reset(mockStorageService, mockBusinessVerificationService, mockAuditService)
      }

    }
  }

}

