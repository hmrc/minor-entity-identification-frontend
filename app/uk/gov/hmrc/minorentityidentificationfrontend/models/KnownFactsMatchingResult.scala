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

case object UnMatchableWithRetry extends KnownFactsMatchFailure

case object UnMatchableWithoutRetry extends KnownFactsMatchFailure

case object DetailsMismatch extends KnownFactsMatchFailure

case object DetailsNotFound extends KnownFactsMatchFailure

object KnownFactsMatchingResult {
  val KnownFactsMatchingResultKey = "identifiersMatch"
  val SuccessfulMatchKey = "SuccessfulMatch"
  val UnMatchableWithoutRetryKey = "UnMatchableWithoutRetry"
  val UnMatchableWithRetryKey = "UnMatchableWithRetry"
  val DetailsMismatchKey = "DetailsMismatch"
  val DetailsNotFoundKey = "DetailsNotFound"

  implicit val format: Format[KnownFactsMatchingResult] = new Format[KnownFactsMatchingResult] {
    override def writes(knownFactsMatchingResult: KnownFactsMatchingResult): JsObject = {
      val knownFactsMatchingResultString = knownFactsMatchingResult match {
        case SuccessfulMatch => SuccessfulMatchKey
        case UnMatchableWithoutRetry => UnMatchableWithoutRetryKey
        case UnMatchableWithRetry => UnMatchableWithRetryKey
        case DetailsMismatch => DetailsMismatchKey
        case DetailsNotFound => DetailsNotFoundKey
      }

      Json.obj(KnownFactsMatchingResultKey -> knownFactsMatchingResultString)
    }

    override def reads(json: JsValue): JsResult[KnownFactsMatchingResult] =
      (json \ KnownFactsMatchingResultKey).validate[String].collect(JsonValidationError("Invalid business validation state")) {
        case SuccessfulMatchKey => SuccessfulMatch
        case UnMatchableWithRetryKey => UnMatchableWithRetry
        case UnMatchableWithoutRetryKey => UnMatchableWithoutRetry
        case DetailsMismatchKey => DetailsMismatch
        case DetailsNotFoundKey => DetailsNotFound
      }
  }
}
