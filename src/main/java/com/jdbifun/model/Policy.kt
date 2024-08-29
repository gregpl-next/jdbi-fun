package com.jdbifun.model

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Converter
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "policies")
data class Policy(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_id")
    val policyId: Int? = null,

    @Column(name = "prev_policy_id")
    val prevPolicyId: Int? = null,

    @Column(name = "customer_name", nullable = false)
    val customerName: String,

    @Convert(converter = LobConverter::class)
    @Column(name = "lob", nullable = false)
    val lob: LOB,

    @Column(name = "coverage_start_date", nullable = false)
    val coverageStartDate: LocalDate = LocalDate.now(),

    @Column(name = "coverage_end_date", nullable = false)
    val coverageEndDate: LocalDate = coverageStartDate.plusYears(1),

    @Column(name = "cancellation_date_time")
    val cancellationDateTime: LocalDateTime? = null
)

enum class LOB(val stableId: String) {
  GL("GL"),
  PL("PL"),
  WCv1("WC"),
  ;

  companion object {
    private val lobToStableId = entries.associateBy { it.stableId }

    fun fromStableId(stableId: String): LOB = lobToStableId[stableId] ?: throw Exception("unknown LOB: $stableId")
  }
}

@Converter(autoApply = true)
private class LobConverter : AttributeConverter<LOB, String> {

  override fun convertToDatabaseColumn(attribute: LOB?): String {
    return attribute?.stableId!!
  }

  override fun convertToEntityAttribute(dbData: String?): LOB {
    return dbData!!.let { LOB.fromStableId(it) }
  }
}
