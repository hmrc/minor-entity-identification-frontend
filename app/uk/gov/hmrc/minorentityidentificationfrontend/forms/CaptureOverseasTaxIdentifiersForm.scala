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
import uk.gov.hmrc.minorentityidentificationfrontend.forms.utils.ValidationHelper.{validate, validateNot}
import uk.gov.hmrc.minorentityidentificationfrontend.models.Overseas

import scala.util.matching.Regex

object CaptureOverseasTaxIdentifiersForm {

  val identifiersRegex: Regex = """[A-Za-z0-9]{1,60}""".r

  private val taxIdentifierNotEntered: Constraint[String] = Constraint("tax_identifier.not_entered")(
    taxIdentifier => validate(
      constraint = taxIdentifier.isEmpty,
      errMsg = "error.no_tax_identifier"
    )
  )

  private val taxIdentifierInvalid: Constraint[String] = Constraint("tax_identifier.invalid")(
    taxIdentifier => validateNot(
      constraint = taxIdentifier matches identifiersRegex.regex,
      errMsg = "error.invalid_tax_identifier"
    )
  )

  private val taxIdentifierTooLong: Constraint[String] = Constraint("tax_identifier.too-long")(
    taxIdentifier => validateNot(
      constraint = taxIdentifier.length <= 60,
      errMsg = "error.invalid_tax_identifier_length"
    )
  )

  private val countryNotEntered: Constraint[String] = Constraint("country.not-entered")(
    country => validate(
      constraint = country.isEmpty,
      errMsg = "error.no_tax_identifier_country"
    )
  )

  val form: Form[Overseas] = {
    Form(
      mapping(
        "tax-identifier" -> text.verifying(taxIdentifierNotEntered andThen taxIdentifierTooLong andThen taxIdentifierInvalid),
        "country" -> text.verifying(countryNotEntered)
      )(Overseas.apply)(Overseas.unapply)
    )
  }

}
