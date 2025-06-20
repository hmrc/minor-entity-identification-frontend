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

package uk.gov.hmrc.minorentityidentificationfrontend.assets

object MessageLookup {

  object Base {
    val confirmAndContinue = "Confirm and continue"
    val saveAndContinue = "Save and continue"
    val getHelp = "Is this page not working properly? (opens in new tab)"
    val change = "Change"
    val continue = "Continue"
    val yes = "Yes"
    val no = "No"
    val back = "Back"

    object Error {
      val title = "There is a problem"
      val error = "Error: "
    }

  }

  object Header {
    val signOut = "Sign out"
  }

  object BetaBanner {
    val title = "This is a new service – your feedback will help us to improve it."
  }

  object CaptureUtr {
    val title = "What is the company’s UK Unique Taxpayer Reference?"
    val line_1 = "This is 10 numbers, for example 1234567890. It will be on HMRC tax returns and other letters about Corporation Tax or Self Assessment. It may be called ‘reference‘, ‘UTR‘ or ‘official use‘."
    val no_utr_link = "The company does not have a UTR"

    object Error {
      val error_not_entered = "Enter the company’s UK Unique Taxpayer Reference"
      val error_invalid_format = "Enter the company’s UK Unique Taxpayer Reference in the correct format"
      val error_invalid_length = "Enter the company’s UK Unique Taxpayer Reference in the correct format"
    }
  }

  object CaptureTrustUtr {
    val title = "Your trust’s Self Assessment Unique Taxpayer Reference (UTR)"
    val p1 = "You can find it in your Personal Tax Account, the HMRC app or on tax returns and other documents from HMRC. It might be called ‘reference’, ‘UTR’ or ‘official use’."
    val more_help = "Get more help to find your UTR (opens in new tab)"
    val no_utr_link = "The trust does not have a Self Assessment UTR"
    val label = "What is your Self Assessment UTR?"
    val hint = "Your UTR is 10 digits long."
    val findUtrLink = "https://www.gov.uk/find-utr-number"

    object Error {
      val error_not_entered = "Enter the trust’s Unique Taxpayer Reference"
      val error_invalid_format = "Enter the trust’s Unique Taxpayer Reference in the correct format"
      val error_invalid_length = "Enter the trust’s Unique Taxpayer Reference in the correct format"
    }
  }

  object CaptureUaUtr {
    val title = "What is the association’s Unique Taxpayer Reference?"
    val line_1 = "This is 10 numbers, for example 1234567890. It will be on tax returns and other letters about Corporation Tax. It may be called ‘reference‘, ‘UTR‘ or ‘official use‘. You can"
    val line_1_ending = "find a lost UTR number."
    val no_utr_link = "The association does not have a UTR"

    object Error {
      val error_not_entered = "Enter the association’s Unique Taxpayer Reference"
      val error_invalid_format = "Enter the association’s Unique Taxpayer Reference in the correct format"
      val error_invalid_length = "Enter the association’s Unique Taxpayer Reference in the correct format"
    }
  }

  object CaptureOverseasTaxIdentifiersCountry {

    val title = "Which country issued the overseas tax identifier?"

    object Error {
      val no_input_country = "Enter the name of the country that issued the overseas tax identifier"
    }
  }

  object CaptureOverseasTaxIdentifier {

    val title = "Your overseas tax identifier"
    val p1 = "You can provide any identifier that can be used to verify the business, like a VAT registration number or a Employee Identification Number (EIN)."
    val p2 = "It can help us to identify the business, you do not need to provide a overseas tax identifier if you don’t have one."
    val legend = "Does the business have an overseas tax identifier?"
    val form_field_1 = "Overseas tax identifier"

    object Error {
      val no_tax_identifier_selection = "Select yes if the business has an overseas tax identifier"
      val no_entry_tax_identifier = "Enter the overseas tax identifier"
      val invalid_tax_identifier = "Enter a tax identifier that does not contain special characters"
      val invalid_length_tax_identifier = "The overseas tax identifier must be 60 characters or fewer"
    }

  }

  object CaptureSaPostcode {

    val title = "What is the postcode used to register the trust for Self Assessment?"
    val hint = "For example, AB1 2YZ"
    val no_postcodeLink = "The trust does not have a Self Assessment postcode"

    object Error {
      val invalid_sa_postcode = "Enter the postcode in the correct format"
      val no_entry_sa_postcode = "Enter the postcode used to register the trust for Self Assessment"
    }
  }

  object CaptureOfficePostcode {

    val title = "What is the postcode used to register the association for Corporation Tax?"
    val hint = "For example, AB1 2YZ"

    object Error {
      val invalid_format_office_postcode = "Enter the postcode in the correct format"
      val no_entry_office_postcode = "Enter the postcode used to register the association for Corporation Tax"
    }
  }

  object CaptureCHRN {
    val title = "What is the charity’s HMRC reference number?"
    val inset = "If the charity has registered for Gift Aid then their HMRC reference number will be the same as their Gift Aid number. This is not the same as the charity number available on the charity register."
    val hint = "This could be up to 7 characters and must begin with either one or two letters, followed by 1-5 numbers. For example, A999 or AB99999"
    val link = "The charity does not have a HMRC reference number"
    val label = "HMRC reference number"

    object Error {
      val error_not_entered = "Enter the HMRC reference number"
      val error_invalid_characters = "Enter the HMRC reference number in the correct format"
      val error_invalid_length = "The HMRC reference number must be 7 characters or fewer"
    }
  }

  object CaptureUaChrn {
    val title = "What is the association’s HMRC reference Number?"
    val inset = "If you are registering as a ‘Charitable Incorporated Organisation’, you do not need to enter an HMRC reference number. Instead, click on ‘The association does not have a HMRC reference number’ hyperlink below."
    val paragraph = "If the association has registered for Gift Aid then their HMRC reference number will be the same as their Gift Aid number. This is not the same as the charity number available on the charity register."
    val hint = "This could be up to 7 characters and must begin with either one or two letters, followed by 1-5 numbers. For example, A999 or AB99999"
    val link = "The association does not have a HMRC reference number"
    val label = "HMRC reference number"

    object Error {
      val error_not_entered = "Enter the HMRC reference number"
      val error_invalid_characters = "Enter the HMRC reference number in the correct format"
      val error_invalid_length = "The HMRC reference number must be 7 characters or fewer"
    }
  }

  object CheckYourAnswers {
    val title = "Check your answers"
    val heading = "Check your answers"

    val utr = "Unique Taxpayer Reference (UTR)"
    val noUtr = "The business does not have a UTR"
    val noTrustUtr = "The trust does not have a UTR"
    val noUaUtr = "The association does not have a UTR"

    val postcode = "Self Assessment postcode"
    val noPostCode = "The trust does not have a Self Assessment postcode"
    val uaPostcode = "Corporation Tax postcode"

    val charityHRMCReferenceNumber = "HMRC reference number"
    val charityHMRCReferenceNumberNotProvided = "The charity does not have a HMRC reference number"
    val uaCharityHRMCReferenceNumberNotProvided = "The association does not have a HMRC reference number"

    val overseasTaxIdentifier = "Overseas tax identifier"
    val overseasTaxIdentifierCountry = "Country of overseas tax identifier"
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
