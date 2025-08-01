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

package uk.gov.hmrc.minorentityidentificationfrontend.views

import play.api.i18n.{Lang, MessagesApi}
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ComponentSpecHelper

import scala.io.{BufferedSource, Source}

class WelshLanguageISpec extends ComponentSpecHelper {

  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  "Welsh messages must have the same keys as English messages" in withEnglishAndWelshMessages { (messageKeysEnglish: List[String], messageKeysWelsh: List[String]) =>
    messageKeysEnglish.foreach(englishKey => messageKeysWelsh must contain(englishKey))
  }

  "English messages must have the same keys as Welsh messages" in withEnglishAndWelshMessages { (messageKeysEnglish: List[String], messageKeysWelsh: List[String]) =>
    messageKeysWelsh.foreach(welshKey => messageKeysEnglish must contain(welshKey))
  }

  "An example of the welsh text can be retrieved" in {
    messagesApi("service.name.default")(Lang("cy")) mustBe "Gwasanaeth Dilysu Endid"
  }

  private def withEnglishAndWelshMessages(testCode: (List[String], List[String]) => Any): Any = {
    val englishMessages: BufferedSource = Source.fromResource("messages.en")
    val welshMessages: BufferedSource = Source.fromResource("messages.cy")
    val messageKeysEnglish: List[String] = getMessageKeys(englishMessages).toList
    val messageKeysWelsh: List[String] = getMessageKeys(welshMessages).toList
    try {
      testCode(messageKeysEnglish, messageKeysWelsh)
    } finally {
      englishMessages.close()
      welshMessages.close()
    }
  }

  private def getMessageKeys(source: Source): Iterator[String] =
    source
      .getLines()
      .map(_.trim)
      .filter(!_.startsWith("#"))
      .filter(_.nonEmpty)
      .map(_.split(' ').head)

}
