package com.lifeos.app.features.planner.data.repository

import com.lifeos.app.core.network.ApiException
import com.lifeos.app.core.network.AuthTokenProvider
import com.lifeos.app.core.network.HttpClientFactory
import com.lifeos.app.features.planner.data.remote.PlannerRemoteDataSource
import com.lifeos.app.features.planner.domain.model.CreateTaskRequest
import com.lifeos.app.features.planner.domain.model.TaskCategory
import com.lifeos.app.features.planner.domain.model.TaskDueDate
import com.lifeos.app.features.planner.domain.model.TaskNotFoundException
import com.lifeos.app.features.planner.domain.model.TaskPriority
import com.lifeos.app.features.planner.domain.model.TaskSource
import com.lifeos.app.features.planner.domain.model.TaskStatus
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Pins the current behavior of [PlannerRepositoryImpl] against the real
 * [PlannerRemoteDataSource] and the shared authenticated Ktor client, with
 * HTTP served by [MockEngine], including the real Planner Calendar range
 * queries against `GET /planner/tasks`.
 */
class PlannerRepositoryImplTest {

    private val recordedRequests = mutableListOf<HttpRequestData>()
    private var routes: MutableMap<String, MockRequestHandleScope.() -> HttpResponseData> = mutableMapOf()

    private val engine = MockEngine { request ->
        recordedRequests += request
        val key = "${request.method.value} ${request.url.encodedPath.substringAfter("/api/v1/")}"
        val handler = routes[key] ?: error("Unexpected request: $key")
        handler()
    }

    private fun repositoryAt(today: LocalDate) = PlannerRepositoryImpl(
        remoteDataSource = PlannerRemoteDataSource(
            HttpClientFactory.create(
                engine = engine,
                tokenProvider = object : AuthTokenProvider {
                    override suspend fun currentAccessToken(): String = ACCESS_TOKEN
                    override suspend fun refreshAccessToken(): String? = null
                },
            ),
        ),
        today = { today },
    )

    private val repository = repositoryAt(FIXED_TODAY)

    @Test
    fun getDashboard_mapsBackendDtoToDomain() = runTest {
        routes["GET planner/dashboard"] = {
            respondJson(
                """
                {"data":{
                  "todayTasks":[${taskJson(id = TASK_ID, status = "TODO", dueTime = "\"14:30\"")}],
                  "upcomingTasks":[${taskJson(id = OTHER_TASK_ID, status = "DONE", dueDate = "2026-10-05", dueTime = "null", priority = "HIGH", category = "WORK", source = "TRAVEL")}],
                  "completedCount":3,
                  "pendingCount":2,
                  "progressPercentage":60,
                  "highPriorityTasks":[],
                  "travelTasks":[]
                }}
                """.trimIndent(),
            )
        }
        routes["GET planner/tasks"] = { respondJson(pageJson(tasks = emptyList())) }

        val dashboard = repository.getDashboard().getOrThrow()

        assertEquals(
            "Bearer $ACCESS_TOKEN",
            recordedRequests.single { it.routeKey() == "GET planner/dashboard" }.headers[HttpHeaders.Authorization],
        )
        assertEquals(5, dashboard.overview.totalTaskCount)
        assertEquals(3, dashboard.overview.completedTaskCount)
        assertEquals(60, dashboard.overview.productivityPercent)
        assertEquals(1, dashboard.overview.upcomingEventCount)
        assertEquals("", dashboard.aiInsightMessage)

        val today = dashboard.todayTasks.single()
        assertEquals(TASK_ID, today.id)
        assertEquals(TaskDueDate(LocalDate(2026, 10, 2), LocalTime(14, 30)), today.dueDate)
        assertEquals(TaskStatus.TODO, today.status)
        assertEquals(TaskPriority.MEDIUM, today.priority)
        assertEquals(TaskCategory.PERSONAL, today.category)
        assertEquals(TaskSource.PLANNER, today.source)
        assertEquals(LocalDate(2026, 10, 2), today.createdAt)
        assertTrue(today.tags.isEmpty())

        val upcoming = dashboard.upcomingTasks.single()
        assertEquals(TaskDueDate(LocalDate(2026, 10, 5), null), upcoming.dueDate)
        assertEquals(TaskStatus.DONE, upcoming.status)
        assertEquals(TaskPriority.HIGH, upcoming.priority)
        assertEquals(TaskCategory.WORK, upcoming.category)
        assertEquals(TaskSource.TRAVEL, upcoming.source)
    }

    @Test
    fun createTask_sendsExpectedBody_andMapsResponse() = runTest {
        routes["POST planner/tasks"] = {
            respondJson(
                """{"data":${taskJson(id = TASK_ID, title = "Sunum", dueDate = "2026-10-03", dueTime = "\"09:05\"", priority = "HIGH", category = "WORK")}}""",
                HttpStatusCode.Created,
            )
        }

        val task = repository.createTask(
            CreateTaskRequest(
                title = "Sunum",
                description = "Slaytlar",
                dueDate = TaskDueDate(LocalDate(2026, 10, 3), LocalTime(9, 5)),
                priority = TaskPriority.HIGH,
                category = TaskCategory.WORK,
                tags = listOf("iş"),
                hasReminder = true,
                estimatedDurationLabel = "45 dakika",
                notes = "not",
            ),
        ).getOrThrow()

        val request = recordedRequests.single()
        assertEquals(HttpMethod.Post, request.method)
        val body = Json.parseToJsonElement(request.bodyText()).jsonObject
        assertEquals("Sunum", body["title"]?.jsonPrimitive?.content)
        assertEquals("Slaytlar", body["description"]?.jsonPrimitive?.content)
        assertEquals("2026-10-03", body["dueDate"]?.jsonPrimitive?.content)
        assertEquals("09:05", body["dueTime"]?.jsonPrimitive?.content)
        assertEquals("HIGH", body["priority"]?.jsonPrimitive?.content)
        assertEquals("WORK", body["category"]?.jsonPrimitive?.content)
        // Current behavior: tags, reminder, estimated duration and notes are not sent.
        assertEquals(setOf("title", "description", "dueDate", "dueTime", "priority", "category", "taskListId"), body.keys)

        assertEquals(TASK_ID, task.id)
        assertEquals(TaskDueDate(LocalDate(2026, 10, 3), LocalTime(9, 5)), task.dueDate)
        assertEquals(TaskPriority.HIGH, task.priority)
        assertEquals(TaskCategory.WORK, task.category)
        // Current behavior: the returned task echoes the request's client-only fields.
        assertEquals(listOf("iş"), task.tags)
        assertTrue(task.hasReminder)
        assertEquals("45 dakika", task.estimatedDurationLabel)
    }

    @Test
    fun createTask_withoutTime_sendsNullDueTime() = runTest {
        routes["POST planner/tasks"] = {
            respondJson("""{"data":${taskJson(id = TASK_ID, dueTime = "null")}}""", HttpStatusCode.Created)
        }

        repository.createTask(request(time = null)).getOrThrow()

        val body = Json.parseToJsonElement(recordedRequests.single().bodyText()).jsonObject
        assertIs<JsonNull>(body.getValue("dueTime"))
    }

    @Test
    fun toggleTaskCompletion_whenDone_callsIncomplete() = runTest {
        routes["GET planner/tasks/$TASK_ID"] = { respondJson("""{"data":${taskJson(id = TASK_ID, status = "DONE")}}""") }
        routes["PATCH planner/tasks/$TASK_ID/incomplete"] = { respondJson("""{"data":${taskJson(id = TASK_ID, status = "TODO")}}""") }

        assertTrue(repository.toggleTaskCompletion(TASK_ID).isSuccess)

        assertEquals(
            listOf("GET planner/tasks/$TASK_ID", "PATCH planner/tasks/$TASK_ID/incomplete"),
            recordedRequests.map { it.routeKey() },
        )
    }

    @Test
    fun toggleTaskCompletion_whenNotDone_callsComplete() = runTest {
        for (status in listOf("TODO", "IN_PROGRESS")) {
            recordedRequests.clear()
            routes["GET planner/tasks/$TASK_ID"] = { respondJson("""{"data":${taskJson(id = TASK_ID, status = status)}}""") }
            routes["PATCH planner/tasks/$TASK_ID/complete"] = { respondJson("""{"data":${taskJson(id = TASK_ID, status = "DONE")}}""") }

            assertTrue(repository.toggleTaskCompletion(TASK_ID).isSuccess)

            assertEquals(
                listOf("GET planner/tasks/$TASK_ID", "PATCH planner/tasks/$TASK_ID/complete"),
                recordedRequests.map { it.routeKey() },
                "status=$status",
            )
        }
    }

    @Test
    fun deleteTask_treats204AsSuccess() = runTest {
        routes["DELETE planner/tasks/$TASK_ID"] = { respond(content = "", status = HttpStatusCode.NoContent) }

        assertTrue(repository.deleteTask(TASK_ID).isSuccess)
        assertEquals(HttpMethod.Delete, recordedRequests.single().method)
    }

    @Test
    fun taskEndpoints_map404ToTaskNotFoundException() = runTest {
        val notFound: MockRequestHandleScope.() -> HttpResponseData = {
            respondJson(
                """{"statusCode":404,"error":"NOT_FOUND","message":"Task not found.","path":"/x","timestamp":"t"}""",
                HttpStatusCode.NotFound,
            )
        }
        routes["GET planner/tasks/$TASK_ID"] = notFound
        routes["DELETE planner/tasks/$TASK_ID"] = notFound

        assertIs<TaskNotFoundException>(repository.getTaskDetail(TASK_ID).exceptionOrNull())
        assertIs<TaskNotFoundException>(repository.toggleTaskCompletion(TASK_ID).exceptionOrNull())
        assertIs<TaskNotFoundException>(repository.deleteTask(TASK_ID).exceptionOrNull())
    }

    /**
     * Documents current behavior: `TaskDto.toDomain()` uses `valueOf`, so a
     * single task with an enum value the app doesn't know (e.g. a future
     * backend priority) makes the whole dashboard fail with
     * [IllegalArgumentException]. The repository's catch turns it into a
     * failed [Result] rather than a crash. Not changed in this step.
     */
    @Test
    fun getDashboard_withUnknownEnumValue_failsWholeDashboard() = runTest {
        routes["GET planner/dashboard"] = {
            respondJson(
                """{"data":{"todayTasks":[${taskJson(id = TASK_ID, priority = "URGENT")}],"upcomingTasks":[],"completedCount":0,"pendingCount":1,"progressPercentage":0}}""",
            )
        }
        routes["GET planner/tasks"] = { respondJson(pageJson(tasks = emptyList())) }

        val result = repository.getDashboard()

        assertTrue(result.isFailure)
        assertIs<IllegalArgumentException>(result.exceptionOrNull())
    }

    @Test
    fun getTaskDetail_mapsTask_andLeavesUnsupportedFieldsEmpty() = runTest {
        routes["GET planner/tasks/$TASK_ID"] = { respondJson("""{"data":${taskJson(id = TASK_ID, description = "\"Açıklama\"")}}""") }

        val detail = repository.getTaskDetail(TASK_ID).getOrThrow()

        assertEquals(TASK_ID, detail.task.id)
        assertEquals("Açıklama", detail.task.description)
        assertEquals("", detail.notes)
        assertTrue(detail.subtasks.isEmpty())
        assertTrue(detail.attachments.isEmpty())
        assertTrue(detail.activity.isEmpty())
    }

    @Test
    fun getDashboard_networkFailure_returnsFailure() = runTest {
        routes["GET planner/dashboard"] = {
            respondJson(
                """{"statusCode":500,"error":"INTERNAL_SERVER_ERROR","message":"boom","path":"/x","timestamp":"t"}""",
                HttpStatusCode.InternalServerError,
            )
        }
        routes["GET planner/tasks"] = { respondJson(pageJson(tasks = emptyList())) }

        val result = repository.getDashboard()

        assertTrue(result.isFailure)
        assertNull(result.getOrNull())
    }

    @Test
    fun getDashboard_usesRealTaskCountsForCurrentMonth() = runTest {
        val today = FIXED_TODAY
        routes["GET planner/dashboard"] = { respondJson(EMPTY_DASHBOARD_JSON) }
        routes["GET planner/tasks"] = {
            respondJson(
                pageJson(
                    tasks = listOf(
                        taskJson(id = TASK_ID, dueDate = today.toString()),
                        taskJson(id = OTHER_TASK_ID, dueDate = today.toString()),
                    ),
                ),
            )
        }

        val calendar = repository.getDashboard().getOrThrow().calendar

        val request = tasksRequests().single()
        assertEquals(LocalDate(today.year, today.monthNumber, 1).toString(), request.url.parameters["dueAfter"])
        val todayCell = calendar.days.single { it.date == today }
        assertTrue(todayCell.isToday)
        assertEquals(2, todayCell.taskCount)
        assertEquals(2, calendar.days.sumOf { it.taskCount })
    }

    @Test
    fun getDashboard_taskRangeFailure_stillReturnsDashboard_withZeroCounts() = runTest {
        routes["GET planner/dashboard"] = {
            respondJson(
                """{"data":{"todayTasks":[${taskJson(id = TASK_ID)}],"upcomingTasks":[],"completedCount":1,"pendingCount":1,"progressPercentage":50}}""",
            )
        }
        routes["GET planner/tasks"] = { serverError() }

        val result = repository.getDashboard()

        assertTrue(result.isSuccess)
        val dashboard = result.getOrThrow()
        assertEquals(TASK_ID, dashboard.todayTasks.single().id)
        assertEquals(2, dashboard.overview.totalTaskCount)
        assertEquals(50, dashboard.overview.productivityPercent)
        assertTrue(dashboard.calendar.days.isNotEmpty())
        assertTrue(dashboard.calendar.days.all { it.taskCount == 0 })
        assertTrue(dashboard.calendar.days.any { it.isToday })
    }

    @Test
    fun getDashboard_taskRangeNetworkError_stillReturnsDashboard() = runTest {
        routes["GET planner/dashboard"] = { respondJson(EMPTY_DASHBOARD_JSON) }
        routes["GET planner/tasks"] = { error("offline") }

        val dashboard = repository.getDashboard().getOrThrow()

        assertTrue(dashboard.calendar.days.all { it.taskCount == 0 })
    }

    @Test
    fun getDashboard_sendsLocalDate_andUsesItForCalendarAndDashboardDate() = runTest {
        routes["GET planner/dashboard"] = { respondJson(EMPTY_DASHBOARD_JSON) }
        routes["GET planner/tasks"] = { respondJson(pageJson(tasks = emptyList())) }

        val dashboard = repository.getDashboard().getOrThrow()

        val dashboardRequest = recordedRequests.single { it.routeKey() == "GET planner/dashboard" }
        assertEquals("2026-10-02", dashboardRequest.url.parameters["date"])
        assertEquals(setOf("date"), dashboardRequest.url.parameters.names())
        val range = tasksRequests().single().url.parameters
        assertEquals("2026-10-01", range["dueAfter"])
        assertEquals("2026-10-31", range["dueBefore"])
        assertEquals(FIXED_TODAY, dashboard.date)
        assertEquals(listOf(FIXED_TODAY), dashboard.calendar.days.filter { it.isToday }.map { it.date })
    }

    @Test
    fun getDashboard_onLastDayOfMonth_keepsDashboardAndCalendarOnTheSameDay() = runTest {
        val lastDay = LocalDate(2026, 10, 31)
        routes["GET planner/dashboard"] = { respondJson(EMPTY_DASHBOARD_JSON) }
        routes["GET planner/tasks"] = { respondJson(pageJson(tasks = emptyList())) }

        val dashboard = repositoryAt(lastDay).getDashboard().getOrThrow()

        assertEquals("2026-10-31", recordedRequests.single { it.routeKey() == "GET planner/dashboard" }.url.parameters["date"])
        val range = tasksRequests().single().url.parameters
        assertEquals("2026-10-01", range["dueAfter"])
        assertEquals("2026-10-31", range["dueBefore"])
        assertEquals(lastDay, dashboard.date)
        assertEquals("Ekim 2026", dashboard.calendar.monthLabel)
    }

    @Test
    fun getDashboard_mapsOverdueTasks_includingInProgress() = runTest {
        routes["GET planner/dashboard"] = {
            respondJson(
                """{"data":{
                  "overdueTasks":[
                    ${taskJson(id = "late-todo", dueDate = "2026-09-28", dueTime = "\"09:00\"", priority = "HIGH")},
                    ${taskJson(id = "late-active", dueDate = "2026-09-30", status = "IN_PROGRESS")}
                  ],
                  "todayTasks":[${taskJson(id = TASK_ID)}],
                  "upcomingTasks":[],
                  "completedCount":0,"pendingCount":3,"progressPercentage":0}}""",
            )
        }
        routes["GET planner/tasks"] = { respondJson(pageJson(tasks = emptyList())) }

        val dashboard = repository.getDashboard().getOrThrow()

        assertEquals(listOf("late-todo", "late-active"), dashboard.overdueTasks.map { it.id })
        val first = dashboard.overdueTasks.first()
        assertEquals(TaskDueDate(LocalDate(2026, 9, 28), LocalTime(9, 0)), first.dueDate)
        assertEquals(TaskPriority.HIGH, first.priority)
        assertEquals(TaskStatus.IN_PROGRESS, dashboard.overdueTasks[1].status)
        assertEquals(listOf(TASK_ID), dashboard.todayTasks.map { it.id })
    }

    @Test
    fun getDashboard_withoutOverdueTasksField_mapsToEmptyList() = runTest {
        // EMPTY_DASHBOARD_JSON has no "overdueTasks" key at all, like a backend older than PR #5.
        routes["GET planner/dashboard"] = { respondJson(EMPTY_DASHBOARD_JSON) }
        routes["GET planner/tasks"] = { respondJson(pageJson(tasks = emptyList())) }

        assertEquals(emptyList(), repository.getDashboard().getOrThrow().overdueTasks)
    }

    /** Same documented behavior as [getDashboard_withUnknownEnumValue_failsWholeDashboard]: `valueOf` mapping applies to overdue tasks too. */
    @Test
    fun getDashboard_withUnknownEnumInOverdueTasks_failsWholeDashboard() = runTest {
        routes["GET planner/dashboard"] = {
            respondJson(
                """{"data":{"overdueTasks":[${taskJson(id = "late", dueDate = "2026-09-30", status = "BLOCKED")}],"todayTasks":[],"upcomingTasks":[],"completedCount":0,"pendingCount":1,"progressPercentage":0}}""",
            )
        }
        routes["GET planner/tasks"] = { respondJson(pageJson(tasks = emptyList())) }

        assertIs<IllegalArgumentException>(repository.getDashboard().exceptionOrNull())
    }

    @Test
    fun getCalendarMonth_requestsWholeMonth_withMaxLimit() = runTest {
        routes["GET planner/tasks"] = { respondJson(pageJson(tasks = emptyList())) }

        repository.getCalendarMonth(2028, 2).getOrThrow()

        val parameters = tasksRequests().single().url.parameters
        assertEquals("2028-02-01", parameters["dueAfter"])
        assertEquals("2028-02-29", parameters["dueBefore"])
        assertEquals("100", parameters["limit"])
        assertEquals("dueDate", parameters["sort"])
        assertNull(parameters["cursor"])
        assertEquals("Bearer $ACCESS_TOKEN", tasksRequests().single().headers[HttpHeaders.Authorization])
    }

    @Test
    fun getCalendarMonth_mapsRealTaskCountsToDays() = runTest {
        routes["GET planner/tasks"] = {
            respondJson(
                pageJson(
                    tasks = listOf(
                        taskJson(id = "a", dueDate = "2026-10-02"),
                        taskJson(id = "b", dueDate = "2026-10-02"),
                        taskJson(id = "c", dueDate = "2026-10-15", status = "DONE"),
                    ),
                ),
            )
        }

        val calendar = repository.getCalendarMonth(2026, 10).getOrThrow()

        fun countOn(day: Int) = calendar.days.single { it.date == LocalDate(2026, 10, day) }.taskCount
        assertEquals(2, countOn(2))
        assertEquals(1, countOn(15))
        assertEquals(0, countOn(3))
        assertEquals(3, calendar.days.sumOf { it.taskCount })
        assertEquals(31, calendar.days.count { it.isCurrentMonth })
        assertTrue(calendar.days.filterNot { it.isCurrentMonth }.all { it.taskCount == 0 })
    }

    @Test
    fun getCalendarMonth_emptyRange_returnsGridWithZeroCounts() = runTest {
        routes["GET planner/tasks"] = { respondJson(pageJson(tasks = emptyList())) }

        val calendar = repository.getCalendarMonth(2026, 9).getOrThrow()

        assertEquals(30, calendar.days.count { it.isCurrentMonth })
        assertTrue(calendar.days.all { it.taskCount == 0 })
    }

    @Test
    fun getCalendarMonth_followsCursorUntilHasMoreIsFalse() = runTest {
        routes["GET planner/tasks"] = {
            when (recordedRequests.last().url.parameters["cursor"]) {
                null -> respondJson(pageJson(tasks = listOf(taskJson(id = "a", dueDate = "2026-10-01")), nextCursor = "a", hasMore = true))
                "a" -> respondJson(pageJson(tasks = listOf(taskJson(id = "b", dueDate = "2026-10-20")), nextCursor = "b", hasMore = true))
                else -> respondJson(pageJson(tasks = listOf(taskJson(id = "c", dueDate = "2026-10-20"))))
            }
        }

        val calendar = repository.getCalendarMonth(2026, 10).getOrThrow()

        assertEquals(listOf(null, "a", "b"), tasksRequests().map { it.url.parameters["cursor"] })
        assertTrue(tasksRequests().all { it.url.parameters["dueAfter"] == "2026-10-01" && it.url.parameters["dueBefore"] == "2026-10-31" })
        assertEquals(1, calendar.days.single { it.date == LocalDate(2026, 10, 1) }.taskCount)
        assertEquals(2, calendar.days.single { it.date == LocalDate(2026, 10, 20) }.taskCount)
    }

    @Test
    fun getCalendarMonth_duplicateCursor_stopsSafely() = runTest {
        routes["GET planner/tasks"] = {
            respondJson(pageJson(tasks = listOf(taskJson(id = "a", dueDate = "2026-10-01")), nextCursor = "same", hasMore = true))
        }

        val result = repository.getCalendarMonth(2026, 10)

        assertTrue(result.isSuccess)
        assertEquals(listOf(null, "same"), tasksRequests().map { it.url.parameters["cursor"] })
    }

    @Test
    fun getCalendarMonth_neverEndingPages_stopAtSafetyCap() = runTest {
        routes["GET planner/tasks"] = {
            val next = "cursor-${tasksRequests().size}"
            respondJson(pageJson(tasks = emptyList(), nextCursor = next, hasMore = true))
        }

        assertTrue(repository.getCalendarMonth(2026, 10).isSuccess)
        assertEquals(10, tasksRequests().size)
    }

    @Test
    fun getCalendarMonth_httpFailure_returnsFailure() = runTest {
        routes["GET planner/tasks"] = { serverError() }

        val result = repository.getCalendarMonth(2026, 10)

        assertTrue(result.isFailure)
        assertEquals(500, assertIs<ApiException>(result.exceptionOrNull()).statusCode)
    }

    @Test
    fun getTasksForDay_requestsSingleDayRange_andOrdersByTime() = runTest {
        routes["GET planner/tasks"] = {
            respondJson(
                pageJson(
                    tasks = listOf(
                        taskJson(id = "untimed", dueTime = "null"),
                        taskJson(id = "late", dueTime = "\"18:00\""),
                        taskJson(id = "early", dueTime = "\"08:15\"", priority = "HIGH"),
                    ),
                ),
            )
        }

        val tasks = repository.getTasksForDay(LocalDate(2026, 10, 2)).getOrThrow()

        val parameters = tasksRequests().single().url.parameters
        assertEquals("2026-10-02", parameters["dueAfter"])
        assertEquals("2026-10-02", parameters["dueBefore"])
        assertEquals("100", parameters["limit"])
        assertEquals(listOf("early", "late", "untimed"), tasks.map { it.id })
        assertEquals(TaskPriority.HIGH, tasks.first().priority)
        assertEquals(TaskDueDate(LocalDate(2026, 10, 2), LocalTime(8, 15)), tasks.first().dueDate)
    }

    @Test
    fun getTasksForDay_httpFailure_returnsFailure() = runTest {
        routes["GET planner/tasks"] = { serverError() }

        assertIs<ApiException>(repository.getTasksForDay(LocalDate(2026, 10, 2)).exceptionOrNull())
    }

    @Test
    fun getTasksForDay_networkError_returnsFailure() = runTest {
        routes["GET planner/tasks"] = { error("offline") }

        assertTrue(repository.getTasksForDay(LocalDate(2026, 10, 2)).isFailure)
    }

    @Test
    fun toggleSubtaskCompletion_failsWithoutNetworkCall() = runTest {
        val result = repository.toggleSubtaskCompletion(TASK_ID, "subtask-1")

        assertIs<UnsupportedOperationException>(result.exceptionOrNull())
        assertTrue(recordedRequests.isEmpty())
    }

    private fun request(time: LocalTime?) = CreateTaskRequest(
        title = "Görev",
        description = null,
        dueDate = TaskDueDate(LocalDate(2026, 10, 2), time),
        priority = TaskPriority.MEDIUM,
        category = TaskCategory.PERSONAL,
        tags = emptyList(),
        hasReminder = false,
        estimatedDurationLabel = null,
        notes = null,
    )

    private fun taskJson(
        id: String,
        title: String = "Görev",
        description: String = "null",
        dueDate: String = "2026-10-02",
        dueTime: String = "null",
        priority: String = "MEDIUM",
        category: String = "PERSONAL",
        status: String = "TODO",
        source: String = "PLANNER",
    ) = """{"id":"$id","title":"$title","description":$description,"dueDate":"$dueDate","dueTime":$dueTime,""" +
        """"priority":"$priority","category":"$category","status":"$status","source":"$source","taskListId":null,""" +
        """"createdAt":"2026-10-02T11:35:30.588Z","updatedAt":"2026-10-02T11:35:30.588Z"}"""

    private fun pageJson(tasks: List<String>, nextCursor: String? = null, hasMore: Boolean = false) =
        """{"data":[${tasks.joinToString(",")}],"meta":{"nextCursor":${nextCursor?.let { "\"$it\"" } ?: "null"},"limit":100,"hasMore":$hasMore}}"""

    private fun MockRequestHandleScope.serverError(): HttpResponseData = respondJson(
        """{"statusCode":500,"error":"INTERNAL_SERVER_ERROR","message":"boom","path":"/x","timestamp":"t"}""",
        HttpStatusCode.InternalServerError,
    )

    private fun tasksRequests(): List<HttpRequestData> = recordedRequests.filter { it.routeKey() == "GET planner/tasks" }

    private fun MockRequestHandleScope.respondJson(
        body: String,
        status: HttpStatusCode = HttpStatusCode.OK,
    ): HttpResponseData = respond(
        content = body,
        status = status,
        headers = headersOf(HttpHeaders.ContentType, "application/json"),
    )

    private fun HttpRequestData.routeKey(): String = "${method.value} ${url.encodedPath.substringAfter("/api/v1/")}"

    private fun HttpRequestData.bodyText(): String =
        (body as? OutgoingContent.ByteArrayContent)?.bytes()?.decodeToString().orEmpty()

    private companion object {
        const val ACCESS_TOKEN = "test-access-token"
        const val TASK_ID = "11111111-1111-1111-1111-111111111111"
        const val OTHER_TASK_ID = "22222222-2222-2222-2222-222222222222"
        val FIXED_TODAY = LocalDate(2026, 10, 2)
        const val EMPTY_DASHBOARD_JSON =
            """{"data":{"todayTasks":[],"upcomingTasks":[],"completedCount":0,"pendingCount":0,"progressPercentage":0}}"""
    }
}
