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

import play.api.data.{Form, FormError}
import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.validation.Constraint
import uk.gov.hmrc.minorentityidentificationfrontend.forms.utils.ValidationHelper.{validate, validateNot}
import uk.gov.hmrc.minorentityidentificationfrontend.models.OverseasTaxIdentifier
import uk.gov.hmrc.minorentityidentificationfrontend.models.enumerations.YesNo

import scala.util.matching.Regex

object CaptureOverseasTaxIdentifiersForm {

  val identifiersRegex: Regex = """[A-Za-z0-9]{1,60}""".r

  private val overSeasTaxIdentifierRadioKey: String = "tax-identifier-radio"
  private val overSeasTaxIdentifierKey: String = "tax-identifier"

  private val noSelectionMadeErrorMsg: String = "error.no_tax_identifier_selection"
  private val overSeasTaxIdentifierNotEnteredErrorMsg: String = "error.no_tax_identifier"
  private val overSeasTaxIdentifierTooLongErrorMsg: String = "error.invalid_tax_identifier_length"
  private val overSeasTaxIdentifierInvalidCharsErrorMsg: String = "error.invalid_tax_identifier"

  private val overSeasTaxIdentifierFormatter: Formatter[OverseasTaxIdentifier] = new Formatter[OverseasTaxIdentifier]  {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], OverseasTaxIdentifier] = {

      val overSeasTaxIdentifierChoiceExists: Boolean = data.getOrElse(key,"").nonEmpty

      if(overSeasTaxIdentifierChoiceExists) {

        val overSeasTaxIdentifierChoice: Option[String] = data.get(key)

        if(overSeasTaxIdentifierChoice.get == YesNo.Yes.toString){
          handleOverseasTaxIdentifier(data)
        } else {
          Right(OverseasTaxIdentifier(YesNo.No.toString))
        }

      } else {

        Left(Seq(FormError(key, noSelectionMadeErrorMsg)))

      }

    }

    override def unbind(key: String, value: OverseasTaxIdentifier): Map[String, String] = {

      if(value.taxIdentifierExists()){
        Map(
          key -> value.yesNo,
          overSeasTaxIdentifierKey -> value.taxIdentifier.get
        )
      } else Map(key -> value.yesNo)
    }

    def handleOverseasTaxIdentifier(data: Map[String, String]): Either[Seq[FormError], OverseasTaxIdentifier] = {

      data.get(overSeasTaxIdentifierKey) match {
        case Some(id) => validateOverSeasTaxIdentifier(id)
        case None => Left(Seq(FormError(overSeasTaxIdentifierKey, overSeasTaxIdentifierNotEnteredErrorMsg)))
      }
    }

    def validateOverSeasTaxIdentifier(id: String): Either[Seq[FormError], OverseasTaxIdentifier] = {

      if(validateEntered(id)) {

        if(validateLength(id)) {

          if(validateCharacters(id)){

            Right(OverseasTaxIdentifier(YesNo.Yes.toString, Some(id)))

          } else {

            Left(Seq(FormError(overSeasTaxIdentifierKey, overSeasTaxIdentifierInvalidCharsErrorMsg)))

          }

        } else {

          Left(Seq(FormError(overSeasTaxIdentifierKey, overSeasTaxIdentifierTooLongErrorMsg)))

        }

      } else {

        Left(Seq(FormError(overSeasTaxIdentifierKey, overSeasTaxIdentifierNotEnteredErrorMsg)))

      }

    }

    def validateEntered(id: String): Boolean = id.nonEmpty

    def validateLength(id: String): Boolean = id.length <= 60

    def validateCharacters(id: String): Boolean = id matches identifiersRegex.regex
  }

  val form: Form[OverseasTaxIdentifier] = {
    Form(
      single(overSeasTaxIdentifierRadioKey -> of[OverseasTaxIdentifier](overSeasTaxIdentifierFormatter))
    )
  }

}
