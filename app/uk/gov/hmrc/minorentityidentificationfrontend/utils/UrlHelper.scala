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

package uk.gov.hmrc.minorentityidentificationfrontend.utils

import uk.gov.hmrc.minorentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl.idFunctor
import uk.gov.hmrc.play.bootstrap.binders.{AbsoluteWithHostnameFromAllowlist, OnlyRelative, RedirectUrl}

import javax.inject.{Inject, Singleton}

@Singleton
class UrlHelper @Inject()(appConfig: AppConfig) {

  def areRelativeOrAcceptedUrls(urls: List[String]): Boolean = {
    val allowedUrls = urls.map(url =>
      RedirectUrl(url).getEither(OnlyRelative | AbsoluteWithHostnameFromAllowlist(appConfig.allowedHosts)) match {
        case Right(_) => true
        case Left(_) => false
      })
    !allowedUrls.contains(false)
  }
}
