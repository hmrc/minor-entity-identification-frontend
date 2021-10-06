/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.models.{UtrType, _}
import uk.gov.hmrc.minorentityidentificationfrontend.stubs.MinorEntityIdentificationStub
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ComponentSpecHelper

class MinorEntityIdentificationConnectorISpec extends ComponentSpecHelper with MinorEntityIdentificationStub {

  private val minorEntityIdentificationConnector = app.injector.instanceOf[MinorEntityIdentificationConnector]

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  val utrKey = "utrKey"
  val typeKey = "type"
  val sautrKey = "Sautr"
  val ctutrKey = "Ctutr"

  s"storeData($testJourneyId, $utrKey)" should {
    "return SuccessfullyStored" in {
      stubStoreUtr(testJourneyId, Utr(Sautr, testUtr))(status = OK)
      val result = await(minorEntityIdentificationConnector.storeDataField[Utr](
        testJourneyId, utrKey, Utr(Sautr, testUtr)))

      result mustBe SuccessfullyStored
    }
  }

  s"retrieveMinorEntityIdentification($testJourneyId)" should {
    "return utr" when {
      "there is utr stored against the journeyId" in {
        stubRetrieveUtr(testJourneyId)(
          status = OK,
          body = testUtrJson
        )

        val result = await(minorEntityIdentificationConnector.retrieveDataField[JsObject](testJourneyId, utrKey))

        result mustBe Some(testUtrJson)
      }
    }

    "return None" when {
      "there is no utr stored against the journeyId" in {
        stubRetrieveUtr(testJourneyId)(status = NOT_FOUND)

        val result = await(minorEntityIdentificationConnector.retrieveDataField[JsObject](testJourneyId, utrKey))

        result mustBe None
      }
    }
  }

  s"retrieveMinorEntityIdentification($testJourneyId, $utrKey)" should {
    "return utr" when {
      "the utr key is given and a full utr is stored against the journeyId" in {
        stubRetrieveUtr(testJourneyId)(OK, Json.toJsObject(Utr(Sautr, testUtr)))

        val result = await(minorEntityIdentificationConnector.retrieveDataField[JsObject](testJourneyId, utrKey))

        result mustBe Some(testUtrJson)
      }
    }

    "return None" when {
      "the utr key is given but there is no utr stored against the journeyId" in {
        stubRetrieveUtr(testJourneyId)(NOT_FOUND)
        val result = await(minorEntityIdentificationConnector.retrieveDataField[JsString](testJourneyId, utrKey))

        result mustBe None
      }
    }
  }
}