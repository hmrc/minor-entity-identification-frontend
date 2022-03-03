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
import uk.gov.hmrc.minorentityidentificationfrontend.models._
import uk.gov.hmrc.minorentityidentificationfrontend.services.mocks.{MockAuditService, MockStorageService, MockValidateTrustKnownFactsService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubmissionServiceSpec extends AnyWordSpec with Matchers with MockValidateTrustKnownFactsService with MockStorageService with MockAuditService {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  object TestSubmissionService extends SubmissionService(mockValidateTrustKnownFactsService, mockAuditService, mockStorageService)

  "submit" should {
    "return retry url" when {
      "TrustKnownFacts returns DetailsNotFound, DetailsMismatch or UnMatchableWithRetry" in {

        List(DetailsNotFound, DetailsMismatch, UnMatchableWithRetry).foreach(knownFactsMatchFailure => {

          mockStorageService.retrieveUtr(testJourneyId) returns Future.successful(Some(Sautr(testSautr)))
          mockStorageService.retrieveSaPostcode(testJourneyId) returns Future.successful(Some(testSaPostcode))
          mockStorageService.retrieveCHRN(testJourneyId) returns Future.successful(Some(testCHRN))

          mockValidateTrustKnownFactsService.validateTrustKnownFacts(journeyId = testJourneyId,
            optSaUtr = Some(testSautr),
            optSaPostcode = Some(testSaPostcode),
            optCHRN = Some(testCHRN)) returns Future.successful(knownFactsMatchFailure)

          mockAuditService.auditJourney(testJourneyId, testTrustJourneyConfig) returns Future.successful(())

          val result = await(TestSubmissionService.submit(testJourneyId, testTrustJourneyConfig))

          result mustBe errorRoutes.CannotConfirmBusinessController.show(testJourneyId).url

          reset(mockStorageService, mockValidateTrustKnownFactsService)

        })
      }
    }

    "return continue url" when {
      "match is SuccessfulMatch or match is UnMatchableWithoutRetry" in {

        List(SuccessfulMatch, UnMatchableWithoutRetry).foreach(knownFactsMatchResult => {

          mockStorageService.retrieveUtr(testJourneyId) returns Future.successful(Some(Sautr(testSautr)))
          mockStorageService.retrieveSaPostcode(testJourneyId) returns Future.successful(Some(testSaPostcode))
          mockStorageService.retrieveCHRN(testJourneyId) returns Future.successful(Some(testCHRN))

          mockValidateTrustKnownFactsService.validateTrustKnownFacts(journeyId = testJourneyId,
            optSaUtr = Some(testSautr),
            optSaPostcode = Some(testSaPostcode),
            optCHRN = Some(testCHRN)) returns Future.successful(knownFactsMatchResult)

          mockAuditService.auditJourney(testJourneyId, testTrustJourneyConfig)

          val result = await(TestSubmissionService.submit(testJourneyId, testTrustJourneyConfig))

          result mustBe testTrustJourneyConfig.continueUrl + s"?journeyId=$testJourneyId"

          reset(mockStorageService, mockValidateTrustKnownFactsService)
        })
      }
    }

    "delegates to the ValidateTrustKnownFactsService any value" in {

      List(Some("someValue"), None).foreach((someValue: Option[String]) => {

        mockStorageService.retrieveUtr(testJourneyId) returns Future.successful(someValue.map(Sautr))
        mockStorageService.retrieveSaPostcode(testJourneyId) returns Future.successful(someValue)
        mockStorageService.retrieveCHRN(testJourneyId) returns Future.successful(someValue)

        mockValidateTrustKnownFactsService.validateTrustKnownFacts(journeyId = testJourneyId,
          optSaUtr = someValue,
          optSaPostcode = someValue,
          optCHRN = someValue) returns Future.successful(UnMatchableWithoutRetry)

        mockAuditService.auditJourney(testJourneyId, testTrustJourneyConfig)

        val result = await(TestSubmissionService.submit(testJourneyId, testTrustJourneyConfig))

        result mustBe testTrustJourneyConfig.continueUrl + s"?journeyId=$testJourneyId"

      })


    }

  }

}
