/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.minorentityidentificationfrontend.forms.overseasForm

import play.api.data.Forms.{of, single}
import play.api.data.format.Formatter
import play.api.data.{Form, FormError}
import uk.gov.hmrc.minorentityidentificationfrontend.models.enumerations.YesNo

import scala.util.matching.Regex

object OverseasCaptureTaxIdentifierForm {

  val identifiersRegex: Regex = """[A-Za-z0-9]{1,60}""".r

  private val overseasTaxIdentifierRadioKey: String = "tax-identifier-radio"
  private val overseasTaxIdentifierKey: String = "tax-identifier"

  private val noSelectionMadeErrorMsg: String = "error.no_tax_identifier_selection"
  private val overseasTaxIdentifierNotEnteredErrorMsg: String = "error.no_tax_identifier"
  private val overseasTaxIdentifierTooLongErrorMsg: String = "error.invalid_tax_identifier_length"
  private val overseasTaxIdentifierInvalidCharsErrorMsg: String = "error.invalid_tax_identifier"

  private val overseasTaxIdentifierFormatter: Formatter[Option[String]] = new Formatter[Option[String]] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] = {

      val overseasTaxIdentifierChoiceExists: Boolean = data.getOrElse(key, "").nonEmpty

      if (overseasTaxIdentifierChoiceExists) {

        val overseasTaxIdentifierChoice: Option[String] = data.get(key)

        if (overseasTaxIdentifierChoice.get == YesNo.Yes.toString) {
          handleOverseasTaxIdentifier(data)
        } else {
          Right(None)
        }

      } else {

        Left(Seq(FormError(key, noSelectionMadeErrorMsg)))

      }

    }

    override def unbind(key: String, value: Option[String]): Map[String, String] = {

      value match {
        case Some(overseasTaxIdentifier) => Map(
          key -> YesNo.Yes.toString,
          overseasTaxIdentifierKey -> overseasTaxIdentifier
        )
        case None => Map(key -> YesNo.No.toString)
      }

    }

    def handleOverseasTaxIdentifier(data: Map[String, String]): Either[Seq[FormError], Option[String]] = {

      data.get(overseasTaxIdentifierKey) match {
        case Some(id) => validateOverseasTaxIdentifier(id)
        case None => Left(Seq(FormError(overseasTaxIdentifierKey, overseasTaxIdentifierNotEnteredErrorMsg)))
      }
    }

    def validateOverseasTaxIdentifier(id: String): Either[Seq[FormError], Option[String]] = {

      if (validateEntered(id)) {

        if (validateLength(id)) {

          if (validateCharacters(id)) {

            Right(Some(id))

          } else {

            Left(Seq(FormError(overseasTaxIdentifierKey, overseasTaxIdentifierInvalidCharsErrorMsg)))

          }

        } else {

          Left(Seq(FormError(overseasTaxIdentifierKey, overseasTaxIdentifierTooLongErrorMsg)))

        }

      } else {

        Left(Seq(FormError(overseasTaxIdentifierKey, overseasTaxIdentifierNotEnteredErrorMsg)))

      }

    }

    def validateEntered(id: String): Boolean = id.nonEmpty

    def validateLength(id: String): Boolean = id.length <= 60

    def validateCharacters(id: String): Boolean = id matches identifiersRegex.regex
  }

  val form: Form[Option[String]] = {
    Form(
      single(overseasTaxIdentifierRadioKey -> of[Option[String]](overseasTaxIdentifierFormatter))
    )
  }

}
