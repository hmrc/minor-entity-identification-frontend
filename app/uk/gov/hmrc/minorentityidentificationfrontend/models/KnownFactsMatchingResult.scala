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

package uk.gov.hmrc.minorentityidentificationfrontend.models

import play.api.libs.json._

sealed trait KnownFactsMatchingResult

case object SuccessfulMatch extends KnownFactsMatchingResult

sealed trait KnownFactsMatchFailure extends KnownFactsMatchingResult


case object UnMatchable extends KnownFactsMatchFailure

case object DetailsMismatch extends KnownFactsMatchFailure

case object DetailsNotFound extends KnownFactsMatchFailure

object KnownFactsMatchingResult {
  val KnownFactsMatchingResultKey = "identifiersMatch"
  val SuccessfulMatchKey = "SuccessfulMatch"
  val UnMatchableKey = "UnMatchable"
  val DetailsMismatchKey = "DetailsMismatch"
  val DetailsNotFoundKey = "DetailsNotFound"

  implicit val format: Format[KnownFactsMatchingResult] = new Format[KnownFactsMatchingResult] {
    override def writes(knownFactsMatchingResult: KnownFactsMatchingResult): JsValue = {
      val knownFactsMatchingResultString = knownFactsMatchingResult match {
        case SuccessfulMatch => SuccessfulMatchKey
        case UnMatchable => UnMatchableKey
        case DetailsMismatch => DetailsMismatchKey
        case DetailsNotFound => DetailsNotFoundKey
      }

      JsString(knownFactsMatchingResultString)
    }

    override def reads(json: JsValue): JsResult[KnownFactsMatchingResult] =
      (json).validate[String].collect(JsonValidationError("Invalid Known Facts Matching Result")) {
        case SuccessfulMatchKey => SuccessfulMatch
        case UnMatchableKey => UnMatchable
        case DetailsMismatchKey => DetailsMismatch
        case DetailsNotFoundKey => DetailsNotFound
      }
  }
}
