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

enum UserChannel(val apiValue: String) {
  case CustomerLed extends UserChannel("1")
  case HmrcUnconfirmed extends UserChannel("2")
  case HmrcConfirmed extends UserChannel("3")

  def getApiValue: String = apiValue
}

object UserChannel {
  def getApiValueForUserChannel(userChannel: UserChannel): String = {
    userChannel match {
      case CustomerLed     => CustomerLed.getApiValue
      case HmrcUnconfirmed => HmrcUnconfirmed.getApiValue
      case HmrcConfirmed   => HmrcConfirmed.getApiValue
    }
  }

  def fromString(value: String): UserChannel = {
    value match {
      case "customer-led"         => CustomerLed
      case "hmrc-led-unconfirmed" => HmrcUnconfirmed
      case "hmrc-led-confirmed"   => HmrcConfirmed
      case _                      => throw new IllegalArgumentException(s"Invalid user channel: $value")
    }
  }
}


