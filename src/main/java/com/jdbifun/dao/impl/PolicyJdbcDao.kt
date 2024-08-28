package com.jdbifun.dao.impl

import com.jdbifun.dao.PolicyDao
import com.jdbifun.dao.PolicyDaoUtils.setPolicy
import com.jdbifun.dao.PolicyDaoUtils.toPolicy
import com.jdbifun.model.Policy
import java.sql.Connection

class PolicyJdbcDao(private val connectionFactory: () -> Connection) : PolicyDao {

  override fun name(): String = PolicyJdbcDao::class.java.simpleName

  override fun countPolicies(): Long =
      connectionFactory().use { connection ->
        connection.prepareStatement("SELECT COUNT(*) FROM policies").use { statement ->
          statement.executeQuery().use { rs ->
            if (rs.next()) rs.getLong(1) else 0L
          }
        }
      }

  override fun getPolicies(): List<Policy> {
    val policies = mutableListOf<Policy>()
    val sql = """
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

    connectionFactory().use { connection ->
      connection.prepareStatement(sql).use { statement ->
        val resultSet = statement.executeQuery()
        while (resultSet.next()) {
          policies.add(resultSet.toPolicy())
        }
      }
    }
    return policies
  }

  override fun addPolicies(policies: List<Policy>): Int {
    val sql = """
            INSERT INTO policies (
                prev_policy_id,
                customer_name,
                lob,
                coverage_start_date,
                coverage_end_date,
                cancellation_date_time
            ) VALUES (?, ?, ?, ?, ?, ?)
        """

    var totalInserted = 0

    connectionFactory().use { connection ->
      connection.prepareStatement(sql).use { statement ->
        policies.forEach { policy ->
          statement.setPolicy(policy)
          statement.addBatch()
        }
        val result = statement.executeBatch()
        totalInserted = result.sum()
      }
    }

    return totalInserted
  }
}
