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

package uk.gov.hmrc.minorentityidentificationfrontend.stubs

import play.api.libs.json.{JsObject, JsValue, Json}
import uk.gov.hmrc.minorentityidentificationfrontend.models._
import uk.gov.hmrc.minorentityidentificationfrontend.utils.WiremockMethods

trait MinorEntityIdentificationStub extends WiremockMethods {

  def stubStoreUtr(journeyId: String, utr: Utr)(status: Int) =
    when(method = PUT,
      uri = s"/identify-your-minor-entity-business/$journeyId/utrKey",
      body = Json.obj(
        "type" -> utr.utrType.toString,
        "value" -> utr.value
      )
    ).thenReturn(
      status = status
    )

  def stubRetrieveUtr(journeyId: String)(status: Int, body: JsObject = Json.obj()) =
    when(method = GET, uri = s"/identify-your-minor-entity-business/$journeyId/utrKey"
    ).thenReturn(
      status = status,
      body = body
    )

}
