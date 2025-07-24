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

package config

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.minorentityidentificationfrontend.config.AppConfig

class AppConfigSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  val noOfISO3166CountryCodes: Int = 249

  "AppConfig" should {

    "provide the name of a country identified by a given country code" when {

      "the preferred language is english" in {

        appConfig.getCountryName(countryCode = "AU") mustBe "Australia"

      }

      "the preferred language is welsh" in {

        appConfig.getCountryName(countryCode = "GB", langCode = "cy") mustBe "Y Deyrnas Unedig"

      }
    }

    "provide 249 ISO-3166 Country codes" when {

      "the preferred language is english" in {

        appConfig.getOrderedCountryListByLanguage("en").size mustBe noOfISO3166CountryCodes

      }

      "the preferred language is welsh" in {

        appConfig.getOrderedCountryListByLanguage("cy").size mustBe noOfISO3166CountryCodes

      }

    }

    "raise an exception" when {

      "a country name is requested, but the code is invalid" in {

        intercept[InternalServerException]{
          appConfig.getCountryName(countryCode = "XY")
        }

      }

      "a country list file name is incorrectly defined" in {

        intercept[InternalServerException]{
          appConfig.getCountryList(fileName = "invalid")
        }

      }
    }

  }
}
