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

import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.core.models.FeatureSwitch

import javax.inject.Singleton

@Singleton
class FeatureSwitchingModule extends Module with FeatureSwitchRegistry {

  val switches = Seq(EnableFullTrustJourney, TrustVerificationStub, BusinessVerificationStub, EnableFullUAJourney)

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    Seq(
      bind[FeatureSwitchRegistry].to(this).eagerly()
    )
  }
}

case object EnableFullTrustJourney extends FeatureSwitch {
  override val configName: String = "feature-switch.enable-full-trust-journey"
  override val displayName: String = "Enable full trust journey"
}

case object TrustVerificationStub extends FeatureSwitch {
  override val configName: String = "feature-switch.trust-verification-stub"
  override val displayName: String = "Use stub for Trusts further verification flow"
}

case object BusinessVerificationStub extends FeatureSwitch {
  override val configName: String = "feature-switch.business-verification-stub"
  override val displayName: String = "Use stub for Business Verification flow"
}
case object EnableFullUAJourney extends FeatureSwitch {
  override val configName: String = "feature-switch.enable-full-unincorporated-association-journey"
  override val displayName: String = "Enable full Unincorporated association journey"
}
