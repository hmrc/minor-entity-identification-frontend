/*
 * Copyright 2024 HM Revenue & Customs
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

import org.mongodb.scala.result.InsertOneResult
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.minorentityidentificationfrontend.connectors.mocks.MockCreateJourneyConnector
import uk.gov.hmrc.minorentityidentificationfrontend.helpers.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.models.BusinessEntity.OverseasCompany
import uk.gov.hmrc.minorentityidentificationfrontend.repositories.mocks.MockJourneyConfigRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class JourneyServiceSpec
  extends AnyWordSpec
    with Matchers
    with MockCreateJourneyConnector
    with MockJourneyConfigRepository {

  object TestJourneyService extends JourneyService(mockCreateJourneyConnector, mockJourneyConfigRepository)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "createJourney" should {
    "return a journeyID and store the provided journey config" in {
      mockCreateJourneyConnector.createJourney()(any[HeaderCarrier]) returns Future.successful(testJourneyId)
      mockJourneyConfigRepository.insertJourneyConfig(
        eqTo(testJourneyId),
        eqTo(testInternalId),
        eqTo(testJourneyConfig(OverseasCompany))
      ) returns Future.successful(mock[InsertOneResult])

      val result = await(TestJourneyService.createJourney(testJourneyConfig(OverseasCompany), testInternalId))

      result mustBe testJourneyId
      verifyCreateJourney()
      verifyInsertJourneyConfig(testJourneyId, testInternalId, testJourneyConfig(OverseasCompany))
    }

    "throw an exception" when {
      "create journey API returns an invalid response" in {
        mockCreateJourneyConnector.createJourney()(any[HeaderCarrier]) returns
          Future.failed(new InternalServerException("Invalid response returned from create journey API"))

        intercept[InternalServerException](
          await(TestJourneyService.createJourney(testJourneyConfig(OverseasCompany), testInternalId))
        )
        verifyCreateJourney()
      }
    }
  }

  "getJourneyConfig" should {
    "return the journey config for a specific journey id" when {
      "the journey id exists in the database" in {
        mockJourneyConfigRepository.getJourneyConfig(testJourneyId, testInternalId) returns Future.successful(Some(testJourneyConfig(OverseasCompany)))

        val result = await(TestJourneyService.getJourneyConfig(testJourneyId, testInternalId))

        result mustBe testJourneyConfig(OverseasCompany)
        verifyGetJourneyConfig(testJourneyId, testInternalId)
      }
    }

    "throw an Internal Server Exception" when {
      "the journey config does not exist in the database" in {
        mockJourneyConfigRepository.getJourneyConfig(testJourneyId, testInternalId) returns Future.successful(None)

        intercept[InternalServerException](
          await(TestJourneyService.getJourneyConfig(testJourneyId, testInternalId))
        )
        verifyGetJourneyConfig(testJourneyId, testInternalId)
      }
    }
  }

}
