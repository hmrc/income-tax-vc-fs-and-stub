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

import org.mongodb.scala.bson.BsonString
import uk.gov.hmrc.incometaxvcfsandstub.testUtils.TestSupport

import java.time.LocalDate

class ObligationsDataUtilsSpec extends TestSupport {

  "createObligationsData" should {
    "generate obligations data with correct structure and dates for current tax year" in {
      val obligationsData = ObligationsDataUtils.createObligationsData()
      val today = LocalDate.now()
      val currentTaxYearStart = if (today.isBefore(LocalDate.of(today.getYear, 4, 6))) LocalDate.of(today.minusYears(1).getYear, 4, 6) else LocalDate.of(today.getYear, 4, 6)
      val taxYearTaxReturnDueDate = LocalDate.of(currentTaxYearStart.getYear + 1, 1, 31)

      obligationsData.size shouldBe 2

      val itbsObligation = obligationsData.head
      itbsObligation("identification").asDocument().getString("incomeSourceType") shouldBe BsonString("ITSB")
      itbsObligation("obligationDetails").asArray().size() shouldBe 1
      val itbsDetails = itbsObligation("obligationDetails").asArray().get(0).asDocument()
      itbsDetails.getString("inboundCorrespondenceFromDate") shouldBe BsonString(currentTaxYearStart.minusYears(1).toString)
      itbsDetails.getString("inboundCorrespondenceToDate") shouldBe BsonString(currentTaxYearStart.minusDays(1).toString)
      itbsDetails.getString("inboundCorrespondenceDueDate") shouldBe BsonString(taxYearTaxReturnDueDate.toString)

      val itsaObligation = obligationsData(1)
      itsaObligation("identification").asDocument().getString("incomeSourceType") shouldBe BsonString("ITSA")
      itsaObligation("obligationDetails").asArray().size() shouldBe 1
      val itsaDetails = itsaObligation("obligationDetails").asArray().get(0).asDocument()
      itsaDetails.getString("inboundCorrespondenceFromDate") shouldBe BsonString(currentTaxYearStart.minusYears(1).toString)
      itsaDetails.getString("inboundCorrespondenceToDate") shouldBe BsonString(currentTaxYearStart.minusDays(1).toString)
      itsaDetails.getString("inboundCorrespondenceDueDate") shouldBe BsonString(taxYearTaxReturnDueDate.toString)
    }

    "handle edge case where today is exactly April 6" in {
      val fixedToday = LocalDate.of(2026, 4, 6)
      val obligationsData = ObligationsDataUtils.createObligationsData()
      val currentTaxYearStart = LocalDate.of(fixedToday.getYear, 4, 6)
      val taxYearTaxReturnDueDate = LocalDate.of(currentTaxYearStart.getYear + 1, 1, 31)

      obligationsData.size shouldBe 2

      val itbsObligation = obligationsData.head
      val itbsDetails = itbsObligation("obligationDetails").asArray().get(0).asDocument()
      itbsDetails.getString("inboundCorrespondenceFromDate") shouldBe BsonString(currentTaxYearStart.minusYears(1).toString)
      itbsDetails.getString("inboundCorrespondenceToDate") shouldBe BsonString(currentTaxYearStart.minusDays(1).toString)
      itbsDetails.getString("inboundCorrespondenceDueDate") shouldBe BsonString(taxYearTaxReturnDueDate.toString)
    }

    "handle edge case where today is before April 6" in {
      val fixedToday = LocalDate.of(2026, 4, 5)
      val obligationsData = ObligationsDataUtils.createObligationsData()
      val currentTaxYearStart = LocalDate.of(fixedToday.minusYears(1).getYear, 4, 6)
      val taxYearTaxReturnDueDate = LocalDate.of(currentTaxYearStart.plusYears(1).getYear + 1, 1, 31)

      obligationsData.size shouldBe 2

      val itsaObligation = obligationsData(1)
      val itsaDetails = itsaObligation("obligationDetails").asArray().get(0).asDocument()
      itsaDetails.getString("inboundCorrespondenceFromDate") shouldBe BsonString(currentTaxYearStart.toString)
      itsaDetails.getString("inboundCorrespondenceToDate") shouldBe BsonString(currentTaxYearStart.plusYears(1).minusDays(1).toString)
      itsaDetails.getString("inboundCorrespondenceDueDate") shouldBe BsonString(taxYearTaxReturnDueDate.toString)
    }
  }
}
