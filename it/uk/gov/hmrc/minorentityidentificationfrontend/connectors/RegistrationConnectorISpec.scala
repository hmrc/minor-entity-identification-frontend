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

package uk.gov.hmrc.minorentityidentificationfrontend.connectors

import play.api.http.Status.UNAUTHORIZED
import play.api.libs.json.{JsArray, Json}
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.minorentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.minorentityidentificationfrontend.models._
import uk.gov.hmrc.minorentityidentificationfrontend.stubs.RegisterStub
import uk.gov.hmrc.minorentityidentificationfrontend.utils.ComponentSpecHelper

class RegistrationConnectorISpec extends ComponentSpecHelper with RegisterStub {

  private val registrationConnector = app.injector.instanceOf[RegistrationConnector]

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  "register trust" should {
    "return Registered" when {
      "the registration has been successful" when {
        "the user has a sautr" in {
          stubRegisterTrust(testSautr, testRegime)(OK, testBackEndRegisteredJson(testSafeId))

          val result = await(registrationConnector.registerTrust(testSautr, testRegime))

          result mustBe Registered(testSafeId)
        }
      }
    }

    "return RegistrationFailed" when {
      "the registration failed with 1 error" in {

        val singleRegistrationFailure: JsArray = Json.arr(Json.obj(
          "code" -> "code1",
          "reason" -> "reason1"
        ))

        stubRegisterTrust(testSautr, testRegime)(OK, testBackendFailedRegistrationJson(failures = singleRegistrationFailure))

        val result: RegistrationStatus = await(registrationConnector.registerTrust(testSautr, testRegime))

        result match {
          case RegistrationFailed(failures) => failures mustBe Array(Failure(code = "code1", reason = "reason1"))
          case _ => fail("Incorrect RegistrationStatus has been returned")
        }
      }

      "the registration failed with more than 1 error" in {

        val multipleRegistrationFailure = Json.arr(Json.obj(
          "code" -> "code1",
          "reason" -> "reason1"
        ),
          Json.obj(
            "code" -> "code2",
            "reason" -> "reason2"
          )
        )

        stubRegisterTrust(testSautr, testRegime)(OK, testBackendFailedRegistrationJson(failures = multipleRegistrationFailure))

        val result = await(registrationConnector.registerTrust(testSautr, testRegime))

        result match {
          case RegistrationFailed(failures) => failures mustBe Array(
            Failure("code1", "reason1"),
            Failure("code2", "reason2")
          )
          case _ => fail("Incorrect RegistrationStatus has been returned")
        }

      }
    }

    "throws InternalServerException" when {
      "the registration http status is different from Ok and INTERNAL_SERVER_ERROR" in {
        stubRegisterTrust(testSautr, testRegime)(UNAUTHORIZED, Json.obj())

        val actualException: InternalServerException = intercept[InternalServerException] {
          await(registrationConnector.registerTrust(testSautr, testRegime))
        }
        actualException.getMessage mustBe s"Unexpected response from Register API - status = 401, body = {}"
      }
    }
  }

  "register Unincorporated association" should {
    "return Registered" when {
      "the registration has been successful" when {
        "the user has a ctutr" in {

          stubRegisterUA(testCtutr, testRegime)(OK, body = testBackEndRegisteredJson(testSafeId))

          val result = await(registrationConnector.registerUA(testCtutr, testRegime))

          result mustBe Registered(testSafeId)
        }
      }
    }

    "return RegistrationFailed" when {
      "the registration failed with 1 error" in {

        val singleRegistrationFailure = Json.arr(Json.obj(
          "code" -> "code1",
          "reason" -> "reason1"
        ))

        stubRegisterUA(testCtutr, testRegime)(OK, testBackendFailedRegistrationJson(failures = singleRegistrationFailure))

        val result = await(registrationConnector.registerUA(testCtutr, testRegime))

        result match {
          case RegistrationFailed(failures) => failures mustBe Array(Failure(code = "code1", reason = "reason1"))
          case _ => fail("Incorrect RegistrationStatus has been returned")
        }
      }

      "the registration failed with more than 1 error" in {

        val multipleRegistrationFailure = Json.arr(Json.obj(
          "code" -> "code1",
          "reason" -> "reason1"
        ),
          Json.obj(
            "code" -> "code2",
            "reason" -> "reason2"
          )
        )

        stubRegisterUA(testCtutr, testRegime)(OK, testBackendFailedRegistrationJson(failures = multipleRegistrationFailure))

        val result = await(registrationConnector.registerUA(testCtutr, testRegime))

        result match {
          case RegistrationFailed(failures) => failures mustBe Array(
            Failure("code1", "reason1"),
            Failure("code2", "reason2")
          )
          case _ => fail("Incorrect RegistrationStatus has been returned")
        }

      }
    }

    "throws InternalServerException" when {
      "the registration http status is different from Ok and INTERNAL_SERVER_ERROR" in {
        stubRegisterUA(testCtutr, testRegime)(UNAUTHORIZED,  body = Json.obj())

        val actualException: InternalServerException = intercept[InternalServerException] {
          await(registrationConnector.registerUA(testCtutr, testRegime))
        }

        actualException.getMessage mustBe s"Unexpected response from Register API - status = 401, body = {}"
      }
    }
  }

}
