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

package uk.gov.hmrc.minorentityidentificationfrontend.services

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.minorentityidentificationfrontend.models.{Ctutr, Sautr}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuditService @Inject()(auditConnector: AuditConnector, storageService: StorageService) {

  def auditJourney(journeyId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {
    for {
      optUtr <- storageService.retrieveUtr(journeyId)
    } yield Some(optUtr) match {

      case Some(optUtr) => {
        val optUtrBlock = optUtr match {
          case Some(utr: Ctutr) => Json.obj("userCTUTR" -> utr.value)
          case Some(utr: Sautr) => Json.obj("userSAUTR" -> utr.value)
          case None => Json.obj()
        }

        Json.obj(
          "businessType" -> "Overseas Company",
          "etmpPartyType" -> "55"
        ) ++ optUtrBlock
      }
      case _ =>
        throw new InternalServerException(s"Not enough information to audit minor entity journey for Journey ID $journeyId")
    }
  }.map {
    auditJson =>
      auditConnector.sendExplicitAudit(
        auditType = "OverseasCompanyRegistration",
        detail = auditJson
      )
  }
}