package com.jdbifun.dao

import com.jdbifun.dao.impl.PolicyHibernateDao
import com.jdbifun.dao.impl.PolicyJdbcDao
import com.jdbifun.dao.impl.PolicyJdbiFluentApiDao
import com.jdbifun.dao.impl.PolicyJdbiObjectSqlDao
import com.jdbifun.model.LOB
import com.jdbifun.model.Policy
import com.nhaarman.expect.expect
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.h2.tools.RunScript
import org.hibernate.cfg.Configuration
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.sqlobject.SqlObjectPlugin
import org.jdbi.v3.sqlobject.kotlin.KotlinSqlObjectPlugin
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.nio.charset.StandardCharsets
import java.sql.Connection
import java.sql.DriverManager
import kotlin.random.Random

internal class PolicyDaoTest {

  companion object {
    private const val NUM_WARM_UP_ITERATIONS = 50 // I guess maybe there's a cache warming up? connection optimizations?
    private const val NUM_POLICIES_TO_ADD = 50

    private val logger: Logger = LogManager.getLogger(PolicyDaoTest::class.java)

    @JvmStatic
    fun getPoliciesTestDataList(): List<Array<Any>> {
      return generateDaos().map { arrayOf(it.name(), it) }
    }

    @JvmStatic
    fun addPoliciesTestDataList(): List<Array<Any>> {
      return generateDaos().map { arrayOf(it.name(), it, generateRandomPolicies(1)) }
    }

    private fun generateRandomPolicies(count: Int = NUM_POLICIES_TO_ADD): List<Policy> =
        List(count) {
          Policy(
              lob = LOB.entries[Random.nextInt(LOB.entries.size)],
              customerName = (1..20).map { ('A'..'Z').random() }.joinToString("")
          )
        }

    private fun generateDaos(): List<PolicyDao> {
      // Hibernate
      val sessionFactory = Configuration().configure().buildSessionFactory()

      // JDBI
      val jdbi = Jdbi.create(generateDbConnection())
      jdbi.installPlugin(SqlObjectPlugin())
          .installPlugin(KotlinPlugin())
          .installPlugin(KotlinSqlObjectPlugin())

      val daos = listOf(
          PolicyJdbcDao{ generateDbConnection() },
          PolicyJdbiFluentApiDao(jdbi),
          jdbi.onDemand(PolicyJdbiObjectSqlDao::class.java),
          PolicyHibernateDao(sessionFactory),
      )
      return daos
    }

    private fun generateDbConnection(): Connection =
        DriverManager.getConnection("jdbc:h2:mem:test;MODE=MYSQL;DB_CLOSE_DELAY=-1", "sa", "")

    private fun runPerformance(numIterations: Int, methodName: String, block: (PolicyDao) -> Unit) {
      val daos = generateDaos()

      val totalDurations = mutableMapOf<String, Long>() // To store the total duration per DAO
      val counts = mutableMapOf<String, Int>() // To count the number of iterations per DAO

      for (i in 1..numIterations) {
        // Randomly pick a DAO from the list
        val dao = daos[Random.nextInt(daos.size)]
        val daoName = dao.name()

        // Measure the time taken for function
        val start = System.nanoTime()
        block(dao)
        val end = System.nanoTime()
        val durationInUs = (end - start) / 1_000

        if (i <= NUM_WARM_UP_ITERATIONS) {
          continue;
        }

        // Accumulate the total duration for this DAO
        totalDurations[daoName] = totalDurations.getOrDefault(daoName, 0L) + durationInUs
        counts[daoName] = counts.getOrDefault(daoName, 0) + 1
      }

      // Calculate and print the average duration per DAO
      totalDurations.entries.sortedBy { it.key }.forEach { (daoName, totalDuration) ->
        val averageDuration = totalDuration / counts[daoName]!!.toDouble()
        logger.info("Average time taken by $daoName#${methodName}: $averageDuration μs")
      }
    }
  }

  @BeforeEach
  fun resetDatabase() {
    val connection = generateDbConnection()
    RunScript.execute(connection, "DROP TABLE policies IF EXISTS;".reader())
    RunScript.execute(connection, this::class.java.getResourceAsStream("/schema.sql")!!.reader(StandardCharsets.UTF_8))
  }

  @ParameterizedTest(name = "{displayName} - [daoName: {0}]")
  @MethodSource("getPoliciesTestDataList")
  fun `getPolicies successful fetches data`(daoName: String, dao: PolicyDao) {
    val result = dao.getPolicies()
    expect(result).toHaveSize(50_000)
  }

  @ParameterizedTest(name = "{displayName} - [daoName: {0}]")
  @MethodSource("addPoliciesTestDataList")
  fun `addPolicies successful persists data`(daoName: String, dao: PolicyDao, randomPolicies: List<Policy>) {
    val result = dao.addPolicies(randomPolicies)
    expect(randomPolicies).toHaveSize(result)
    expect(dao.countPolicies()).toBe(50_000L + result)
    expect(dao.getPolicies()).toHaveSize(50_000 + result)
  }

  @Test
  fun `getPolicies performance`() {
    runPerformance(numIterations = 100, methodName = "getPolicies") { dao ->
      dao.getPolicies()
    }
  }

  @Test
  fun `addPolicies performance`() {
    runPerformance(numIterations = 20_000, methodName = "addPolicies") { dao ->
      dao.addPolicies(generateRandomPolicies())
    }
  }
}
