
package uk.gov.hmrc.minorentityidentificationfrontend.journeys

import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers.{CREATED, NOT_FOUND, OK}
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.models.Sautr
import uk.gov.hmrc.minorentityidentificationfrontend.stubs.{AuthStub, JourneyStub, MinorEntityIdentificationStub}
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ComponentSpecHelper

class OverseasJourneyISpec extends ComponentSpecHelper with AuthStub with JourneyStub with MinorEntityIdentificationStub {

  "An overseas user completes a journey" in {
    val testJourneyConfigJson: JsObject = Json.obj(
      "continueUrl" -> testContinueUrl,
      "deskProServiceId" -> testDeskProServiceId,
      "signOutUrl" -> testSignOutUrl,
      "accessibilityUrl" -> testAccessibilityUrl
    )

    stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
    stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))
    post("/minor-entity-identification/api/overseas-company-journey", testJourneyConfigJson)

    stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
    get(s"/identify-your-overseas-business/$testJourneyId/non-uk-company-utr")
    stubStoreUtr(testJourneyId, Sautr(testUtr))(OK)
    post(s"/identify-your-overseas-business/$testJourneyId/non-uk-company-utr")("utr" -> testUtr)

    stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
    stubRetrieveUtr(testJourneyId)(NOT_FOUND)
    get(s"/identify-your-overseas-business/$testJourneyId/check-your-answers-business")
    post(s"/identify-your-overseas-business/$testJourneyId/check-your-answers-business")()

  }

}
