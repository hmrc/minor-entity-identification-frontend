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
import play.api.data.validation.Constraint
import uk.gov.hmrc.minorentityidentificationfrontend.forms.utils.ConstraintUtil.ConstraintUtil
import uk.gov.hmrc.minorentityidentificationfrontend.forms.utils.MappingUtil.{OTextUtil, optText}
import uk.gov.hmrc.minorentityidentificationfrontend.forms.utils.ValidationHelper.{validate, validateNot}

import scala.util.matching.Regex

object CaptureSaPostcodeForm {

  val postCodeRegex: Regex = """^[A-Z]{1,2}[0-9][0-9A-Z]?\s?[0-9][A-Z]{2}$""".r

  val postcodeNotEntered: Constraint[String] = Constraint("sa-postcode.not-entered")(
    country => validate(
      constraint = country.isEmpty,
      errMsg = "error.no_entry_sa-postcode"
    )
  )

  val postcodeInvalid: Constraint[String] = Constraint("sa-postcode.invalid-format")(
    postcode => validateNot(
      constraint = postcode.toUpperCase matches postCodeRegex.regex,
      errMsg = "error.invalid_format_sa-postcode"
    )
  )

  val form: Form[String] =
    Form(
      "saPostcode" -> optText.toText.verifying(postcodeNotEntered andThen postcodeInvalid)
    )
}
