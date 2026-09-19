package com.example.temacker.feature_tasks.presentation.navigation

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.example.temacker.feature_tasks.domain.model.HandoffDestination
import org.junit.jupiter.api.Test

class NotificationRouteTest {

    @Test
    fun `each destination maps to its route`() {
        assertThat(notificationRoute(HandoffDestination.INCOMING, "t1", "h1")).isEqualTo(IncomingRoute("t1", "h1"))
        assertThat(notificationRoute(HandoffDestination.DECLINE, "t1", "h1")).isEqualTo(DeclineRoute("t1", "h1"))
        assertThat(notificationRoute(HandoffDestination.TASK_DETAIL, "t1", null)).isEqualTo(TaskDetailRoute("t1"))
    }

    @Test
    fun `an unusable intent yields no route`() {
        assertThat(notificationRoute(null, "t1", "h1")).isNull()
        assertThat(notificationRoute(HandoffDestination.INCOMING, null, "h1")).isNull()
        assertThat(notificationRoute(HandoffDestination.INCOMING, "t1", "")).isNull()
        assertThat(notificationRoute(HandoffDestination.DECLINE, "t1", null)).isNull()
    }

    @Test
    fun `a target needs a project as well as a route`() {
        assertThat(notificationTarget(HandoffDestination.INCOMING, "p1", "t1", "h1"))
            .isEqualTo(NotificationTarget("p1", IncomingRoute("t1", "h1")))
        assertThat(notificationTarget(HandoffDestination.INCOMING, null, "t1", "h1")).isNull()
        assertThat(notificationTarget(HandoffDestination.INCOMING, "p1", null, "h1")).isNull()
    }
}
