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

package uk.gov.hmrc.minorentityidentificationfrontend.assets

object MessageLookup {

  object Base {
    val confirmAndContinue = "Confirm and continue"
    val saveAndContinue = "Save and continue"
    val getHelp = "Is this page not working properly?"

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

}
