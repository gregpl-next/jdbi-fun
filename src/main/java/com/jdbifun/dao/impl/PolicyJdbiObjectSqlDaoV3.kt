package com.jdbifun.dao.impl

import com.jdbifun.dao.PolicyDao
import com.jdbifun.dao.PolicyDaoUtils.toPolicy
import com.jdbifun.model.Policy
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import org.jdbi.v3.sqlobject.config.RegisterRowMapper
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import java.sql.ResultSet
import java.time.LocalDate
import java.time.LocalDateTime

@RegisterRowMapper(PolicyJdbiObjectSqlDaoV3.PolicyRowMapper::class)
interface PolicyJdbiObjectSqlDaoV3 : PolicyDao {

  override fun name(): String = "${PolicyJdbiObjectSqlDaoV3::class.java.simpleName} (RowMapper + spread)"

  @SqlQuery("SELECT COUNT(*) FROM policies")
  override fun countPolicies(): Long

  @SqlQuery(
      """
        SELECT
            policy_id,
            prev_policy_id,
            customer_name,
            lob,
            coverage_start_date,
            coverage_end_date,
            cancellation_date_time
        FROM policies
        """
  )
  override fun getPolicies(): List<Policy>

  @SqlUpdate(
      """
        INSERT INTO policies (
            prev_policy_id,
            customer_name,
            lob,
            coverage_start_date,
            coverage_end_date,
            cancellation_date_time
        ) VALUES (
            :prevPolicyId,
            :customerName,
            :lob,
            :coverageStartDate,
            :coverageEndDate,
            :cancellationDateTime
        )
        """
  )
  fun internal_addPolicy(
      prevPolicyId: Int?,
      customerName: String,
      lob: String,
      coverageStartDate: LocalDate,
      coverageEndDate: LocalDate,
      cancellationDateTime: LocalDateTime?
  ): Boolean

  override fun addPolicies(@BindBean policies: List<Policy>): Int =
      policies.map {
        internal_addPolicy(
            prevPolicyId = it.prevPolicyId,
            customerName = it.customerName,
            lob = it.lob.stableId,
            coverageStartDate = it.coverageStartDate,
            coverageEndDate = it.coverageEndDate,
            cancellationDateTime = it.cancellationDateTime,
        )
      }.count { it }

  class PolicyRowMapper : RowMapper<Policy> {
    override fun map(rs: ResultSet, ctx: StatementContext): Policy =
        rs.toPolicy()
  }
}
