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

package uk.gov.hmrc.minorentityidentificationfrontend.stubs

import play.api.libs.json._
import uk.gov.hmrc.minorentityidentificationfrontend.models._
import uk.gov.hmrc.minorentityidentificationfrontend.utils.{WiremockHelper, WiremockMethods}

trait StorageStub extends WiremockMethods {

  def stubStoreUtr(journeyId: String, utr: Utr)(status: Int): Unit =
    when(method = PUT,
      uri = s"/minor-entity-identification/journey/$journeyId/utr",
      body = Json.obj(
        "type" -> utr.utrType,
        "value" -> utr.value
      )
    ).thenReturn(
      status = status
    )

  def stubStoreStoreTrustsKnownFacts(journeyId: String, expBody: JsValue)(status: Int): Unit =
    when(method = PUT, uri = s"/minor-entity-identification/journey/$journeyId/trustKnownFacts", expBody)
      .thenReturn(status = status)

  def stubStoreIdentifiersMatch(journeyId: String, identifiersMatch: String)(status: Int): Unit =
    when(method = PUT, uri = s"/minor-entity-identification/journey/$journeyId/identifiersMatch", body = JsString(identifiersMatch))
      .thenReturn(status = status)

  def stubRetrieveUtr(journeyId: String)(status: Int, body: JsObject = Json.obj()): Unit =
    when(method = GET, uri = s"/minor-entity-identification/journey/$journeyId/utr").thenReturn(
      status = status,
      body = body
    )

  def stubRemoveUtr(journeyId: String)(status: Int, body: JsObject = Json.obj()): Unit =
    when(method = DELETE, uri = s"/minor-entity-identification/journey/$journeyId/utr").thenReturn(
      status = status,
      body = body
    )

  def stubStoreOverseasTaxIdentifiersCountry(journeyId: String, taxIdentifiersCountry: String)(status: Int): Unit =
    when(method = PUT,
      uri = s"/minor-entity-identification/journey/$journeyId/country", body = JsString(taxIdentifiersCountry)
    ).thenReturn(
      status = status
    )

  def stubRetrieveOverseasTaxIdentifiersCountry(journeyId: String)(status: Int, country: String = "") : Unit =
    when(method = GET,
      uri = s"/minor-entity-identification/journey/$journeyId/country"
    ).thenReturn(
      status = status,
      body = JsString(country)
    )

  def stubRemoveOverseasTaxIdentifiersCountry(journeyId: String)(status: Int, body: String = ""): Unit =
    when(method = DELETE,
      uri = s"/minor-entity-identification/journey/$journeyId/country"
    ).thenReturn(
      status = status,
      body = body
    )

  def verifyStoreOverseasTaxIdentifierCountry(journeyId: String, overseasTaxIdentifierCountry: String): Unit =
    WiremockHelper.verifyPut(
      uri = s"/minor-entity-identification/journey/$journeyId/country",
      optBody = Some(JsString(overseasTaxIdentifierCountry).toString())
    )

  def stubStoreOverseasTaxIdentifier(journeyId: String, overseasTaxIdentifier: String)(status: Int): Unit =
    when(method = PUT,
      uri = s"/minor-entity-identification/journey/$journeyId/overseasTaxIdentifier",
      body = JsString(overseasTaxIdentifier)
    ).thenReturn(
      status = status
    )

  def stubRetrieveOverseasTaxIdentifier(journeyId: String)(status: Int, overseasTaxIdentifier: String = "") : Unit =
    when(method = GET,
      uri = s"/minor-entity-identification/journey/$journeyId/overseasTaxIdentifier"
    ).thenReturn(
      status = status,
      body = JsString(overseasTaxIdentifier)
    )

  def stubRemoveOverseasTaxIdentifier(journeyId: String)(status: Int, body: String = ""): Unit =
    when(method = DELETE,
      uri = s"/minor-entity-identification/journey/$journeyId/overseasTaxIdentifier"
    ).thenReturn(
      status = status,
      body = body
    )


  def stubStorePostcode(journeyId: String, saPostcode: String)(status: Int): Unit =
    when(method = PUT,
      uri = s"/minor-entity-identification/journey/$journeyId/postcode",
      body = JsString(saPostcode)
    ).thenReturn(
      status = status
    )

  def stubRemovePostcode(journeyId: String)(status: Int, body: String = ""): Unit =
    when(method = DELETE,
      uri = s"/minor-entity-identification/journey/$journeyId/postcode"
    ).thenReturn(
      status = status,
      body = body
    )

  def stubStoreCHRN(journeyId: String, chrn: String)(status: Int): Unit =
    when(method = PUT,
      uri = s"/minor-entity-identification/journey/$journeyId/chrn",
      body = JsString(chrn)
    ).thenReturn(
      status = status
    )

  def stubRetrievePostcode(journeyId: String)(status: Int, postcode: String = ""): Unit =
    when(method = GET,
      uri = s"/minor-entity-identification/journey/$journeyId/postcode"
    ).thenReturn(
      status = status,
      body = JsString(postcode)
    )

  def stubRemoveCHRN(journeyId: String)(status: Int, body: String = ""): Unit =
    when(method = DELETE,
      uri = s"/minor-entity-identification/journey/$journeyId/chrn"
    ).thenReturn(
      status = status,
      body = body
    )

  def stubRetrieveCHRN(journeyId: String)(status: Int, optCharityHMRCReferenceNumber: String = ""): Unit =
    when(method = GET,
      uri = s"/minor-entity-identification/journey/$journeyId/chrn"
    ).thenReturn(
      status = status,
      body = JsString(optCharityHMRCReferenceNumber)
    )

  def stubRemoveAllData(journeyId: String)(status: Int, body: String = ""): Unit =
    when(method = DELETE,
      uri = s"/minor-entity-identification/journey/$journeyId"
    ).thenReturn(
      status = status,
      body = body
    )

  def verifyStoreTrustsKnownFacts(journeyId: String, expBody: JsValue): Unit =
    WiremockHelper.verifyPut(
      uri = s"/minor-entity-identification/journey/$journeyId/trustKnownFacts",
      optBody = Some(Json.stringify(expBody))
    )

  def verifyStoreIdentifiersMatch(journeyId: String, expBody: JsValue): Unit =
    WiremockHelper.verifyPut(
      uri = s"/minor-entity-identification/journey/$journeyId/identifiersMatch",
      optBody = Some(Json.stringify(expBody))
    )

  def verifyRemoveCHRN(journeyId: String): Unit =
    WiremockHelper.verifyDelete(uri = s"/minor-entity-identification/journey/$journeyId/chrn")

  def verifyRemoveUtr(journeyId: String): Unit =
    WiremockHelper.verifyDelete(uri = s"/minor-entity-identification/journey/$journeyId/utr")

  def verifyRemovePostcode(journeyId: String): Unit =
    WiremockHelper.verifyDelete(uri = s"/minor-entity-identification/journey/$journeyId/postcode")

  def stubRetrieveIdentifiersMatch(journeyId: String)(status: Int, identifiersMatch: String = ""): Unit =
    when(method = GET,
      uri = s"/minor-entity-identification/journey/$journeyId/identifiersMatch"
    ).thenReturn(
      status = status,
      body = JsString(identifiersMatch)
    )

  def stubStoreBusinessVerificationStatus(journeyId: String,
                                          businessVerificationStatus: BusinessVerificationStatus
                                         )(status: Int): Unit =
    when(method = PUT,
      uri = s"/minor-entity-identification/journey/$journeyId/businessVerification",
      body = Json.toJson(businessVerificationStatus)
    ).thenReturn(
      status = status
    )

  def stubStoreBusinessVerificationStatus(journeyId: String,
                                          expBody: JsValue
                                         )(status: Int): Unit =
    when(method = PUT,
      uri = s"/minor-entity-identification/journey/$journeyId/businessVerification",
      body = expBody
    ).thenReturn(
      status = status
    )

  def stubStoreRegistrationStatus(journeyId: String, registrationStatus: RegistrationStatus)(status: Int): Unit =
    stubStoreRegistrationStatus(journeyId, Json.toJsObject(registrationStatus))(status)

  def stubStoreRegistrationStatus(journeyId: String, jsonBody: JsObject)(status: Int): Unit = {
    when(method = PUT,
      uri = s"/minor-entity-identification/journey/$journeyId/registration",
      body = jsonBody
    ).thenReturn(
      status = status
    )
  }

  def verifyStoreRegistrationStatus(journeyId: String, registrationStatus: RegistrationStatus): Unit = {
    val jsonBody = Json.toJsObject(registrationStatus)
    WiremockHelper.verifyPut(uri = s"/minor-entity-identification/journey/$journeyId/registration", optBody = Some(jsonBody.toString()))
  }

  def verifyStoreRegistrationStatus(journeyId: String, jsonBody: JsObject): Unit =
    WiremockHelper.verifyPut(uri = s"/minor-entity-identification/journey/$journeyId/registration", optBody = Some(jsonBody.toString()))

  def stubRetrieveBusinessVerificationStatus(journeyId: String)(status: Int, body: JsValue = Json.obj()): Unit =
    when(method = GET,
      uri = s"/minor-entity-identification/journey/$journeyId/businessVerification"
    ).thenReturn(
      status = status,
      body = body
    )

  def verifyStoreBusinessVerificationStatus(journeyId: String, businessVerificationStatus: BusinessVerificationStatus): Unit = {
    val jsonBody = Json.toJson(businessVerificationStatus)
    WiremockHelper.verifyPut(uri = s"/minor-entity-identification/journey/$journeyId/businessVerification", optBody = Some(jsonBody.toString()))
  }

  def verifyStoreBusinessVerificationStatus(journeyId: String, expBody: JsObject): Unit =
    WiremockHelper.verifyPut(uri = s"/minor-entity-identification/journey/$journeyId/businessVerification", optBody = Some(Json.stringify(expBody)))

  def stubRetrieveRegistrationStatus(journeyId: String)(status: Int, body: JsValue = Json.obj()): Unit =
    when(method = GET,
      uri = s"/minor-entity-identification/journey/$journeyId/registration"
    ).thenReturn(
      status = status,
      body = body
    )

  def stubRetrieveEntityDetails(journeyId: String)(status: Int, body: JsValue = Json.obj()): Unit = {
    when(method = GET, uri =  s"/minor-entity-identification/journey/$journeyId").thenReturn(status = status, body = body)
  }

  def verifyStoreOverseasTaxIdentifier(journeyId: String, overseasTaxIdentifier: String): Unit =
    WiremockHelper.verifyPut(
      uri = s"/minor-entity-identification/journey/$journeyId/overseasTaxIdentifier",
      optBody = Some(JsString(overseasTaxIdentifier).toString())
    )

  def verifyRemoveOverseasTaxIdentifier(journeyId: String): Unit =
    WiremockHelper.verifyDelete(uri = s"/minor-entity-identification/journey/$journeyId/overseasTaxIdentifier")

  def verifyRemoveOverseasTaxIdentifiersCountry(journeyId: String): Unit =
    WiremockHelper.verifyDelete(uri = s"/minor-entity-identification/journey/$journeyId/country")

  def verifyRetrieveEntityDetails(journeyId: String, times: Int = 1): Unit =
    WiremockHelper.verifyGet(times,s"/minor-entity-identification/journey/$journeyId")
}
