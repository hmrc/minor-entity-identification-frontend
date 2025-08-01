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

package uk.gov.hmrc.minorentityidentificationfrontend.stubs

import play.api.libs.json.{JsObject, Json}

import uk.gov.hmrc.minorentityidentificationfrontend.utils.WiremockMethods

trait ValidateUnincorporatedAssociationDetailsConnectorStub extends WiremockMethods {

  def stubValidateUnincorporatedAssociationDetails(ctUtr: String, postcode: String)
                                                  (status: Int, body: JsObject = Json.obj()): Unit = {

    val requestBody: JsObject = Json.obj(
      "ctutr" -> ctUtr,
      "postcode" -> postcode
    )

    when(method = POST, uri = "/minor-entity-identification/validate-details", requestBody)
      .thenReturn(
        status = status,
        body = body
      )
  }

}
