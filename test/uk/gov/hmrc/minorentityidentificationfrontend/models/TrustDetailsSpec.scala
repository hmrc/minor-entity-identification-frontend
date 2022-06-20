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

package uk.gov.hmrc.minorentityidentificationfrontend.models

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import play.api.libs.json.{JsObject, Json, __}
import uk.gov.hmrc.minorentityidentificationfrontend.helpers.TestConstants.{testCHRN, testRegistrationStatusJson, testRegistrationStatusRegistered}

class TrustDetailsSpec extends AnyFlatSpec {

  val anEmptyTrustDetails: TrustDetails = TrustDetails(
    optUtr = None,
    optSaPostcode = None,
    optChrn = None,
    optIdentifiersMatch = None,
    optBusinessVerificationStatus = None,
    optRegistrationStatus = None
  )

  "RegistrationStatus Registered" should "create a json with REGISTERED status and an Id" in {

    val expectedRegistrationJson = testRegistrationStatusJson(value = "REGISTERED") ++
      Json.obj("registeredBusinessPartnerId" -> testRegistrationStatusRegistered.registeredBusinessPartnerId)


    List(true, false).foreach(businessVerificationCheck => {
      val actualJson = TrustDetails.writesForJourneyEnd(
        trustDetails = anEmptyTrustDetails.copy(optRegistrationStatus = Some(testRegistrationStatusRegistered)),
        businessVerificationCheck = businessVerificationCheck
      )

      extractRegistrationTag(actualJson) should be(expectedRegistrationJson)

    })

  }

  "RegistrationStatus RegistrationFailed" should "create a json with REGISTRATION_FAILED status and the reasons of failure" in {

    List(true, false).foreach(businessVerificationCheck => {

      val aFailure = Failure("code1", "reason1")

      val actualJson = TrustDetails.writesForJourneyEnd(
        trustDetails = anEmptyTrustDetails.copy(optRegistrationStatus = Some(RegistrationFailed(registrationFailures = Array(aFailure)))),
        businessVerificationCheck = businessVerificationCheck
      )

      extractRegistrationTag(actualJson) should be(testRegistrationStatusJson(value = "REGISTRATION_FAILED") ++ Json.obj(
        "failures" -> Json.arr(
          Json.obj(
            "code" -> aFailure.code,
            "reason" -> aFailure.reason
          )
        )
      ))

    })

  }


  "RegistrationStatus RegistrationNotCalled" should "create a json with REGISTRATION_NOT_CALLED status and no Id" in {

    List(true, false).foreach(businessVerificationCheck => {
      val actualJson = TrustDetails.writesForJourneyEnd(
        trustDetails = anEmptyTrustDetails.copy(optRegistrationStatus = Some(RegistrationNotCalled)),
        businessVerificationCheck = businessVerificationCheck
      )

      extractRegistrationTag(actualJson) should be(testRegistrationStatusJson(value = "REGISTRATION_NOT_CALLED"))

    })
  }

  "no RegistrationStatus at all" should "have REGISTRATION_NOT_CALLED status and no Id" in {

    List(true, false).foreach(businessVerificationCheck => {
      val actualJson = TrustDetails.writesForJourneyEnd(
        trustDetails = anEmptyTrustDetails,
        businessVerificationCheck = businessVerificationCheck
      )
      extractRegistrationTag(actualJson) should be(testRegistrationStatusJson(value = "REGISTRATION_NOT_CALLED"))
    })
  }

  "chrn values with lower case alphabetic characters" should "have the lower case characters converted to upper case" in {

    List(true, false).foreach(businessVerificationCheck => {
      val actualJson = TrustDetails.writesForJourneyEnd(
        trustDetails = anEmptyTrustDetails.copy(optChrn = Some(testCHRN)),
        businessVerificationCheck = businessVerificationCheck
      )
      extractCHRN(actualJson) should be(testCHRN.toUpperCase)
    })

  }

  private def extractCHRN(json: JsObject): String = (json \ "chrn").as[String]

  private def extractRegistrationTag(json: JsObject): JsObject = json.as[JsObject]((__ \ "registration").read)

}
