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

package uk.gov.hmrc.minorentityidentificationfrontend.models

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import play.api.libs.json.{Json, JsError, JsSuccess, JsValue}

class TrustKnownFactsSpec extends AnyFlatSpec {

  "A valid Json representation of a UK trust" should "be deserialized correctly" in new Setup {

    val trustKnownFactsAsJsValue: JsValue = Json.parse(trustKnownFactsAsString)

    val expected: TrustKnownFacts = TrustKnownFacts(Some(correspondencePostcode), Some(declarationPostcode), false)

    trustKnownFactsAsJsValue.validate[TrustKnownFacts] match {
      case JsSuccess(actual, _) => actual should be(expected)
      case JsError(error) => fail(s"Parsing json failed with error : $error")
    }

  }

  "A valid representation of a foreign trust" should "be deserialized correctly" in new Setup {

    val trustKnownFactsAbroadAsJson: JsValue = Json.parse(trustKnownFactsAbroadAsString)

    val expected: TrustKnownFacts = TrustKnownFacts(None, None, true)

    trustKnownFactsAbroadAsJson.validate[TrustKnownFacts] match {
      case JsSuccess(actual, _) => actual should be(expected)
      case JsError(error) => fail(s"Parsing json failed with error : $error")
    }

  }

  "An instance of TrustKnownFacts for a UK trust" should "be serialized correctly" in new Setup {

    val actual: JsValue = Json.toJson(TrustKnownFacts(Some(correspondencePostcode), Some(declarationPostcode), false))

    withoutWhitespace(actual.toString) should be(withoutWhitespace(trustKnownFactsAsString))
  }

  "An instance of TrustKnownFacts for a foreign trust" should "be serialized correctly" in new Setup {

    val actual: JsValue = Json.toJson(TrustKnownFacts(None, None, true))

    actual.toString should be(withoutWhitespace(trustKnownFactsAbroadAsString))
  }

  trait Setup {

    val correspondencePostcode: String = "NE98 1WE"
    val declarationPostcode: String = "AA1 1AA"

    val trustKnownFactsAsString: String =  s"""{
         |  "correspondencePostcode" : "$correspondencePostcode",
         |  "declarationPostcode" : "$declarationPostcode",
         |  "isAbroad" : false
         |}""".stripMargin

    val trustKnownFactsAbroadAsString: String = """{ "isAbroad" : true }"""

    def withoutWhitespace(s: String): String = s.filterNot(_.isWhitespace)

  }
}
