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

package uk.gov.hmrc.minorentityidentificationfrontend.forms.trust

import play.api.data.Form
import play.api.data.Forms._
import uk.gov.hmrc.minorentityidentificationfrontend.forms.helpers.UtrCaptureHelper
import uk.gov.hmrc.minorentityidentificationfrontend.forms.utils.ConstraintUtil.ConstraintUtil
import uk.gov.hmrc.minorentityidentificationfrontend.forms.utils.UtrMapping.utrMapping
import uk.gov.hmrc.minorentityidentificationfrontend.models.Utr

import scala.util.matching.Regex

object TrustCaptureUtrForm {
  val UtrKey = "utr"

  val TrustUtrNotEnteredErrorKey = "utr.trust.error_not_entered"
  val TrustUtrInvalidCharactersErrorKey = "utr.trust.error_invalid_characters"
  val TrustUtrInvalidLengthErrorKey = "utr.trust.error_invalid_length"

  val utrRegex: Regex = "[0-9]{10}".r

  val trustForm: Form[Utr] = Form(
      UtrKey -> of(utrMapping(TrustUtrNotEnteredErrorKey))
        .verifying(
          UtrCaptureHelper.utrInvalidLength(TrustUtrInvalidLengthErrorKey) andThen
            UtrCaptureHelper.utrInvalidCharacters(TrustUtrInvalidCharactersErrorKey)
        )
    )

}
