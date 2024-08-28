package com.jdbifun.dao.impl

import com.jdbifun.dao.PolicyDao
import com.jdbifun.dao.PolicyDaoUtils.setPolicy
import com.jdbifun.dao.PolicyDaoUtils.toPolicy
import com.jdbifun.model.Policy
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi

class PolicyJdbiFluentApiDao(private val jdbi: Jdbi) : PolicyDao {

  override fun name(): String = PolicyJdbiFluentApiDao::class.java.simpleName

  override fun countPolicies(): Long =
      jdbi.withHandle<Long, Exception> { handle ->
        handle.createQuery("SELECT COUNT(*) FROM policies")
            .mapTo(Long::class.javaObjectType)
            .one()
      }

  override fun getPolicies(): List<Policy> {
    return jdbi.withHandle<List<Policy>, Exception> { handle: Handle ->
      handle.createQuery(
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
          .map { rs, _ -> rs.toPolicy() }
          .list()
    }
  }

  override fun addPolicies(policies: List<Policy>): Int {
    return jdbi.withHandle<Int, Exception> { handle: Handle ->
      val batch = handle.prepareBatch(
          """
                INSERT INTO policies (
                    policy_id,
                    prev_policy_id,
                    customer_name,
                    lob,
                    coverage_start_date,
                    coverage_end_date,
                    cancellation_date_time
                ) VALUES (?,?,?,?,?,?,?)
                """
      )

      policies.forEach { policy ->
        batch.setPolicy(policy)
      }

      val result = batch.execute()
      result.sum()
    }
  }
}
