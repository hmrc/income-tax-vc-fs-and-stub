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

package uk.gov.hmrc.incometaxvcfsandstub.utils

import uk.gov.hmrc.incometaxvcfsandstub.models.customUser.{DecoupledCustomUserModel, UserType}

object CustomUserUtils {
  
  val userNino = "CR000000A"
  
  def translateCode(userCode: String): DecoupledCustomUserModel = {
    val codeParts = userCode.split("-")
    
    val userType = UserType.userTypeFromCode(codeParts(0))
    val numberOfSoleTraders = codeParts(1).substring(1).toInt
    val activeUkProperty = codeParts(2).substring(1) match {
      case "1" => true
      case "0" => false
      case _ => throw new IllegalArgumentException(s"Invalid active UK property code: ${codeParts(2)}")
    }
    val activeForeignProperty = codeParts(3).substring(1) match {
      case "1" => true
      case "0" => false
      case _ => throw new IllegalArgumentException(s"Invalid active foreign property code: ${codeParts(3)}")
    }
    val previousYearCrystallisationStatus = previousYearCrystallisationStatusFromCode(codeParts(4))
    val previousYearITSAStatus = itsaStatusFromCode(codeParts(5).substring(2))
    val currentYearITSAStatus = itsaStatusFromCode(codeParts(6).substring(2))
    val nextYearITSAStatus = itsaStatusFromCode(codeParts(7).substring(2))
    
    DecoupledCustomUserModel(
      userType,
      numberOfSoleTraders,
      activeUkProperty,
      activeForeignProperty,
      previousYearCrystallisationStatus.toString,
      previousYearITSAStatus,
      currentYearITSAStatus,
      nextYearITSAStatus
    )
  }

  private def itsaStatusFromCode(code: String): String = {
    code match {
      case "1" => "Annual"
      case "2" => "MTD Voluntary"
      case "3" => "MTD Mandated"
      case "4" => "MTD Exempt"
      case "5" => "Digitally Exempt"
      case "6" => "No Status"
      case "7" => "Dormant"
      case _ => throw new IllegalArgumentException(s"Invalid ITSA status code: $code")
    }
  }

  private def previousYearCrystallisationStatusFromCode(code: String): String = {
    code match {
      case "PYF1" => "Crystallised"
      case "PYF2" => "NonCrystallised"
      case _ => throw new IllegalArgumentException(s"Invalid crystallisation status code: $code")
    }
  }

}
