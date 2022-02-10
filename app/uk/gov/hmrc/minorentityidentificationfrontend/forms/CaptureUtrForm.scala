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

package uk.gov.hmrc.minorentityidentificationfrontend.forms

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraint
import uk.gov.hmrc.minorentityidentificationfrontend.forms.utils.ConstraintUtil.ConstraintUtil
import uk.gov.hmrc.minorentityidentificationfrontend.forms.utils.UtrMapping.utrMapping
import uk.gov.hmrc.minorentityidentificationfrontend.forms.utils.ValidationHelper.{validate, validateNot}
import uk.gov.hmrc.minorentityidentificationfrontend.models.Utr

import scala.util.matching.Regex

object CaptureUtrForm {
  val UtrKey = "utr"

  val UtrNotEnteredErrorKey = "utr.error_not_entered"
  val UtrInvalidCharactersErrorKey = "utr.error_invalid_characters"
  val UtrInvalidLengthErrorKey = "utr.error_invalid_length"

  val TrustUtrNotEnteredErrorKey = "utr.error_not_entered"
  val TrustUtrInvalidCharactersErrorKey = "utr.error_invalid_characters"
  val TrustUtrInvalidLengthErrorKey = "utr.error_invalid_length"

  val utrRegex: Regex = "[0-9]{10}".r

  private def utrInvalidCharacters(errMessageKey: String): Constraint[Utr] = Constraint("utr.invalid_format")(
    utr => validateNot(
      constraint = utr.value matches utrRegex.regex,
      errMsg = errMessageKey
    )
  )

  private def utrInvalidLength(errMessageKey: String): Constraint[Utr] = Constraint("utr.invalid_length")(
    utr => validate(
      constraint = utr.value.length != 10,
      errMsg = errMessageKey
    )
  )

  val form: Form[Utr] =
    Form(
      UtrKey -> of(utrMapping(UtrNotEnteredErrorKey))
        .verifying(
          utrInvalidLength(UtrInvalidCharactersErrorKey) andThen utrInvalidCharacters(UtrInvalidLengthErrorKey)
        )
    )

  val trustForm: Form[Utr] =
    Form(
      UtrKey -> of(utrMapping(TrustUtrNotEnteredErrorKey))
        .verifying(
          utrInvalidLength(TrustUtrInvalidCharactersErrorKey) andThen utrInvalidCharacters(TrustUtrInvalidLengthErrorKey)
        )
    )

}
