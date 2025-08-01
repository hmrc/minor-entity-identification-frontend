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

package uk.gov.hmrc.minorentityidentificationfrontend.connectors.mocks

import org.mockito.scalatest.IdiomaticMockito
import org.scalatest.{BeforeAndAfterEach, Suite}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

trait MockAuditConnector extends IdiomaticMockito with BeforeAndAfterEach {
  self: Suite =>

  val mockAuditConnector: AuditConnector = mock[AuditConnector]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuditConnector)
  }
}

