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

import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.Json
import play.api.test.Helpers.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK, await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants.{testCtutr, testPostcode}
import uk.gov.hmrc.minorentityidentificationfrontend.models.{DetailsMismatch, DetailsNotFound, KnownFactsMatchingResult, SuccessfulMatch}
import uk.gov.hmrc.minorentityidentificationfrontend.stubs.ValidateUnincorporatedAssociationDetailsConnectorStub
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ComponentSpecHelper

import scala.concurrent.Future

class ValidateUnincorporatedAssociationDetailsConnectorISpec
  extends ComponentSpecHelper with ScalaFutures with ValidateUnincorporatedAssociationDetailsConnectorStub {

  private val validateUADetailsConnector: ValidateUnincorporatedAssociationDetailsConnector =
    app.injector.instanceOf[ValidateUnincorporatedAssociationDetailsConnector]

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  "validate unincorporated association details connector" should {

    "return details match" in {

      stubValidateUnincorporatedAssociationDetails(testCtutr, testPostcode)(OK, Json.obj("matched" -> true))

      val result: KnownFactsMatchingResult =
        await(validateUADetailsConnector.validateUnincorporatedAssociationDetails(testCtutr, testPostcode))

      result mustBe SuccessfulMatch
    }

    "return details do not match" in {

      stubValidateUnincorporatedAssociationDetails(testCtutr, testPostcode)(OK, Json.obj("matched" -> false))

      val result: KnownFactsMatchingResult =
        await(validateUADetailsConnector.validateUnincorporatedAssociationDetails(testCtutr, testPostcode))

      result mustBe DetailsMismatch

    }

    "return details not found" in {

      stubValidateUnincorporatedAssociationDetails(testCtutr, testPostcode)(BAD_REQUEST, Json.obj("code" -> "NOT_FOUND",
        "reason" -> "The back end has indicated that CT UTR cannot be returned"
      ))

      val result: KnownFactsMatchingResult =
        await(validateUADetailsConnector.validateUnincorporatedAssociationDetails(testCtutr, testPostcode))

      result mustBe DetailsNotFound

    }

    "throw an exception for an unknown response" in {

      stubValidateUnincorporatedAssociationDetails(testCtutr, testPostcode)(INTERNAL_SERVER_ERROR, Json.obj("error" -> "testError"))

      val result: Future[KnownFactsMatchingResult] =
        validateUADetailsConnector.validateUnincorporatedAssociationDetails(testCtutr, testPostcode)

      whenReady(result.failed) { ex =>
        ex.getMessage mustBe """Invalid response from validate unincorporated association details: 500: {"error":"testError"}"""
      }

    }

  }

}
