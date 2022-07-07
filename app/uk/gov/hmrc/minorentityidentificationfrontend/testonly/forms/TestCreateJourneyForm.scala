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

package uk.gov.hmrc.minorentityidentificationfrontend.testonly.forms

import play.api.data.Forms.{boolean, mapping, of, text}
import play.api.data.format.Formatter
import play.api.data.validation.Constraint
import play.api.data.{Form, FormError}
import uk.gov.hmrc.minorentityidentificationfrontend.forms.utils.MappingUtil.optText
import uk.gov.hmrc.minorentityidentificationfrontend.forms.utils.ValidationHelper.validate
import uk.gov.hmrc.minorentityidentificationfrontend.models.BusinessEntity.BusinessEntity
import uk.gov.hmrc.minorentityidentificationfrontend.models._
import uk.gov.hmrc.minorentityidentificationfrontend.testonly.models._

object TestCreateJourneyForm {

  val continueUrl = "continueUrl"
  val serviceName = "serviceName"
  val deskProServiceId = "deskProServiceId"
  val signOutUrl = "signOutUrl"
  val accessibilityUrl = "accessibilityUrl"
  val businessVerificationCheck = "businessVerificationCheck"
  val regime = "regime"
  val labels = "labels"
  val welshServiceName = "welshServiceName"

  def continueUrlEmpty: Constraint[String] = Constraint("continue_url.not_entered")(
    continueUr => validate(
      constraint = continueUr.isEmpty,
      errMsg = "Continue URL not entered"
    )
  )

  def deskProServiceIdEmpty: Constraint[String] = Constraint("desk_pro_service_id.not_entered")(
    serviceId => validate(
      constraint = serviceId.isEmpty,
      errMsg = "DeskPro Service Identifier is not entered"
    )
  )

  def signOutUrlEmpty: Constraint[String] = Constraint("sign_out_url.not_entered")(
    signOutUrl => validate(
      constraint = signOutUrl.isEmpty,
      errMsg = "Sign Out Url is not entered"
    )
  )

  def accessibilityUrlEmpty: Constraint[String] = Constraint("sign_out_url.not_entered")(
    accessibilityUrl => validate(
      constraint = accessibilityUrl.isEmpty,
      errMsg = "Accessibility Url is not entered"
    )
  )

  def regimeEmpty: Constraint[String] = Constraint("regime.not_entered")(
    regime => validate(
      constraint = regime.isEmpty,
      errMsg = "Regime is not entered"
    )
  )

  def knownFactsMapping(error: String): Formatter[KnownFactsMatchStub] = new Formatter[KnownFactsMatchStub] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], KnownFactsMatchStub] = {
      data.get(key) match {
        case Some("GBResponse") =>
          data.get("postcode") match {
            case Some(postcode) => Right(GBResponse(postcode))
            case _ => Left(Seq(FormError("postcode", error)))
          }
        case Some("Not Found") => Right(KnownFactsNotFound)
        case Some("Abroad Response") => Right(AbroadResponse)
        case _ => Left(Seq(FormError(key, error)))
      }
    }

    override def unbind(key: String, value: KnownFactsMatchStub): Map[String, String] = {
      val newValue = value match {
        case GBResponse(_) => "GBResponse"
        case KnownFactsNotFound => "Not Found"
        case AbroadResponse => "Abroad Response"
      }
      Map(key -> newValue)
    }
  }

  def registrationMapping(error: String): Formatter[RegistrationStatus] = new Formatter[RegistrationStatus] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], RegistrationStatus] = {
      data.get(key) match {
        case Some("Pass") =>
          data.get("safeID") match {
            case Some(id) => Right(Registered(id))
            case _ => Left(Seq(FormError("safeID", error)))
          }
        case Some(_) =>
          Right(RegistrationFailed(
            Array(
              Failure("INVALID_PAYLOAD", "Request has not passed validation. Invalid Payload."),
              Failure("INVALID_REGIME", "Request has not passed validation. Invalid Regime.")
            )))
        case _ => Left(Seq(FormError(key, error)))
      }
    }

    override def unbind(key: String, value: RegistrationStatus): Map[String, String] = {
      value match {
        case Registered(id) => Map(key -> "Pass", "safeID" -> id)
        case RegistrationFailed(_) => Map(key -> "Fail")
      }
    }
  }

  def stringMapping(error: String): Formatter[String] = new Formatter[String] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] = {
      data.get(key) match {
        case Some(value) => Right(value)
        case _ => Left(Seq(FormError(key, error)))
      }
    }

    override def unbind(key: String, value: String): Map[String, String] = {
      Map(key -> value)
    }
  }

  def form(businessEntity: BusinessEntity): Form[JourneyConfig] = {
    Form(mapping(
      continueUrl -> text.verifying(continueUrlEmpty),
      serviceName -> optText,
      deskProServiceId -> text.verifying(deskProServiceIdEmpty),
      signOutUrl -> text.verifying(signOutUrlEmpty),
      accessibilityUrl -> text.verifying(accessibilityUrlEmpty),
      businessVerificationCheck -> boolean,
      regime -> text.verifying(regimeEmpty),
      welshServiceName -> optText
    )((continueUrl, serviceName, deskProServiceId, signOutUrl, accessibilityUrl, businessVerificationCheck, regime, welshServiceName) =>
      JourneyConfig.apply(continueUrl, PageConfig(serviceName, deskProServiceId, welshServiceName, signOutUrl, accessibilityUrl), businessEntity, businessVerificationCheck, regime)
    )(journeyConfig =>
      Some(journeyConfig.continueUrl,
        journeyConfig.pageConfig.optServiceName,
        journeyConfig.pageConfig.deskProServiceId,
        journeyConfig.pageConfig.signOutUrl,
        journeyConfig.pageConfig.accessibilityUrl,
        journeyConfig.businessVerificationCheck,
        journeyConfig.regime,
        journeyConfig.pageConfig.optLabels.map(_.welshServiceName)
      )
    ))
  }

  def newForm(businessEntity: BusinessEntity): Form[TestSetup] = {
    Form(mapping(
      continueUrl -> text.verifying(continueUrlEmpty),
      serviceName -> optText,
      deskProServiceId -> text.verifying(deskProServiceIdEmpty),
      signOutUrl -> text.verifying(signOutUrlEmpty),
      accessibilityUrl -> text.verifying(accessibilityUrlEmpty),
      businessVerificationCheck -> boolean,
      regime -> text.verifying(regimeEmpty),
      welshServiceName -> optText,
      "knownFactsMatch" -> of(knownFactsMapping("Known Facts Stub not entered")),
      "businessVerificationStub" -> of(stringMapping("Business Verification Stub Not Entered")),
      "registrationStub" -> of(registrationMapping("Registration Stub Not Entered"))
    )((continueUrl, serviceName, deskProServiceId, signOutUrl, accessibilityUrl, businessVerificationCheck, regime, welshServiceName, knownFactsStub, businessVerificationStub, registrationStub) =>
      TestSetup.apply(
        JourneyConfig.apply(
          continueUrl,
          PageConfig(serviceName, deskProServiceId, welshServiceName, signOutUrl, accessibilityUrl),
          businessEntity,
          businessVerificationCheck,
          regime
        ),
        Stubs.apply(knownFactsStub, businessVerificationStub, registrationStub))
    )(testConfig =>
      Some(testConfig.journeyConfig.continueUrl,
        testConfig.journeyConfig.pageConfig.optServiceName,
        testConfig.journeyConfig.pageConfig.deskProServiceId,
        testConfig.journeyConfig.pageConfig.signOutUrl,
        testConfig.journeyConfig.pageConfig.accessibilityUrl,
        testConfig.journeyConfig.businessVerificationCheck,
        testConfig.journeyConfig.regime,
        testConfig.journeyConfig.pageConfig.optLabels.map(_.welshServiceName),
        testConfig.stubs.knownFactsMatch,
        testConfig.stubs.businessVerificationStub,
        testConfig.stubs.registrationStub
      )
    ))
  }


}
