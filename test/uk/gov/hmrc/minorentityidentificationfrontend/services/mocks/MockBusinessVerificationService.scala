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

package uk.gov.hmrc.minorentityidentificationfrontend.services.mocks

import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.mockito.scalatest.{IdiomaticMockito, ResetMocksAfterEachTest}
import org.scalatest.Suite
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.minorentityidentificationfrontend.models.JourneyConfig
import uk.gov.hmrc.minorentityidentificationfrontend.services.BusinessVerificationService

import scala.concurrent.Future

trait MockBusinessVerificationService extends IdiomaticMockito with ResetMocksAfterEachTest {
  self: Suite =>

  val mockBusinessVerificationService: BusinessVerificationService = mock[BusinessVerificationService]

  def createBusinessVerificationJourney(journeyId: String,
                                        sautr: String,
                                        journeyConfig: JourneyConfig)
                                       (response: Future[Option[String]]): Unit =
    when(mockBusinessVerificationService.createBusinessVerificationJourney(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(sautr),
      ArgumentMatchers.eq(journeyConfig)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)

}
