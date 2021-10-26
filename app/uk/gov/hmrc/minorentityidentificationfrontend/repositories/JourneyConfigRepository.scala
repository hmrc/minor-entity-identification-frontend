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

package uk.gov.hmrc.minorentityidentificationfrontend.repositories

import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{Filters, IndexModel, IndexOptions}
import org.mongodb.scala.result.InsertOneResult
import play.api.libs.json._
import uk.gov.hmrc.minorentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.minorentityidentificationfrontend.models.BusinessEntity._
import uk.gov.hmrc.minorentityidentificationfrontend.models.JourneyConfig
import uk.gov.hmrc.minorentityidentificationfrontend.repositories.JourneyConfigRepository._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}

import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class JourneyConfigRepository @Inject()(mongoComponent: MongoComponent,
                                        appConfig: AppConfig)
                                       (implicit ec: ExecutionContext) extends PlayMongoRepository[JsObject](
  collectionName = "minor-entity-identification-frontend",
  mongoComponent = mongoComponent,
  domainFormat = implicitly[Format[JsObject]],
  indexes = Seq(timeToLiveIndex(appConfig.timeToLiveSeconds)),
  extraCodecs = Seq(Codecs.playFormatCodec(journeyConfigFormat))
) {

  def insertJourneyConfig(journeyId: String, authInternalId: String, journeyConfig: JourneyConfig): Future[InsertOneResult] = {
    val document = Json.obj(
      JourneyIdKey -> journeyId,
      AuthInternalIdKey -> authInternalId,
      CreationTimestampKey -> Json.obj("$date" -> Instant.now.toEpochMilli)
    ) ++ Json.toJsObject(journeyConfig)

    collection.insertOne(document).toFuture()
  }

  def getJourneyConfig(journeyId: String, authInternalId: String): Future[Option[JourneyConfig]] =
    collection.find[JourneyConfig](
      Filters.and(Filters.equal(JourneyIdKey, journeyId), Filters.equal(AuthInternalIdKey, authInternalId))
    ).headOption

  def drop: Future[Unit] = collection.drop().toFuture.map(_ => Unit)

}

object JourneyConfigRepository {
  val JourneyIdKey = "_id"
  val AuthInternalIdKey = "authInternalId"
  val CreationTimestampKey = "creationTimestamp"
  val BusinessEntityKey = "businessEntity"

  def timeToLiveIndex(timeToLiveDuration: Long): IndexModel = IndexModel(
    keys = ascending(CreationTimestampKey),
    indexOptions = IndexOptions()
      .name("MinorEntityIdentificationFrontendExpires")
      .expireAfter(timeToLiveDuration, TimeUnit.SECONDS)
  )

  val OverseasCompanyKey = "OverseasCompany"
  val TrustsKey = "Trusts"
  val UnincorporatedAssociationKey = "UnincorporatedAssociation"

  implicit val businessEntityMongoFormat: Format[BusinessEntity] = new Format[BusinessEntity] {
    override def reads(json: JsValue): JsResult[BusinessEntity] = json.validate[String].collect(JsonValidationError("Invalid entity type")) {
      case OverseasCompanyKey => OverseasCompany
      case TrustsKey => Trusts
      case UnincorporatedAssociationKey => UnincorporatedAssociation
    }

    override def writes(partnershipType: BusinessEntity): JsValue = partnershipType match {
      case OverseasCompany => JsString(OverseasCompanyKey)
      case Trusts => JsString(TrustsKey)
      case UnincorporatedAssociation => JsString(UnincorporatedAssociationKey)
    }
  }

  implicit val journeyConfigFormat: OFormat[JourneyConfig] = Json.format[JourneyConfig]

}
