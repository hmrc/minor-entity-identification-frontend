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

package uk.gov.hmrc.minorentityidentificationfrontend.stubs

import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.minorentityidentificationfrontend.utils.{WiremockHelper, WiremockMethods}

trait BusinessVerificationStub extends WiremockMethods {

  def stubCreateBusinessVerificationJourney(expBody: JsObject)(status: Int,
                                                               body: JsObject = Json.obj()): Unit =
    when(method = POST, uri = "/business-verification/journey", body = expBody)
      .thenReturn(
        status = status,
        body = body
      )

  def stubRetrieveBusinessVerificationResult(journeyId: String)
                                            (status: Int,
                                             body: JsObject = Json.obj()): Unit =
    when(method = GET, uri = s"/business-verification/journey/$journeyId/status")
      .thenReturn(
        status = status,
        body = body
      )

  def stubCreateBusinessVerificationJourneyFromStub(expBody: JsObject)(status: Int,
                                                                       body: JsObject = Json.obj()): Unit = {
    when(method = POST, uri = "/identify-your-trust/test-only/business-verification/journey", expBody)
      .thenReturn(
        status = status,
        body = body
      )
  }

  def stubRetrieveBusinessVerificationResultFromStub(journeyId: String)
                                                    (status: Int,
                                                     body: JsObject = Json.obj()): Unit =
    when(method = GET, uri = s"/identify-your-trust/test-only/business-verification/journey/$journeyId/status")
      .thenReturn(
        status = status,
        body = body
      )

  def verifyCreateBusinessVerificationJourneyFromStub(expBody: JsObject): Unit =
    WiremockHelper.verifyPost(
      uri = "/identify-your-trust/test-only/business-verification/journey",
      optBody = Some(Json.stringify(expBody))
    )

  def verifyCreateBusinessVerificationJourney(expBody: JsObject): Unit =
    WiremockHelper.verifyPost(
      uri = s"/business-verification/journey",
      optBody = Some(Json.stringify(expBody))
    )

}
