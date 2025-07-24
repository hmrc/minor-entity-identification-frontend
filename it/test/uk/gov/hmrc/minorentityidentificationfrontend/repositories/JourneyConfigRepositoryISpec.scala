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

package test.uk.gov.hmrc.minorentityidentificationfrontend.repositories

import play.api.test.Helpers.{await, _}
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ComponentSpecHelper

class JourneyConfigRepositoryISpec extends ComponentSpecHelper {

  "insertJourneyConfig" should {
    "insert the provided journey config" in {
      await(journeyConfigRepository.insertJourneyConfig(testJourneyId, testInternalId, testOverseasCompanyJourneyConfig(businessVerificationCheck = true)))
      val insertedJourneyConfig = await(journeyConfigRepository.getJourneyConfig(testJourneyId, testInternalId))

      insertedJourneyConfig mustBe Some(testOverseasCompanyJourneyConfig(businessVerificationCheck = true))
    }
  }
}
