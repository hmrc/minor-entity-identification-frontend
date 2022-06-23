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

import play.api.data.Form
import play.api.data.Forms.{boolean, mapping, text}
import play.api.data.validation.Constraint
import uk.gov.hmrc.minorentityidentificationfrontend.forms.utils.MappingUtil.optText
import uk.gov.hmrc.minorentityidentificationfrontend.forms.utils.ValidationHelper.validate
import uk.gov.hmrc.minorentityidentificationfrontend.models.BusinessEntity.BusinessEntity
import uk.gov.hmrc.minorentityidentificationfrontend.models.{JourneyConfig, PageConfig}

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

}
