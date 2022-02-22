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

package uk.gov.hmrc.minorentityidentificationfrontend.forms.utils

import play.api.data.validation.Constraint
import uk.gov.hmrc.minorentityidentificationfrontend.forms.utils.ValidationHelper.{validate, validateNot}
import uk.gov.hmrc.minorentityidentificationfrontend.models.Utr

import scala.util.matching.Regex

object UtrCaptureHelper {

  val utrRegex: Regex = "[0-9]{10}".r

  def utrInvalidCharacters(errMessageKey: String): Constraint[Utr] = Constraint("utr.invalid_format")(
    utr => validateNot(
      constraint = utr.value matches utrRegex.regex,
      errMsg = errMessageKey
    )
  )

  def utrInvalidLength(errMessageKey: String): Constraint[Utr] = Constraint("utr.invalid_length")(
    utr => validate(
      constraint = utr.value.length != 10,
      errMsg = errMessageKey
    )
  )

}
