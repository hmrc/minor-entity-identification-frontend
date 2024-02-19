/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.minorentityidentificationfrontend.connectors.mocks

import org.mockito.ArgumentMatchers
import org.mockito.scalatest.{IdiomaticMockito, ResetMocksAfterEachTest}
import org.scalatest.Suite
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.minorentityidentificationfrontend.connectors.CreateJourneyConnector

trait MockCreateJourneyConnector extends IdiomaticMockito with ResetMocksAfterEachTest {
  self: Suite =>

  val mockCreateJourneyConnector: CreateJourneyConnector = mock[CreateJourneyConnector]

  def verifyCreateJourney(): Unit =
    mockCreateJourneyConnector.createJourney()(ArgumentMatchers.any[HeaderCarrier]) was called

}