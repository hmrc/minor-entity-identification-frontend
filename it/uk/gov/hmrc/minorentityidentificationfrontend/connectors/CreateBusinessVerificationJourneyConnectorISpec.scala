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

package uk.gov.hmrc.minorentityidentificationfrontend.connectors

import play.api.http.Status.FORBIDDEN
import play.api.libs.json.Json
import play.api.test.Helpers.{CREATED, NOT_FOUND, await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.connectors.CreateBusinessVerificationJourneyConnector.{BusinessVerificationJourneyCreated, NotEnoughEvidence, UserLockedOut}
import uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.core.config.{BusinessVerificationStub, EnableFullTrustJourney, FeatureSwitching}
import uk.gov.hmrc.minorentityidentificationfrontend.stubs.BusinessVerificationStub
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ComponentSpecHelper

class CreateBusinessVerificationJourneyConnectorISpec extends ComponentSpecHelper with BusinessVerificationStub with FeatureSwitching {
  private lazy val createBusinessVerificationJourneyConnector = app.injector.instanceOf[CreateBusinessVerificationJourneyConnector]

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  "createBusinessVerificationJourneyConnector" when {
    s"the $BusinessVerificationStub feature switch is enabled" should {
      "return the redirectUri and therefore no BV status" when {
        "the journey creation has been successful" in {
          enable(BusinessVerificationStub)
          enable(EnableFullTrustJourney)
          stubCreateBusinessVerificationJourneyFromStub(testCreateBusinessVerificationJourneyJson(testSautr, testJourneyId, testTrustsJourneyConfig(true)))(CREATED, Json.obj("redirectUri" -> testContinueUrl))

          val result = await(createBusinessVerificationJourneyConnector.createBusinessVerificationJourney(testJourneyId, testSautr, testTrustsJourneyConfig(true)))

          result mustBe Right(BusinessVerificationJourneyCreated(testContinueUrl))
        }

      }
      "return no redirect URL and an appropriate BV status" when {
        "the journey creation has been unsuccessful because BV cannot find the record" in {
          enable(BusinessVerificationStub)
          enable(EnableFullTrustJourney)

          stubCreateBusinessVerificationJourneyFromStub(testCreateBusinessVerificationJourneyJson(testSautr, testJourneyId, testTrustsJourneyConfig(true)))(NOT_FOUND, Json.obj())

          val result = await(createBusinessVerificationJourneyConnector.createBusinessVerificationJourney(testJourneyId, testSautr, testTrustsJourneyConfig(true)))

          result mustBe Left(NotEnoughEvidence)
        }

        "the journey creation has been unsuccessful because the user has had too many attempts and is logged out" in {
          enable(BusinessVerificationStub)
          enable(EnableFullTrustJourney)
          stubCreateBusinessVerificationJourneyFromStub(testCreateBusinessVerificationJourneyJson(testSautr, testJourneyId, testTrustsJourneyConfig(true)))(FORBIDDEN, Json.obj())

          val result = await(createBusinessVerificationJourneyConnector.createBusinessVerificationJourney(testJourneyId, testSautr, testTrustsJourneyConfig(true)))

          result mustBe Left(UserLockedOut)
        }
      }
    }
    s"the $BusinessVerificationStub feature switch is disabled" should {
      "return the redirectUri and therefore no BV status" when {
        "the journey creation has been successful" in {
          disable(BusinessVerificationStub)
          enable(EnableFullTrustJourney)
          stubCreateBusinessVerificationJourney(expBody = testCreateBusinessVerificationJourneyJson(testSautr, testJourneyId, testTrustsJourneyConfig(true)))(CREATED, Json.obj("redirectUri" -> testContinueUrl))

          val result = await(createBusinessVerificationJourneyConnector.createBusinessVerificationJourney(testJourneyId, testSautr, testTrustsJourneyConfig(true)))

          result mustBe Right(BusinessVerificationJourneyCreated(testContinueUrl))
        }

      }
      "return no redirect URL and an appropriate BV status" when {
        "the journey creation has been unsuccessful because BV cannot find the record" in {
          disable(BusinessVerificationStub)
          enable(EnableFullTrustJourney)
          stubCreateBusinessVerificationJourney(expBody = testCreateBusinessVerificationJourneyJson(testSautr, testJourneyId, testTrustsJourneyConfig(true)))(NOT_FOUND, Json.obj())

          val result = await(createBusinessVerificationJourneyConnector.createBusinessVerificationJourney(testJourneyId, testSautr, testTrustsJourneyConfig(true)))

          result mustBe Left(NotEnoughEvidence)
        }
        "the journey creation has been unsuccessful because the user has had too many attempts and is logged out" in {
          disable(BusinessVerificationStub)
          enable(EnableFullTrustJourney)
          stubCreateBusinessVerificationJourney(expBody = testCreateBusinessVerificationJourneyJson(testSautr, testJourneyId, testTrustsJourneyConfig(true)))(FORBIDDEN, Json.obj())

          val result = await(createBusinessVerificationJourneyConnector.createBusinessVerificationJourney(testJourneyId, testSautr, testTrustsJourneyConfig(true)))

          result mustBe Left(UserLockedOut)
        }
      }
    }
  }

}
