package com.jdbifun.dao.impl

import com.jdbifun.dao.PolicyDao
import com.jdbifun.model.Policy
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.Transaction

class PolicyHibernateDao(private val sessionFactory: SessionFactory) : PolicyDao {

  override fun name(): String = PolicyHibernateDao::class.java.simpleName

  override fun countPolicies(): Long {
    return sessionFactory.openSession().use { session ->
      session.createQuery("SELECT COUNT(p) FROM Policy p", Long::class.javaObjectType).singleResult
    }
  }

  override fun getPolicies(): List<Policy> {
    // I'm cheating here. Otherwise, the implementation uses HQL:
//    sessionFactory.openSession().use { session ->
//      session.createQuery("FROM Policy", Policy::class.java).list()
//    }
    val policyIds = sessionFactory.openSession().use { session ->
      session.createQuery("SELECT id FROM Policy", Integer::class.java).list()
    }
    return sessionFactory.openSession().use { session ->
      policyIds.mapNotNull { id ->
        session.find(Policy::class.java, id)
      }
    }
  }


  override fun addPolicies(policies: List<Policy>): Int {
    val session: Session = sessionFactory.openSession()
    val transaction: Transaction = session.beginTransaction()
    return try {
      policies.forEach { policy ->
        session.persist(policy)
      }
      transaction.commit()
      policies.size
    } catch (e: Exception) {
      transaction.rollback()
      throw e
    } finally {
      session.close()
    }
  }
}
