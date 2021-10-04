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

package uk.gov.hmrc.minorentityidentificationfrontend.forms

import play.api.data.Form
import play.api.data.Forms.text
import play.api.data.validation.Constraint
import uk.gov.hmrc.minorentityidentificationfrontend.forms.utils.ConstraintUtil.ConstraintUtil
import uk.gov.hmrc.minorentityidentificationfrontend.forms.utils.ValidationHelper.{validate, validateNot}

import scala.util.matching.Regex

object CaptureUtrForm {
  val utrErrorKey: String = "utr.error"
  val utrRegex: Regex = "[0-9]{10}".r

  val utrNotEntered: Constraint[String] = Constraint("utr.not_entered")(
    utr => validate(
      constraint = utr.isEmpty,
      errMsg = "utr.error_not_entered"
    )
  )

  val utrInvalidCharacters: Constraint[String] = Constraint("utr.invalid_format")(
    utr => validateNot(
      constraint = utr matches utrRegex.regex,
      errMsg = "utr.error_invalid_format"
    )
  )

  val utrInvalidLength: Constraint[String] = Constraint("utr.invalid_length")(
    utr => validate(
      constraint = utr.length != 10,
      errMsg = "utr.error_invalid_length"
    )
  )

  val form: Form[String] =
    Form(
      "utr" -> text.verifying(utrNotEntered andThen utrInvalidLength andThen utrInvalidCharacters)
    )

}
