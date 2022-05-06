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

package uk.gov.hmrc.minorentityidentificationfrontend.forms.trustForms

import play.api.data.Form
import play.api.data.Forms.text
import play.api.data.validation.Constraint
import uk.gov.hmrc.minorentityidentificationfrontend.forms.utils.ConstraintUtil.ConstraintUtil
import uk.gov.hmrc.minorentityidentificationfrontend.forms.utils.ValidationHelper.{validate, validateNot}

import scala.util.matching.Regex

object CaptureCHRNForm {

  val ChrnKey: String = "chrn"

  val ChrnNotEnteredErrorKey: String = "chrn.error_not_entered"
  val ChrnInvalidCharactersErrorKey: String = "chrn.error_invalid_characters"
  val ChrnInvalidLengthErrorKey: String = "chrn.error_invalid_length"

  val ChrnRegex: Regex = "[A-Za-z]{1,2}[0-9]{1,5}".r

  def chrnEmpty: Constraint[String] = Constraint("chrn.not_entered")(
    chrn => validate(constraint = chrn.isEmpty, errMsg = ChrnNotEnteredErrorKey)
  )

  def chrnInvalidCharacters: Constraint[String] = Constraint("chrn.invalid_characters")(
    chrn => validateNot(
      constraint = chrn matches ChrnRegex.regex,
      errMsg = ChrnInvalidCharactersErrorKey
    )
  )

  def chrnInvalidLength: Constraint[String] = Constraint("chrn.invalid_length")(
    chrn => validate(
      constraint = chrn.length > 7,
      errMsg = ChrnInvalidLengthErrorKey
    )
  )

  val form: Form[String] =
    Form(
      ChrnKey -> text.verifying(chrnEmpty andThen chrnInvalidLength andThen chrnInvalidCharacters)
    )

}
