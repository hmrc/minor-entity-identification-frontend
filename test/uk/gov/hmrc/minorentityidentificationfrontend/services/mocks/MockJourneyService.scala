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

package uk.gov.hmrc.minorentityidentificationfrontend.services.mocks

import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.mockito.scalatest.{IdiomaticMockito, ResetMocksAfterEachTest}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.Suite
import uk.gov.hmrc.minorentityidentificationfrontend.models.JourneyConfig
import uk.gov.hmrc.minorentityidentificationfrontend.services.JourneyService

import scala.concurrent.Future

trait MockJourneyService extends IdiomaticMockito with ResetMocksAfterEachTest {
  self: Suite =>

  val mockJourneyService: JourneyService = mock[JourneyService]

  def mockGetJourneyConfig(journeyId: String,
                            authInternalId: String)
                           (response: Future[JourneyConfig]): OngoingStubbing[_] =
    when(mockJourneyService.getJourneyConfig(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(authInternalId)
    )).thenReturn(response)
}
