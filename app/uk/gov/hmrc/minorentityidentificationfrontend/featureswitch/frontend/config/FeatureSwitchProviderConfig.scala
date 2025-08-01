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

package uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.frontend.config

import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.minorentityidentificationfrontend.featureswitch.frontend.models.FeatureSwitchProvider

import javax.inject.{Inject, Singleton}

@Singleton
class FeatureSwitchProviderConfig @Inject()(configuration: Configuration) {

  val servicesConfig = new ServicesConfig(configuration)

  lazy val selfBaseUrl: String = servicesConfig.baseUrl("self")

  lazy val selfFeatureSwitchUrl = s"$selfBaseUrl/minor-entity-identification/test-only/api/feature-switches"

  lazy val minorEntityIdentificationFeatureSwitchUrl =
    s"${servicesConfig.baseUrl("minor-entity-identification")}/minor-entity-identification/test-only/api/feature-switches"

  lazy val selfFeatureSwitchProvider: FeatureSwitchProvider = FeatureSwitchProvider(
    id = "minor-entity-identification-frontend",
    appName = "Minor Entity Identification Frontend",
    url = selfFeatureSwitchUrl
  )

  lazy val minorEntityIdentificationFeatureSwitchProvider: FeatureSwitchProvider = FeatureSwitchProvider(
    id = "minor-entity-identification",
    appName = "Minor Entity Identification",
    url = minorEntityIdentificationFeatureSwitchUrl
  )

  lazy val featureSwitchProviders: Seq[FeatureSwitchProvider] =
    Seq(selfFeatureSwitchProvider, minorEntityIdentificationFeatureSwitchProvider)

}
