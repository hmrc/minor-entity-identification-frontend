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

package uk.gov.hmrc.minorentityidentificationfrontend.assets

object MessageLookup {

  object Base {
    val confirmAndContinue = "Confirm and continue"
    val saveAndContinue = "Save and continue"
    val getHelp = "Is this page not working properly?"
    val change = "Change"
    val continue = "Continue"
    val yes = "Yes"
    val no = "No"

    object Error {
      val title = "There is a problem"
      val error = "Error: "
    }

  }

  object Header {
    val signOut = "Sign out"
  }

  object BetaBanner {
    val title = "This is a new service - your feedback (opens in new tab) will help us to improve it."
  }

  object CaptureUtr {
    val title = "What is the company‘s UK Unique Taxpayer Reference?"
    val line_1 = "This is 10 numbers, for example 1234567890. It will be on HMRC tax returns and other letters about Corporation Tax or Self Assessment. It may be called ‘reference‘, ‘UTR‘ or ‘official use‘."
    val no_utr_link = "The company does not have a UTR"

    object Error {
      val error_not_entered = "Enter the company‘s UK Unique Taxpayer Reference"
      val error_invalid_format = "Enter the UK Unique Taxpayer Reference using numbers only"
      val error_invalid_length = "Enter a UK Unique Taxpayer Reference that is 10 numbers"
    }
  }

  object CaptureTrustUtr {
    val title = "What is your Unique Taxpayer Reference?"
    val line_1 = "This is 10 numbers, for example 1234567890. It will be on tax returns and other letters about Self Assessment. It may be called ‘reference‘, ‘UTR‘ or ‘official use‘. You can"
    val line_1_ending = "find a lost UTR number."
    val no_utr_link = "The company does not have a UTR"

    object Error {
      val error_not_entered = "Enter your UK Unique Taxpayer Reference"
      val error_invalid_format = "Enter the UK Unique Taxpayer Reference using numbers only"
      val error_invalid_length = "Enter a UK Unique Taxpayer Reference that is 10 numbers"
    }
  }

  object CaptureOverseasTaxIdentifiers {

    val title = "What is the business’s overseas tax identifier?"
    val hint = "We may use this number to help us identify your business. The tax identifier could be VAT registration number, Employee Identification Number (EIN) or any other identifier we could use to verify your business details."
    val form_field_1 = "Enter a tax identifier"
    val form_field_2 = "Enter the name of the country that issued the tax identifier"
    val no_identifierLink = "I do not want to provide an identifier"

    object Error {
      val invalid_tax_identifier = "Enter a tax identifier that does not contain special characters"
      val no_entry_tax_identifier = "Enter a tax identifier"
      val invalid_length_tax_identifier = "Enter a tax identifier that is 60 characters or less"
      val no_entry_country = "Enter the name of the country that issued the tax identifier"
    }
  }

  object CaptureSaPostcode {

    val title = "What is the postcode where the trust is registered for Self Assessment?"
    val hint = "For example, AB1 2YZ"
    val no_postcodeLink = "The trust does not have a Self Assessment postcode"

    object Error {
      val invalid_sa_postcode = "Enter a UK postcode"
      val no_entry_sa_postcode = "Enter a postcode"
    }
  }

  object CaptureOfficePostcode {

    val title = "What is the postcode used to register the association?"
    val hint = "For example, AB1 2YZ"

    object Error {
      val invalid_format_office_postcode = "Enter a UK postcode"
      val no_entry_office_postcode = "Enter a postcode"
    }
  }

  object CaptureCHRN {
    val title = "What is the charity’s HMRC reference number?"
    val inset = "If the charity has registered for Gift Aid then their HMRC reference number will be the same as their Gift Aid number. This is not the same as the charity number available on the charity register."
    val hint = "This could be up to 7 characters and must begin with either one or two letters at the beginning followed by 1-5 numbers. For example, A999 or AB99999"
    val link = "The charity does not have a HMRC reference number"

    object Error {
      val error_not_entered = "Enter the HMRC reference number"
      val error_invalid_characters = "Enter the HMRC reference number in the correct format"
      val error_invalid_length = "Enter a HMRC reference number that is 7 characters or less"
    }
  }

  object CaptureUaChrn {
    val title = "What is the association’s HMRC reference Number?"
    val inset = "If the association has registered for Gift Aid then their HMRC reference number will be the same as their Gift Aid number. This is not the same as the charity number available on the charity register."
    val hint = "This could be up to 7 characters and must begin with either one or two letters at the beginning followed by 1-5 numbers. For example, A999 or AB99999"
    val link = "The association does not have a HMRC reference number"

    object Error {
      val error_not_entered = "Enter the HMRC reference number"
      val error_invalid_characters = "Enter the HMRC reference number in the correct format"
      val error_invalid_length = "Enter a HMRC reference number that is 7 characters or less"
    }
  }

  object CheckYourAnswers {
    val title = "Check your answers"
    val heading = "Check your answers"

    val utr = "Unique taxpayers reference number"
    val noUtr = "The business does not have a UTR"

    val postcode = "Self Assessment postcode"

    val charityHRMCReferenceNumber = "HMRC reference number"
    val charityHRMCReferenceNumberNotProvided = "The charity does not have a HMRC reference number"

    val overseasTaxIdentifier = "Overseas tax identifier"
    val overseasTaxIdentifierNotProvided = "I do not want to provide an identifier"
  }

  object CannotConfirmBusiness {
    val title = "The details you provided do not match records held by HMRC"
    val heading = "The details you provided do not match records held by HMRC"
    val line_1 = "If these details are correct, you can still register. If you entered the wrong details, go back and make changes."
    val question = "Do you want to continue registering with the details you provided?"

    object Error {
      val no_selection = "Select yes if you want to continue registering with the details you provided"
    }
  }

}
