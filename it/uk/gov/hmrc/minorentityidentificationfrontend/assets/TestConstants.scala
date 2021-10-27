/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.minorentityidentificationfrontend.assets

import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.minorentityidentificationfrontend.models.BusinessEntity._
import uk.gov.hmrc.minorentityidentificationfrontend.models.{JourneyConfig, PageConfig}

import java.util.UUID

object TestConstants {

  val testJourneyId: String = UUID.randomUUID().toString
  val testInternalId: String = UUID.randomUUID().toString
  val testContinueUrl: String = "/test"
  val testDeskProServiceId: String = "vrs"
  val testSignOutUrl: String = "/sign-out"
  val testAccessibilityUrl: String = "/accessibility"
  val testOverseasCompanyJourneyConfig: JourneyConfig =
    JourneyConfig(
      testContinueUrl,
      PageConfig(
        None,
        testDeskProServiceId,
        testSignOutUrl,
        testAccessibilityUrl
      ),
      OverseasCompany
    )

  val testTrustsJourneyConfig: JourneyConfig =
    JourneyConfig(
      testContinueUrl,
      PageConfig(
        None,
        testDeskProServiceId,
        testSignOutUrl,
        testAccessibilityUrl
      ),
      Trusts
    )

  val testUnincorporatedAssociationJourneyConfig: JourneyConfig =
    JourneyConfig(
      testContinueUrl,
      PageConfig(
        None,
        testDeskProServiceId,
        testSignOutUrl,
        testAccessibilityUrl
      ),
      UnincorporatedAssociation
    )

  val testUtr: String = "1234567890"
  val testUtrType: String = "sautr"

  val testUtrJson: JsObject = {
    Json.obj(
      "type" -> testUtrType,
      "value" -> testUtr
    )
  }
}
