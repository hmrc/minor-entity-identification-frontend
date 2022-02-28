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

package uk.gov.hmrc.minorentityidentificationfrontend.stubs

import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.trustControllers.{routes => trustControllersRoutes}
import uk.gov.hmrc.minorentityidentificationfrontend.utils.WiremockMethods


trait BusinessVerificationStub extends WiremockMethods {

  def stubCreateBusinessVerificationJourney(sautr: String,
                                            journeyId: String,
                                            accessibilityUrl: String,
                                            regime: String
                                           )(status: Int,
                                             body: JsObject = Json.obj()): Unit = {

    val postBody = Json.obj("journeyType" -> "BUSINESS_VERIFICATION",
      "origin" -> regime,
      "identifiers" -> Json.arr(
        Json.obj(
          "saUtr" -> sautr
        )
      ),
      "continueUrl" -> trustControllersRoutes.BusinessVerificationController.retrieveBusinessVerificationResult(journeyId).url,
      "accessibilityStatementUrl" -> accessibilityUrl
    )

    when(method = POST, uri = "/business-verification/journey", postBody)
      .thenReturn(
        status = status,
        body = body
      )
  }

  def stubRetrieveBusinessVerificationResult(journeyId: String)
                                            (status: Int,
                                             body: JsObject = Json.obj()): Unit =
    when(method = GET, uri = s"/business-verification/journey/$journeyId/status")
      .thenReturn(
        status = status,
        body = body
      )

  def stubCreateBusinessVerificationJourneyFromStub(sautr: String,
                                                    journeyId: String,
                                                    accessibilityUrl: String,
                                                    regime: String
                                                   )(status: Int,
                                                     body: JsObject = Json.obj()): Unit = {

    val postBody = Json.obj("journeyType" -> "BUSINESS_VERIFICATION",
      "origin" -> regime,
      "identifiers" -> Json.arr(
        Json.obj(
          "saUtr" -> sautr
        )
      ),
      "continueUrl" -> trustControllersRoutes.BusinessVerificationController.retrieveBusinessVerificationResult(journeyId).url,
      "accessibilityStatementUrl" -> accessibilityUrl

    )

    when(method = POST, uri = "/identify-your-trust/test-only/business-verification/journey", postBody)
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

}

