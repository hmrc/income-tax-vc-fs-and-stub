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

import uk.gov.hmrc.incometaxvcfsandstub.models.customUser._
import uk.gov.hmrc.incometaxvcfsandstub.models.customUser.UserType._

object CustomUserUtils {

  val userNino = "CR000000A"

  def translateCode(userCode: String): DecoupledCustomUserModel = {

    val parts = userCode.split('|').toList

    val userType      = decodeUserType(parts.head)
    val userChannel   = decodeUserChannel(parts(1))
    val soleTrader    = decodeSoleTrader(parts(2))
    val ukProperty    = decodeUkProperty(parts(3))
    val foreignProp   = decodeForeignProperty(parts(4))
    val itsaStatus    = decodeItsaStatus(parts(5))
    val obligations   = decodeObligations(parts(6))

    DecoupledCustomUserModel(
      agentType = userType,
      incomeSources = DecoupledIncomeSources(
        userChannel           = UserChannel.fromString(userChannel),
        activeSoleTrader      = soleTrader.contains('A'),
        latentSoleTrader      = soleTrader.contains('L'),
        ceasedSoleTrader      = soleTrader.contains('C'),
        activeUkProperty      = ukProperty.contains('A'),
        ceasedUkProperty      = ukProperty.contains('C'),
        activeForeignProperty = foreignProp.contains('A'),
        ceasedForeignProperty = foreignProp.contains('C')
      ),
      itsaStatus = itsaStatus,
      obligations = obligations
    )
  }

  private def decodeUserType(code: String): UserType =
    code match {
      case "U1" => Individual
      case "U2" => PrimaryAgent
      case "U3" => SupportingAgent
      case _    => invalid("user type", code)
    }

  private def decodeUserChannel(code: String): String =
    code match {
      case "UC1" => "customer-led"
      case "UC2" => "hmrc-led-unconfirmed"
      case "UC3" => "hmrc-led-confirmed"
      case _     => invalid("user channel", code)
    }

  private def decodeSoleTrader(segment: String): Set[Char] = decodeFlags(segment, "ST")
  private def decodeUkProperty(segment: String): Set[Char] = decodeFlags(segment, "P")
  private def decodeForeignProperty(segment: String): Set[Char] = decodeFlags(segment, "F")

  private def decodeFlags(segment: String, prefix: String): Set[Char] = {
    segment match {
      case s if s.startsWith(s"$prefix:") =>
        s.drop(prefix.length + 1) match {
          case "-" => Set.empty
          case v   => v.toSet
        }
      case _ => invalid(s"$prefix flags", segment)
    }
  }

  private def decodeItsaStatus(segment: String): DecoupledItsaStatus = {
    val parts = segment.stripPrefix("ITSA:").split("-")

    DecoupledItsaStatus(
      cyMinusOneCrystallisationStatus = parts(0) match {
        case "CR" => "Crystallised"
        case "NC" => "NonCrystallised"
        case _    => invalid("crystallisation status", parts(0))
      },
      cyMinusOneItsaStatus = itsaFromCode(parts(1)),
      cyItsaStatus         = itsaFromCode(parts(2)),
      cyPlusOneItsaStatus  = itsaFromCode(parts(3))
    )
  }

  private def itsaFromCode(code: String): String =
    code match {
      case "0"  => "No Status"
      case "1"  => "MTD Mandated"
      case "2"  => "MTD Voluntary"
      case "3"  => "Annual"
      case "4"  => "Digitally Exempt"
      case "5"  => "Dormant"
      case "99" => "MTD Exempt"
      case _    => invalid("ITSA status", code)
    }

  private def decodeObligations(segment: String): DecoupledObligations = {
    val codes = segment.stripPrefix("OB:").split("-")

    DecoupledObligations(
      annualObligation  = obligationFromCode(codes(0)),
      quarterlyUpdate1  = obligationFromCode(codes(1)),
      quarterlyUpdate2  = obligationFromCode(codes(2)),
      quarterlyUpdate3  = obligationFromCode(codes(3)),
      quarterlyUpdate4  = obligationFromCode(codes(4))
    )
  }

  private def obligationFromCode(code: String): Option[ObligationStatus] =
    code match {
      case "O" => Some(ObligationStatus.Open)
      case "F" => Some(ObligationStatus.Fulfilled)
      case "N" => None
      case _   => invalid("obligation", code)
    }

  private def invalid(context: String, value: String): Nothing =
    throw new IllegalArgumentException(s"Invalid $context code: $value")
}
