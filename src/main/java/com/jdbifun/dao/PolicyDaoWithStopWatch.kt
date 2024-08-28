package com.jdbifun.dao

import com.jdbifun.model.Policy

interface PolicyDao {
  fun name(): String
  fun countPolicies(): Long
  fun getPolicies(): List<Policy>
  fun addPolicies(policies: List<Policy>): Int
}
