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

package uk.gov.hmrc.incometaxvcfsandstub.models.customUser

enum UserType {
  case Individual
  case PrimaryAgent
  case SupportingAgent
}

object UserType {
  def userTypeFromCode(code: String): UserType = {
    code match {
      case "U1" => UserType.Individual
      case "U2" => UserType.PrimaryAgent
      case "U3" => UserType.SupportingAgent
      case _ => throw new IllegalArgumentException(s"Invalid user type code: $code")
    }
  }
}


