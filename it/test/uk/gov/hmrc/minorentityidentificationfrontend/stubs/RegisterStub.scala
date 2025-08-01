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
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants.testRegisterUAJson
import uk.gov.hmrc.minorentityidentificationfrontend.models.BusinessEntity.{OverseasCompany, Trusts, UnincorporatedAssociation}
import uk.gov.hmrc.minorentityidentificationfrontend.models.JourneyConfig
import uk.gov.hmrc.minorentityidentificationfrontend.utils.{WiremockHelper, WiremockMethods}

trait RegisterStub extends WiremockMethods {

  def stubRegister(utr: String, journeyConfig: JourneyConfig)(status: Int, body: JsObject): Unit = {
    journeyConfig.businessEntity match {
      case Trusts => stubRegisterTrust(utr, journeyConfig.regime)(status, body)
      case UnincorporatedAssociation => stubRegisterUA(utr, journeyConfig.regime)(status, body)
      case OverseasCompany => throw new IllegalArgumentException("Overseas Company is not supported for registration")
    }
  }

  def stubRegisterTrust(sautr: String, regime: String)(status: Int, body: JsObject): Unit = {
    val jsonBody = Json.obj(
      "sautr" -> sautr.toUpperCase,
      "regime" -> regime
    )

    when(method = POST, uri = "/minor-entity-identification/register-trust", jsonBody)
      .thenReturn(
        status = status,
        body = body
      )
  }

  def stubRegisterUA(ctutr: String, regime: String)(status: Int, body: JsObject): Unit =
    when(method = POST, uri = "/minor-entity-identification/register-ua", testRegisterUAJson(ctutr.toUpperCase, regime))
      .thenReturn(
        status = status,
        body = body
      )

  def verifyRegisterTrust(sautr: String, regime: String): Unit = {
    val jsonBody = Json.obj(
      "sautr" -> sautr.toUpperCase,
      "regime" -> regime
    )

    WiremockHelper.verifyPost(uri = "/minor-entity-identification/register-trust", optBody = Some(jsonBody.toString()))
  }

  def verifyRegisterUA(jsonBody: JsObject): Unit =
    WiremockHelper.verifyPost(uri = "/minor-entity-identification/register-ua", optBody = Some(jsonBody.toString()))

}
