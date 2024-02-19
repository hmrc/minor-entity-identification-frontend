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

package uk.gov.hmrc.minorentityidentificationfrontend.utils

import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import uk.gov.hmrc.minorentityidentificationfrontend.helpers.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.models.BusinessEntity.Trusts
import uk.gov.hmrc.minorentityidentificationfrontend.models.JourneyLabels

class MessageHelperSpec extends AnyFlatSpec with BeforeAndAfterEach {

  val initialMessages = Map(
    "en" -> Map("house" -> "house-en"),
    "cy" -> Map("house" -> "house-cy")
  )

  "given a journey config with a welsh and english service name, messages" should "be amended to include it" in {

    val welshServiceName = "This is a welsh service name"
    val englishServiceName = "This is an english service name"

    val labels: JourneyLabels = JourneyLabels(Some(welshServiceName), Some(englishServiceName))

    val actualAmendedMessages = MessagesHelper.amendMessagesWithLabelsFromJourneyConfig(
      initialMessages = initialMessages,
      journeyConfig = testJourneyConfig(Trusts).copy(pageConfig = testDefaultPageConfig
        .copy(optLabels = Some(labels))
        .copy(optServiceName = Some(englishServiceName))
      )
    )

    actualAmendedMessages("en").size should be(2)
    actualAmendedMessages("en")("optServiceName") should be(englishServiceName)

    actualAmendedMessages("cy").size should be(2)
    actualAmendedMessages("cy")("optServiceName") should be(welshServiceName)

  }

  "given a journey config with no welsh and english service name, messages" should "not contain the new translation" in {

    val actualAmendedMessages = MessagesHelper.amendMessagesWithLabelsFromJourneyConfig(
      initialMessages = initialMessages,
      journeyConfig = testJourneyConfig(Trusts).copy(pageConfig = testDefaultPageConfig
        .copy(optLabels = None)
        .copy(optServiceName = None)
      )
    )

    actualAmendedMessages should be(initialMessages)

  }

}
