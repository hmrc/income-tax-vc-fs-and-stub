/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxvcfsandstub.controllers

import com.google.inject.{Inject, Singleton}
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.incometaxvcfsandstub.models.TaxYear
import uk.gov.hmrc.incometaxvcfsandstub.models.customUser.{CreateCustomUserModel, CustomUserResponse, UserChannel}
import uk.gov.hmrc.incometaxvcfsandstub.repositories.DataRepository
import uk.gov.hmrc.incometaxvcfsandstub.utils.{BusinessDataUtils, CustomUserUtils, ObligationsDataUtils}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateCustomUserController @Inject()(cc: MessagesControllerComponents, dataRepository: DataRepository)(implicit val ec: ExecutionContext) extends FrontendController(cc) with Logging {

  private final val customUserNino = "CR000000A"
  private final val customUserMtdid = "XTIT02468246824"
  private final val customUserUTR = "2468246824"

  private def overrideBusinessDetailsUrl(mtdid: String): String = s"/etmp/RESTAdapter/itsa/taxpayer/business-details?mtdReference=$mtdid"
  private def overrideOpenObligationsUrl: String = s"/enterprise/obligation-data/nino/CR000000A/ITSA?status=O"
  private def overrideFulfilledObligationsUrl: String = s"/enterprise/obligation-data/nino/CR000000A/ITSA?status=F"
  private def overrideDatedObligationsUrl: String = s"/enterprise/obligation-data/nino/CR000000A/ITSA?from=2026-04-06&to=2027-04-05"

  def createCustomUser(): Action[AnyContent] =
    Action.async { implicit request =>
      request.body.asJson match {
        case None => Future.successful(BadRequest("Expected JSON body - No JSON found"))
        case Some(json) => json.validate[CreateCustomUserModel].fold(
          invalid = _ => Future.successful(BadRequest("Invalid JSON data")),
          valid = customUserData => {
            TaxYear.createTaxYearGivenTaxYearRange(customUserData.previousTaxYear) match {
              case Some(taxYear) =>
                val businessUrl = overrideBusinessDetailsUrl(customUserMtdid)

                val decoupledModel = CustomUserUtils.translateCode(customUserData.userCode)

                val businessData = BusinessDataUtils.createBusinessData(decoupledModel.incomeSources.activeSoleTrader, decoupledModel.incomeSources.ceasedSoleTrader, decoupledModel.incomeSources.latentSoleTrader)
                val propertyData = BusinessDataUtils.createPropertyData(decoupledModel.incomeSources.activeUkProperty, decoupledModel.incomeSources.activeForeignProperty)

                val obligationsData = ObligationsDataUtils.createCustomUserObligationsData(decoupledModel.obligations)
                for {
                  businessUpdate             <- dataRepository.clearAndReplace(businessUrl, BusinessDataUtils.businessDataKey, businessData)
                  propertyUpdate             <- dataRepository.clearAndReplace(businessUrl, BusinessDataUtils.propertyDataKey, propertyData)
                  channelUpdate              <- dataRepository.clearAndReplaceField(businessUrl, BusinessDataUtils.channelKey, UserChannel.getApiValueForUserChannel(decoupledModel.incomeSources.userChannel))
                  openObligationsUpdate      <- dataRepository.clearAndReplace(overrideOpenObligationsUrl, ObligationsDataUtils.obligationsDataKey, obligationsData.openObligationsData)
                  fulfilledObligationsUpdate <- dataRepository.clearAndReplace(overrideFulfilledObligationsUrl, ObligationsDataUtils.obligationsDataKey, obligationsData.fulfilledObligationsData)
                  datedObligationsUpdate     <- dataRepository.clearAndReplace(overrideDatedObligationsUrl, ObligationsDataUtils.obligationsDataKey, obligationsData.datedData)
                } yield {
                  (businessUpdate.wasAcknowledged(), propertyUpdate.wasAcknowledged(), openObligationsUpdate.wasAcknowledged(), channelUpdate.wasAcknowledged(), fulfilledObligationsUpdate.wasAcknowledged(), datedObligationsUpdate.wasAcknowledged()) match {
                    case (true, true, true, true, true, true) =>
                      logger.info(s"Successfully created custom user with code ${customUserData.userCode}")
                      val response = CustomUserResponse(
                        customUserNino,
                        decoupledModel.isAgent,
                        decoupledModel.isSupportingAgent,
                        customUserUTR,
                        decoupledModel.itsaStatus.cyMinusOneCrystallisationStatus,
                        decoupledModel.itsaStatus.cyMinusOneItsaStatus,
                        decoupledModel.itsaStatus.cyItsaStatus,
                        decoupledModel.itsaStatus.cyPlusOneItsaStatus)
                      Created(Json.toJson(response))
                    case _ =>
                      logger.error(s"Failed to create custom user with code ${customUserData.userCode}")
                      InternalServerError("Failed to create custom user")
                  }
                }
              case None =>
                logger.warn(s"Invalid tax year provided: ${customUserData.previousTaxYear}")
                Future.successful(BadRequest("Invalid tax year format"))
            }
          }
        )
      }
    }
}
