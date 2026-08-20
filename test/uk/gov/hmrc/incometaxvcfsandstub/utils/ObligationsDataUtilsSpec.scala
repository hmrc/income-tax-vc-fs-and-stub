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

import uk.gov.hmrc.incometaxvcfsandstub.testUtils.TestSupport

import java.time.LocalDate

class ObligationsDataUtilsSpec extends TestSupport {

  "createObligationsData" should {
    "return two obligation entries" in {
      val result = ObligationsDataUtils.createFulfilledObligationsData()
      (result \ "obligations").as[Seq[play.api.libs.json.JsValue]].size shouldBe 2
    }

    "return ITSB obligation as the first entry with correct identification" in {
      val result = ObligationsDataUtils.createFulfilledObligationsData()
      val firstObligation = (result \ "obligations")(0)
      (firstObligation \ "identification" \ "incomeSourceType").as[String] shouldBe "ITSB"
      (firstObligation \ "identification" \ "referenceNumber").as[String] shouldBe "XAIS00000000001"
      (firstObligation \ "identification" \ "referenceType").as[String] shouldBe "MTDBIS"
    }

    "return ITSA obligation as the second entry with correct identification" in {
      val result = ObligationsDataUtils.createFulfilledObligationsData()
      val secondObligation = (result \ "obligations")(1)
      (secondObligation \ "identification" \ "incomeSourceType").as[String] shouldBe "ITSA"
      (secondObligation \ "identification" \ "referenceNumber").as[String] shouldBe "XAIT00000000002"
      (secondObligation \ "identification" \ "referenceType").as[String] shouldBe "MTDBIS"
    }

    "return obligations with status Fulfilled" in {
      val result = ObligationsDataUtils.createFulfilledObligationsData()
      val obligations = (result \ "obligations").as[Seq[play.api.libs.json.JsValue]]
      obligations.foreach { obligation =>
        val details = (obligation \ "obligationDetails")(0)
        (details \ "status").as[String] shouldBe "F"
      }
    }

    "return obligation dates relative to the current tax year" in {
      val result = ObligationsDataUtils.createFulfilledObligationsData()
      val today = LocalDate.now()
      val currentTaxYearStart =
        if (today.isBefore(LocalDate.of(today.getYear, 4, 6)))
          LocalDate.of(today.minusYears(1).getYear, 4, 6)
        else
          LocalDate.of(today.getYear, 4, 6)
      val expectedFromDate = currentTaxYearStart.minusYears(1).toString
      val expectedToDate = currentTaxYearStart.minusDays(1).toString
      val expectedDueDate = LocalDate.of(currentTaxYearStart.getYear + 1, 1, 31).toString

      val obligations = (result \ "obligations").as[Seq[play.api.libs.json.JsValue]]
      obligations.foreach { obligation =>
        val details = (obligation \ "obligationDetails")(0)
        (details \ "inboundCorrespondenceFromDate").as[String] shouldBe expectedFromDate
        (details \ "inboundCorrespondenceToDate").as[String] shouldBe expectedToDate
        (details \ "inboundCorrespondenceDueDate").as[String] shouldBe expectedDueDate
      }
    }

    "return ITSB obligation with period key #001" in {
      val result = ObligationsDataUtils.createFulfilledObligationsData()
      val details = (result \ "obligations")(0) \ "obligationDetails"
      ((details)(0) \ "periodKey").as[String] shouldBe "#001"
    }

    "return ITSA obligation with period key C" in {
      val result = ObligationsDataUtils.createFulfilledObligationsData()
      val details = (result \ "obligations")(1) \ "obligationDetails"
      (details(0) \ "periodKey").as[String] shouldBe "C"
    }

    "return ITSB obligation with date received one month ago" in {
      val result = ObligationsDataUtils.createFulfilledObligationsData()
      val details = (result \ "obligations")(0) \ "obligationDetails"
      val expectedDateReceived = LocalDate.now().minusMonths(1).toString
      (details(0) \ "inboundCorrespondenceDateReceived").as[String] shouldBe expectedDateReceived
    }

    "return ITSA obligation with date received as today" in {
      val result = ObligationsDataUtils.createFulfilledObligationsData()
      val details = (result \ "obligations")(1) \ "obligationDetails"
      val expectedDateReceived = LocalDate.now().toString
      (details(0) \ "inboundCorrespondenceDateReceived").as[String] shouldBe expectedDateReceived
    }

    "return tax return due date on January 31 of the year following the current tax year start" in {
      val result = ObligationsDataUtils.createFulfilledObligationsData()
      val today = LocalDate.now()
      val currentTaxYearStart =
        if (today.isBefore(LocalDate.of(today.getYear, 4, 6)))
          LocalDate.of(today.minusYears(1).getYear, 4, 6)
        else
          LocalDate.of(today.getYear, 4, 6)
      val expectedDueDate = LocalDate.of(currentTaxYearStart.getYear + 1, 1, 31).toString

      val obligations = (result \ "obligations").as[Seq[play.api.libs.json.JsValue]]
      obligations.foreach { obligation =>
        val details = (obligation \ "obligationDetails")(0)
        (details \ "inboundCorrespondenceDueDate").as[String] shouldBe expectedDueDate
      }
    }
  }
}
