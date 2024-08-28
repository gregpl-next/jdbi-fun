package com.jdbifun.dao

import com.jdbifun.model.LOB
import com.jdbifun.model.Policy
import org.jdbi.v3.core.statement.PreparedBatch
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp

object PolicyDaoUtils {

  fun ResultSet.toPolicy(): Policy {
    return Policy(
        policyId = getInt("policy_id"),
        prevPolicyId = getInt("prev_policy_id"),
        customerName = getString("customer_name"),
        lob = LOB.fromStableId(getString("lob")),
        coverageStartDate = getDate("coverage_start_date").toLocalDate(),
        coverageEndDate = getDate("coverage_end_date").toLocalDate(),
        cancellationDateTime = getTimestamp("cancellation_date_time")?.toLocalDateTime()
    )
  }

  fun PreparedStatement.setPolicy(policy: Policy) {
    setObject(1, policy.prevPolicyId)
    setString(2, policy.customerName)
    setString(3, policy.lob.stableId)
    setDate(4, java.sql.Date.valueOf(policy.coverageStartDate))
    setDate(5, java.sql.Date.valueOf(policy.coverageEndDate))
    setTimestamp(6, policy.cancellationDateTime?.let { Timestamp.valueOf(it) })
  }

  fun PreparedBatch.setPolicy(policy: Policy) {
    bind(0, policy.policyId)
    bind(1, policy.prevPolicyId)
    bind(2, policy.customerName)
    bind(3, policy.lob.stableId)
    bind(4, java.sql.Date.valueOf(policy.coverageStartDate))
    bind(5, java.sql.Date.valueOf(policy.coverageEndDate))
    bind(6, policy.cancellationDateTime?.let { Timestamp.valueOf(it) })
    add()
  }
}
