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

import org.mongodb.scala.Document

import java.time.LocalDate

object BusinessDataUtils {

  final val businessDataKey = "response.success.taxPayerDisplayResponse.businessData"
  final val propertyDataKey = "response.success.taxPayerDisplayResponse.propertyData"
  final val channelKey = "response.success.taxPayerDisplayResponse.channel"
  private val currentYear: Int = LocalDate.now().getYear
  private val currentTaxYear = if(LocalDate.now().isBefore(LocalDate.of(currentYear, 4, 6))) currentYear - 1 else currentYear

  def createBusinessData(activeSoleTrader: Boolean, ceasedSoleTrader: Boolean, latentSoleTrader: Boolean = false): Seq[Document] = {

    def businessDocument(isCeased: Boolean, index: Int, isLatentBusiness: Boolean): Document = {
      val cessation: Option[(String, String)] = if (isCeased) Some("cessationDate" -> s"${currentYear - 1}-06-30") else None

      val businessInLatency = if(isLatentBusiness) {
        Document(
          "latencyDetails" -> Document(
            "latencyEndDate" -> s"${currentTaxYear + 1}-12-31",
            "taxYear1" -> currentTaxYear.toString,
            "latencyIndicator1" -> "A",
            "taxYear2" -> (currentTaxYear + 1).toString,
            "latencyIndicator2" -> "A"
          )
        )
      } else {
        Document.empty
      }

      val base = Document(
        "incomeSourceId" -> s"XAIT2000000000$index",
        "accPeriodSDate" -> s"$currentYear-04-01",
        "accPeriodEDate" -> s"${currentYear + 1}-03-31",
        "tradingName" -> s"Business $index",
        "incomeSource" -> s"Manufacturing $index",
        "businessAddressDetails" -> Document(
          "addressLine1" -> s"$index Street Street",
          "addressLine2" -> "Cityburg",
          "addressLine3" -> "Countryshire",
          "addressLine4" -> "Townville",
          "postalCode" -> s"AA$index AAA",
          "countryCode" -> "GB"
        ),
        "tradingSDate" -> s"${currentYear - 3}-01-01",
        "seasonalFlag" -> false,
        "paperLessFlag" -> true,
        "firstAccountingPeriodStartDate" -> s"${currentYear - 3}-04-01",
        "firstAccountingPeriodEndDate" -> s"${currentYear - 2}-03-31"
      )

      (cessation, businessInLatency.isEmpty) match {
        case (Some(cessationData), true) => base ++ Document(cessationData)
        case (None, false) => base ++ businessInLatency
        case (_, _) => base
      }
    }

    (activeSoleTrader, ceasedSoleTrader, latentSoleTrader) match {
      case (false, false, false) => Seq.empty
      case (true, false, false)  => Seq(businessDocument(isCeased = false, index = 1, isLatentBusiness = false))
      case (false, true, false)  => Seq(businessDocument(isCeased = true, index = 2, isLatentBusiness = false))
      case (true, true, false)   => Seq(businessDocument(isCeased = false, index = 1, isLatentBusiness = false), businessDocument(isCeased = true, index = 2, isLatentBusiness = false))
      case (false, false, true)  => Seq(businessDocument(isCeased = false, index = 3, isLatentBusiness = true))
      case (true, false, true)   => Seq(businessDocument(isCeased = false, index = 1, isLatentBusiness = false), businessDocument(isCeased = false, index = 3, isLatentBusiness = true))
      case (false, true, true)   => Seq(businessDocument(isCeased = false, index = 2, isLatentBusiness = false), businessDocument(isCeased = false, index = 3, isLatentBusiness = true))
      case (true, true, true)    => Seq(businessDocument(isCeased = false, index = 1, isLatentBusiness = false), businessDocument(isCeased = true, index = 2, isLatentBusiness = false), businessDocument(isCeased = false, index = 3, isLatentBusiness = true))
    }
  }

  def createPropertyData(ukProperty: Boolean, foreignProperty: Boolean): Seq[Document] = {

    def propertyDocument(incomeSourceType: String, incomeSourceId: String): Document =
      Document(
        "incomeSourceId"                 -> incomeSourceId,
        "accPeriodSDate"                 -> s"$currentYear-04-06",
        "accPeriodEDate"                 -> s"${currentYear + 1}-04-05",
        "numPropRentedUK"                -> "4",
        "numPropRentedEEA"               -> "0",
        "numPropRentedNONEEA"            -> "0",
        "numPropRented"                  -> "4",
        "paperLessFlag"                  -> true,
        "firstAccountingPeriodStartDate" -> "2017-04-06",
        "firstAccountingPeriodEndDate"   -> "2018-04-05",
        "incomeSourceType"               -> incomeSourceType,
        "tradingSDate"                   -> "2015-05-01"
      )

    Seq(
      Option.when(ukProperty)(propertyDocument("02", "XAIS00000000011")),
      Option.when(foreignProperty)(propertyDocument("03", "XAIS00000000012"))
    ).flatten
  }
}