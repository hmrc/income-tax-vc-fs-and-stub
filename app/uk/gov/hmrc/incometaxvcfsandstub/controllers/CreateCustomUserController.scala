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
import play.api.libs.json.Json
import play.api.{Logger, Logging}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.incometaxvcfsandstub.models.customUser.{CreateCustomUserModel, CustomUserResponse}
import uk.gov.hmrc.incometaxvcfsandstub.models.{CrystallisationStatus, TaxYear}
import uk.gov.hmrc.incometaxvcfsandstub.repositories.DataRepository
import uk.gov.hmrc.incometaxvcfsandstub.utils.{BusinessDataUtils, CustomUserUtils}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateCustomUserController @Inject()(cc: MessagesControllerComponents, dataRepository: DataRepository)(implicit val ec: ExecutionContext) extends FrontendController(cc) with Logging {

  private final val customUserNino = "CR000000A"
  private final val customUserMtdid = "XTIT02468246824"
  private final val customUserUTR = "2468246824"

  private final val itsaStatusDataKey = "response"

  private def overrideBusinessDetailsUrl(mtdid: String): String = s"/etmp/RESTAdapter/itsa/taxpayer/business-details?mtdReference=$mtdid"

  private def createOverwriteCalculationListUrl(nino: String, taxYear: TaxYear): String = {
    if (taxYear.endYear >= 2024) {
      Logger("application").info(s"[CalculationController][createOverwriteCalculationListUrl] Overwriting calculation details TYS")
      s"/itsa/income-tax/v1/${taxYear.rangeShort}/view/calculations/liability/$nino"
    } else {
      Logger("application").info(s"[CalculationController][createOverwriteCalculationListUrl] Overwriting calculation details legacy")
      s"/income-tax/list-of-calculation-results/$nino?taxYear=${taxYear.endYearString}"
    }
  }

  private def itsaStatusUrl = s"/itsd/person-itd/itsa-status/$customUserNino?taxYear=25-26&futureYears=true&history=false"

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
                
                val businessData = BusinessDataUtils.createBusinessData(decoupledModel.incomeSources.activeSoleTrader, false)
                val propertyData = BusinessDataUtils.createPropertyData(decoupledModel.incomeSources.activeUkProperty, decoupledModel.incomeSources.activeForeignProperty)
                for {
                  businessUpdate <- dataRepository.clearAndReplace(businessUrl, BusinessDataUtils.businessDataKey, businessData)
                  _ = Logger("application").info(s"Business details update for custom user with code ${customUserData.userCode} was acknowledged: ${businessUpdate.wasAcknowledged()}. URL: $businessUrl")
                  propertyUpdate <- dataRepository.clearAndReplace(businessUrl, BusinessDataUtils.propertyDataKey, propertyData)
                  _ = Logger("application").info(s"Property details update for custom user with code ${customUserData.userCode} was acknowledged: ${propertyUpdate.wasAcknowledged()}. URL: $businessUrl")
                } yield {
                  (businessUpdate.wasAcknowledged(), propertyUpdate.wasAcknowledged()) match {
                    case (true, true) =>
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
