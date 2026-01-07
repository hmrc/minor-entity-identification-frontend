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

package uk.gov.hmrc.minorentityidentificationfrontend.repositories.mocks

import org.scalatest.Suite
import org.mockito.Mockito.verify
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.minorentityidentificationfrontend.models.JourneyConfig
import uk.gov.hmrc.minorentityidentificationfrontend.repositories.JourneyConfigRepository

trait MockJourneyConfigRepository extends MockitoSugar {
  self: Suite =>

  val mockJourneyConfigRepository: JourneyConfigRepository = mock[JourneyConfigRepository]

  def verifyInsertJourneyConfig(journeyId: String,
                                internalId: String,
                                journeyConfig: JourneyConfig): Unit =
    verify(mockJourneyConfigRepository).insertJourneyConfig(
      journeyId,
      internalId,
      journeyConfig
    )

  def verifyGetJourneyConfig(journeyId: String, internalId: String): Unit =
    verify(mockJourneyConfigRepository).getJourneyConfig(
      journeyId,
      internalId
    )

}

