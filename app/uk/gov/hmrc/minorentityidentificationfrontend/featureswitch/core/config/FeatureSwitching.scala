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

package uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.core.config

import uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.core.models.FeatureSwitch

trait FeatureSwitching {

  val FEATURE_SWITCH_ON = "true"
  val FEATURE_SWITCH_OFF = "false"

  def isEnabled(featureSwitch: FeatureSwitch): Boolean =
    sys.props get featureSwitch.configName contains FEATURE_SWITCH_ON

  def enable(featureSwitch: FeatureSwitch): Unit =
    sys.props += featureSwitch.configName -> FEATURE_SWITCH_ON

  def disable(featureSwitch: FeatureSwitch): Unit =
    sys.props += featureSwitch.configName -> FEATURE_SWITCH_OFF

}
