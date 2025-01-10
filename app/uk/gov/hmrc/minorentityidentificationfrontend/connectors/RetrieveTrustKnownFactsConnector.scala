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

import play.api.http.Status.{NOT_FOUND, OK}
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, InternalServerException, StringContextOps}
import uk.gov.hmrc.minorentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.minorentityidentificationfrontend.connectors.KnownFactsHttpParser.KnownFactsHttpReads
import uk.gov.hmrc.minorentityidentificationfrontend.models.TrustKnownFacts

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveTrustKnownFactsConnector @Inject()(httpClient: HttpClientV2, appConfig: AppConfig)(implicit ec: ExecutionContext) {

  def retrieveTrustKnownFacts(sautr: String)(implicit hc: HeaderCarrier): Future[Option[TrustKnownFacts]] = {
    httpClient.get(url"${appConfig.retrieveTrustsKnownFactsUrl(sautr)}").execute(
      KnownFactsHttpReads,
      ec)
  }
}

object KnownFactsHttpParser {
  private val TrustKey = "getTrust"
  private val CorrespondenceKey = "correspondence"
  private val AbroadIndicator = "abroadIndicator"
  private val DeclarationKey = "declaration"
  private val AddressKey = "address"
  private val PostcodeKey = "postCode"

  private val knownFactsRead: Reads[TrustKnownFacts] = (
    (JsPath \ CorrespondenceKey \ AddressKey \ PostcodeKey).readNullable[String] and
      (JsPath \ DeclarationKey \ AddressKey \ PostcodeKey).readNullable[String] and
      (JsPath \ CorrespondenceKey \ AbroadIndicator).read[Boolean]
    ) (TrustKnownFacts.apply _)

  implicit object KnownFactsHttpReads extends HttpReads[Option[TrustKnownFacts]] {
    override def read(method: String, url: String, response: HttpResponse): Option[TrustKnownFacts] = {
      response.status match {
        case OK =>
          (response.json \ TrustKey).validate[TrustKnownFacts](knownFactsRead) match {
            case JsSuccess(details, _) => Some(details)
            case JsError(errors) =>
              throw new InternalServerException(s"Invalid JSON returned from Trusts Known Facts Call. Errors - $errors")
          }
        case NOT_FOUND => None
        case status =>
          throw new InternalServerException(s"Unexpected status from Trusts Known Facts Call. Status returned - $status")
      }
    }
  }

}
