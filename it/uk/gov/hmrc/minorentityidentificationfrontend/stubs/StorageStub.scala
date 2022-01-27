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

import play.api.libs.json.{JsObject, JsValue, Json}
import uk.gov.hmrc.minorentityidentificationfrontend.models._
import uk.gov.hmrc.minorentityidentificationfrontend.utils.WiremockMethods

trait StorageStub extends WiremockMethods {

  def stubStoreUtr(journeyId: String, utr: Utr)(status: Int): Unit =
    when(method = PUT,
      uri = s"/minor-entity-identification/journey/$journeyId/utr",
      body = Json.obj(
        "type" -> utr.utrType,
        "value" -> utr.value
      )
    ).thenReturn(
      status = status
    )

  def stubRetrieveUtr(journeyId: String)(status: Int, body: JsObject = Json.obj()): Unit =
    when(method = GET, uri = s"/minor-entity-identification/journey/$journeyId/utr"
    ).thenReturn(
      status = status,
      body = body
    )

  def stubRemoveUtr(journeyId: String)(status: Int, body: JsObject = Json.obj()): Unit =
    when(method = DELETE, uri = s"/minor-entity-identification/journey/$journeyId/utr"
    ).thenReturn(
      status = status,
      body = body
    )

  def stubStoreOverseasTaxIdentifiers(journeyId: String, taxIdentifiers: Overseas)(status: Int): Unit =
    when(method = PUT,
      uri = s"/minor-entity-identification/journey/$journeyId/overseas", body = Json.toJson(taxIdentifiers)
    ).thenReturn(
      status = status
    )

  def stubRetrieveOverseasTaxIdentifiers(journeyId: String)(status: Int, body: JsValue = Json.obj()): Unit =
    when(method = GET,
      uri = s"/minor-entity-identification/journey/$journeyId/overseas"
    ).thenReturn(
      status = status,
      body = body
    )

  def stubRemoveOverseasTaxIdentifiers(journeyId: String)(status: Int, body: String = ""): Unit =
    when(method = DELETE,
      uri = s"/minor-entity-identification/journey/$journeyId/overseas"
    ).thenReturn(
      status = status,
      body = body
    )

}
