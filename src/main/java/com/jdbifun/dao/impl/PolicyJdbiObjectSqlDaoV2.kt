package com.jdbifun.dao.impl

import com.jdbifun.dao.PolicyDao
import com.jdbifun.model.LOB
import com.jdbifun.model.Policy
import org.jdbi.v3.core.argument.AbstractArgumentFactory
import org.jdbi.v3.core.argument.Argument
import org.jdbi.v3.core.config.ConfigRegistry
import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import org.jdbi.v3.sqlobject.config.RegisterArgumentFactory
import org.jdbi.v3.sqlobject.config.RegisterColumnMapper
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.SqlBatch
import org.jdbi.v3.sqlobject.statement.SqlQuery
import java.sql.ResultSet
import java.sql.Types

@RegisterColumnMapper(PolicyJdbiObjectSqlDaoV2.LobColumnMapper::class)
@RegisterArgumentFactory(PolicyJdbiObjectSqlDaoV2.LobArgumentFactory::class)
interface PolicyJdbiObjectSqlDaoV2 : PolicyDao {

  override fun name(): String = "${PolicyJdbiObjectSqlDaoV2::class.java.simpleName} (ColumnMapper + ArgumentFactory)"

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

  @SqlBatch(
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
  fun internal_addPolicies(@BindBean policies: List<Policy>): BooleanArray

  override fun addPolicies(policies: List<Policy>): Int =
      internal_addPolicies(policies).count { it }

  class LobColumnMapper : ColumnMapper<LOB> {
    override fun map(r: ResultSet, columnNumber: Int, ctx: StatementContext): LOB =
        LOB.fromStableId(r.getString(columnNumber))
  }

  class LobArgumentFactory : AbstractArgumentFactory<LOB>(Types.VARCHAR) {
    override fun build(value: LOB, config: ConfigRegistry) = Argument { position, statement, _ ->
      statement.setString(position, value.stableId)
    }
  }
}
