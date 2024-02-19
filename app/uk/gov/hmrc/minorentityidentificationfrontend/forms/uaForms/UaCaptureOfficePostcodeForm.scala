/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.minorentityidentificationfrontend.forms.uaForms

import play.api.data.Form
import play.api.data.validation.Constraint
import uk.gov.hmrc.minorentityidentificationfrontend.forms.utils.ConstraintUtil.ConstraintUtil
import uk.gov.hmrc.minorentityidentificationfrontend.forms.utils.MappingUtil.{OTextUtil, optText}
import uk.gov.hmrc.minorentityidentificationfrontend.forms.utils.ValidationHelper.{validate, validateNot}

import scala.util.matching.Regex

object UaCaptureOfficePostcodeForm {

  val postCodeRegex: Regex = """^[A-Z]{1,2}[0-9][0-9A-Z]?\s?[0-9][A-Z]{2}$""".r

  val postcodeNotEntered: Constraint[String] = Constraint("office-postcode.not-entered")(
    country => validate(
      constraint = country.isEmpty,
      errMsg = "ua.error.no_entry_office_postcode"
    )
  )

  val postcodeInvalid: Constraint[String] = Constraint("office-postcode.invalid-format")(
    postcode => validateNot(
      constraint = postcode.toUpperCase matches postCodeRegex.regex,
      errMsg = "ua.error.invalid_format_office_postcode"
    )
  )

  val form: Form[String] =
    Form(
      "officePostcode" -> optText.toText.verifying(postcodeNotEntered andThen postcodeInvalid)
    )
}
