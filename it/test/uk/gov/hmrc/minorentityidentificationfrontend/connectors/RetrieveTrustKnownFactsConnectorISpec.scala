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

package test.uk.gov.hmrc.minorentityidentificationfrontend.connectors

import play.api.http.Status.{BAD_REQUEST, NOT_FOUND, OK}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants.{testKnownFactsJson, testSautr, testTrustKnownFactsResponse}
import uk.gov.hmrc.minorentityidentificationfrontend.connectors.RetrieveTrustKnownFactsConnector
import uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.core.config.{FeatureSwitching, TrustVerificationStub}
import uk.gov.hmrc.minorentityidentificationfrontend.stubs.RetrieveTrustKnownFactsStub
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ComponentSpecHelper

class RetrieveTrustKnownFactsConnectorISpec extends ComponentSpecHelper with RetrieveTrustKnownFactsStub with FeatureSwitching {

  private val retrieveTrustKnownFactsConnector = app.injector.instanceOf[RetrieveTrustKnownFactsConnector]

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  "retrieveTrustKnownFacts" should {
    "return Trust Known Facts" when {
      "the TrustVerificationStub feature switch is off" in {
        disable(TrustVerificationStub)
        stubRetrieveTrustKnownFacts(testSautr)(OK, testKnownFactsJson)
        val result = await(retrieveTrustKnownFactsConnector.retrieveTrustKnownFacts(testSautr))

        result mustBe Some(testTrustKnownFactsResponse)
      }
      "the TrustVerificationStub feature switch is on" in {
        enable(TrustVerificationStub)
        stubRetrieveTrustKnownFactsFromStub(testSautr)(OK, testKnownFactsJson)

        val result = await(retrieveTrustKnownFactsConnector.retrieveTrustKnownFacts(testSautr))

        result mustBe Some(testTrustKnownFactsResponse)
      }
    }
    "return None" when {
      "the Trust proxy endpoint returns 404" in {
        disable(TrustVerificationStub)
        stubRetrieveTrustKnownFacts(testSautr)(NOT_FOUND)

        val result = await(retrieveTrustKnownFactsConnector.retrieveTrustKnownFacts(testSautr))

        result mustBe None
      }
    }
    "throw an Internal Server Exception" in {
      disable(TrustVerificationStub)
      stubRetrieveTrustKnownFacts(testSautr)(BAD_REQUEST)

      intercept[InternalServerException](await(retrieveTrustKnownFactsConnector.retrieveTrustKnownFacts(testSautr)))
    }
  }
}
