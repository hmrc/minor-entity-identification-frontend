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

package uk.gov.hmrc.minorentityidentificationfrontend.models

import org.scalatest.matchers.should.Matchers._
import uk.gov.hmrc.minorentityidentificationfrontend.helpers.TestConstants.{testJourneyId, testTrustJourneyConfig}

class JourneyConfigSpec extends org.scalatest.flatspec.AnyFlatSpec {

  "continueUrl" should "append the journeyId query parameter to the continue url" in {
    testTrustJourneyConfig.continueUrl(testJourneyId) should be(testTrustJourneyConfig.continueUrl + s"?journeyId=$testJourneyId")
  }

}
