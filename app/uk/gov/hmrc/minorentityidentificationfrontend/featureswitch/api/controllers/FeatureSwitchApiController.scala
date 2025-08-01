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

package uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.api.controllers

import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, InjectedController}
import uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.api.services.FeatureSwitchService
import uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.core.config.FeatureSwitching
import uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.core.models.FeatureSwitchSetting
import uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.core.models.FeatureSwitchSetting._

import javax.inject.{Inject, Singleton}

@Singleton
class FeatureSwitchApiController @Inject()(featureSwitchService: FeatureSwitchService) extends InjectedController with FeatureSwitching {

  def getFeatureSwitches(journey: String): Action[AnyContent] = Action {
    Ok(Json.toJson(featureSwitchService.getFeatureSwitches()))
  }

  def updateFeatureSwitches(journey: String): Action[Seq[FeatureSwitchSetting]] = Action(parse.json[Seq[FeatureSwitchSetting]]) {
    req =>
      val updatedFeatureSwitches = featureSwitchService.updateFeatureSwitches(req.body)
      Ok(Json.toJson(updatedFeatureSwitches))
  }

}
