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

package uk.gov.hmrc.minorentityidentificationfrontend.views.helpers

import play.api.data.Form
import play.api.i18n.Messages

object TitleHelper {

  def title(titleKey: String, form: Form[_])(implicit messages: Messages): String =
    title(titleKey, form.hasErrors)

  def title(titleKey: String, isAnErrorPage: Boolean = false)(implicit messages: Messages): String = {
    val serviceName = if (messages.isDefinedAt("optServiceName")) messages("optServiceName") else messages("service.name.default")
    val titleMessage: String = s"${messages(titleKey)} - $serviceName - ${messages("service.govuk")}"

    if (isAnErrorPage) messages("error.title-prefix") + titleMessage
    else titleMessage
  }
}
