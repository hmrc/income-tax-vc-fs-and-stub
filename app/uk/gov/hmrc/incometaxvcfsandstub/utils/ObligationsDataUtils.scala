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
import uk.gov.hmrc.incometaxvcfsandstub.models.customUser.{DecoupledObligations, ObligationStatus, ObligationsDataModel}

import java.time.LocalDate

object ObligationsDataUtils {
  final val obligationsDataKey = "response.obligations"
  private val today = LocalDate.now()

  def createObligationsData(): Seq[Document] = {
    val currentTaxYearStart = if(today.isBefore(LocalDate.of(today.getYear, 4, 6))) LocalDate.of(today.minusYears(1).getYear, 4, 6) else LocalDate.of(today.getYear, 4, 6)
    val taxYearTaxReturnDueDate = LocalDate.of(currentTaxYearStart.getYear + 1, 1, 31)

    Seq(Document(
      "identification" -> Document(
        "incomeSourceType" -> "ITSB",
        "referenceNumber" -> s"XAIS00000000001",
        "referenceType" -> "MTDBIS"
      ),
      "obligationDetails" -> Seq(
        Document(
          "status" -> "F",
          s"inboundCorrespondenceFromDate" -> currentTaxYearStart.minusYears(1).toString,
          s"inboundCorrespondenceToDate" -> currentTaxYearStart.minusDays(1).toString,
          "inboundCorrespondenceDueDate" -> taxYearTaxReturnDueDate.toString,
          "inboundCorrespondenceDateReceived" -> today.minusMonths(1).toString,
          "periodKey" -> "#001"
        ),
    )
    ),
      Document(
        "identification" -> Document(
          "incomeSourceType" -> "ITSA",
          "referenceNumber" -> s"XAIT00000000002",
          "referenceType" -> "MTDBIS"
        ),
        "obligationDetails" -> Seq(
          Document(
            "status" -> "F",
            s"inboundCorrespondenceFromDate" -> currentTaxYearStart.minusYears(1).toString,
            s"inboundCorrespondenceToDate" -> currentTaxYearStart.minusDays(1).toString,
            "inboundCorrespondenceDueDate" -> taxYearTaxReturnDueDate.toString,
            "inboundCorrespondenceDateReceived" -> today.toString,
            "periodKey" -> "C"
          )
        )
      )
    )
  }

  def createCustomUserObligationsData(obligations: DecoupledObligations): ObligationsDataModel = {
    val currentTaxYearStart = if (today.isBefore(LocalDate.of(today.getYear, 4, 6))) LocalDate.of(today.minusYears(1).getYear, 4, 6) else LocalDate.of(today.getYear, 4, 6)
    val taxYearTaxReturnDueDate = LocalDate.of(currentTaxYearStart.getYear + 1, 1, 31)

    val q1DueDate = LocalDate.of(currentTaxYearStart.getYear, 8, 7)
    val q2DueDate = LocalDate.of(currentTaxYearStart.getYear, 11, 7)
    val q3DueDate = LocalDate.of(currentTaxYearStart.getYear + 1, 2, 7)
    val q4DueDate = LocalDate.of(currentTaxYearStart.getYear + 1, 5, 7)
    val taxReturnDueDate = LocalDate.of(currentTaxYearStart.getYear + 1, 1, 31)

    val q1ToDate = LocalDate.of(currentTaxYearStart.getYear, 7, 5)
    val q2ToDate = LocalDate.of(currentTaxYearStart.getYear, 10, 5)
    val q3ToDate = LocalDate.of(currentTaxYearStart.getYear + 1, 1, 5)
    val q4ToDate = LocalDate.of(currentTaxYearStart.getYear + 1, 4, 5)
    val taxReturnToDate = LocalDate.of(currentTaxYearStart.getYear + 1, 4, 5)

    val quarterlyStatuses: Seq[(String, LocalDate, LocalDate, ObligationStatus)] =
      Seq(
        obligations.quarterlyUpdate1.map(status => ("#001", q1ToDate, q1DueDate, status)),
        obligations.quarterlyUpdate2.map(status => ("#002", q2ToDate, q2DueDate, status)),
        obligations.quarterlyUpdate3.map(status => ("#003", q3ToDate, q3DueDate, status)),
        obligations.quarterlyUpdate4.map(status => ("#004", q4ToDate, q4DueDate, status))
      ).flatten

    val (openObligations, fulfilledObligations) = quarterlyStatuses.partition(_._4 == ObligationStatus.Open)

    val annualObligation = obligations.annualObligation.map { status =>
      Seq(
          Document(
          "identification" -> Document(
            "incomeSourceType" -> "ITSA",
            "referenceNumber" -> s"XAIT10000000001",
            "referenceType" -> "MTDBIS"
          ),
          "obligationDetails" -> Seq(
            Document(
              Map(
                "status" -> ObligationStatus.getApiValue(status),
                s"inboundCorrespondenceFromDate" -> currentTaxYearStart.minusYears(1).toString,
                s"inboundCorrespondenceToDate" -> currentTaxYearStart.minusDays(1).toString,
                "inboundCorrespondenceDueDate" -> taxYearTaxReturnDueDate.toString,
                "periodKey" -> "C"
              ) ++ (if (status.isFulfilled) Map("inboundCorrespondenceDateReceived" -> today.toString) else Map.empty)
            )
          )
        )
      )
    }.getOrElse(Seq.empty)

    val openQuarterlyObligations = openObligations.map { case (periodKey, toDate, dueDate, status) =>
      Document(
        "identification" -> Document(
          "incomeSourceType" -> "ITSB",
          "referenceNumber" -> "XAIT20000000001",
          "referenceType" -> "MTDBIS"
        ),
        "obligationDetails" -> Seq(
          Document(
            Map(
              "status" -> ObligationStatus.getApiValue(status),
              "inboundCorrespondenceFromDate" -> currentTaxYearStart.minusYears(1).toString,
              "inboundCorrespondenceToDate" -> toDate.toString,
              "inboundCorrespondenceDueDate" -> dueDate.toString,
              "periodKey" -> periodKey
            )
          )
        )
      )
    }

    val fulfilledQuarterlyObligations = fulfilledObligations.map { case (periodKey, toDate, dueDate, status) =>
      Document(
        "identification" -> Document(
          "incomeSourceType" -> "ITSB",
          "referenceNumber" -> "XAIT20000000001",
          "referenceType" -> "MTDBIS"
        ),
        "obligationDetails" -> Seq(
          Document(
            Map(
              "status" -> ObligationStatus.getApiValue(status),
              "inboundCorrespondenceFromDate" -> currentTaxYearStart.minusYears(1).toString,
              "inboundCorrespondenceToDate" -> toDate.toString,
              "inboundCorrespondenceDueDate" -> dueDate.toString,
              "periodKey" -> periodKey,
              "inboundCorrespondenceDateReceived" -> today.minusMonths(1).toString
            )
          )
        )
      )
    }

    val quarterlyObligations: Seq[Document] = {
      quarterlyStatuses match {
        case Nil => Seq.empty
        case statuses =>
          Seq(
            Document(
              "identification" -> Document(
                "incomeSourceType" -> "ITSB",
                "referenceNumber" -> "XAIT20000000001",
                "referenceType" -> "MTDBIS"
              ),
              "obligationDetails" -> statuses.map { case (periodKey, toDate, dueDate, status) =>
                Document(
                  Map(
                    "status" -> ObligationStatus.getApiValue(status),
                    "inboundCorrespondenceFromDate" -> currentTaxYearStart.minusYears(1).toString,
                    "inboundCorrespondenceToDate" -> toDate.toString,
                    "inboundCorrespondenceDueDate" -> dueDate.toString,
                    "periodKey" -> periodKey
                  ) ++ (if (status.isFulfilled) Map("inboundCorrespondenceDateReceived" -> today.minusMonths(1).toString)
                    else Map.empty)
                )
              }
            )
          )
      }
    }
    
    val openAnnual = if (obligations.annualObligation.contains(ObligationStatus.Open)) annualObligation else Seq.empty
    val fulfilledAnnual = if (obligations.annualObligation.contains(ObligationStatus.Fulfilled)) annualObligation else Seq.empty

    ObligationsDataModel(
      datedData = annualObligation ++ quarterlyObligations,
      openObligationsData = openAnnual ++ openQuarterlyObligations,
      fulfilledObligationsData = fulfilledAnnual ++ fulfilledQuarterlyObligations
    )
  }
}
