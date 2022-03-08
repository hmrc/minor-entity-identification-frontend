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

import play.api.libs.json.Json
import uk.gov.hmrc.minorentityidentificationfrontend.models.RegistrationStatus
import uk.gov.hmrc.minorentityidentificationfrontend.utils.{WiremockHelper, WiremockMethods}

trait RegisterStub extends WiremockMethods {

  def stubRegister(sautr: String, regime: String)(status: Int, body: RegistrationStatus): Unit = {
    val jsonBody = Json.obj(
      "trust" -> Json.obj(
        "sautr" -> sautr.toUpperCase,
        "regime" -> regime
      ))

    when(method = POST, uri = "/minor-entity-identification/register", jsonBody)
      .thenReturn(
        status = status,
        body = Json.obj("registration" -> body)
      )
  }

  def verifyRegister(sautr: String, regime: String): Unit = {
    val jsonBody = Json.obj(
      "trust" -> Json.obj(
        "sautr" -> sautr.toUpperCase,
        "regime" -> regime
      ))

    WiremockHelper.verifyPost(uri = "/minor-entity-identification/register", optBody = Some(jsonBody.toString()))
  }

}
