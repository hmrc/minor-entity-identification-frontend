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

package uk.gov.hmrc.minorentityidentificationfrontend.controllers.uaControllers

import play.api.libs.json.JsObject
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.controllers.AbstractBusinessVerificationControllerISpec
import uk.gov.hmrc.minorentityidentificationfrontend.models.JourneyConfig

class BusinessVerificationControllerISpec extends AbstractBusinessVerificationControllerISpec {

  override val businessVerificationResultUrlPrefix: String = s"/identify-your-unincorporated-association/$testJourneyId/business-verification-result"

  override val businessEntityBuilder: () => JourneyConfig = () => testUAJourneyConfig

  override val retrieveUtrJson: JsObject = testCtutrJson

  override val testUtr: String = testCtutr

  override val testJourneyDataJson: JsObject = testUAJourneyDataJson

  s"GET /identify-your-unincorporated-association/<JourneyId>/business-verification-result" when {
    commonTest()
  }
}
