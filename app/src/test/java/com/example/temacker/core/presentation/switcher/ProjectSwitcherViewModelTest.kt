package com.example.temacker.core.presentation.switcher

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.example.temacker.core.domain.model.ProjectRef
import com.example.temacker.core.domain.model.ProjectSummary
import com.example.temacker.core.domain.repository.CurrentProjectProvider
import com.example.temacker.core.domain.repository.SelectedProjectStore
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectSwitcherViewModelTest {

    private val a = ProjectRef("a", "Alpha")
    private val b = ProjectRef("b", "Beta")
    private val c = ProjectRef("c", "Gamma")

    // The selected id drives the current id, as ProjectCurrentProjectProvider does.
    private class FakeStore(initial: String?) : SelectedProjectStore {
        val selected = MutableStateFlow(initial)
        override fun observeSelectedProjectId(): Flow<String?> = selected
        override suspend fun setSelectedProjectId(id: String) {
            selected.value = id
        }
        override suspend fun clear() {
            selected.value = null
        }
    }

    private class FakeProvider(private val refs: List<ProjectRef>, private val store: FakeStore) : CurrentProjectProvider {
        override fun observeCurrentProjectId(): Flow<Result<String?, DataError>> = store.selected.map { Result.Success(it) }
        override fun observeCurrentProjectSummary(): Flow<Result<ProjectSummary?, DataError>> = throw NotImplementedError()
        override fun observeUserProjectRefs(): Flow<Result<List<ProjectRef>, DataError>> = MutableStateFlow(Result.Success(refs))
    }

    private fun viewModel(refs: List<ProjectRef>, selected: String?): Pair<ProjectSwitcherViewModel, FakeStore> {
        val store = FakeStore(selected)
        return ProjectSwitcherViewModel(FakeProvider(refs, store), store) to store
    }

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `middle project has both neighbours in list order`() = runTest {
        val (vm, _) = viewModel(listOf(a, b, c), selected = "b")
        assertThat(vm.state.value).isEqualTo(ProjectSwitcherState(previous = a, next = c))
    }

    @Test
    fun `first project has no previous and last has no next`() = runTest {
        val (first, _) = viewModel(listOf(a, b, c), selected = "a")
        assertThat(first.state.value).isEqualTo(ProjectSwitcherState(previous = null, next = b))
        val (last, _) = viewModel(listOf(a, b, c), selected = "c")
        assertThat(last.state.value).isEqualTo(ProjectSwitcherState(previous = b, next = null))
    }

    @Test
    fun `a single project has no neighbours`() = runTest {
        val (vm, _) = viewModel(listOf(a), selected = "a")
        assertThat(vm.state.value).isEqualTo(ProjectSwitcherState())
    }

    @Test
    fun `swiping up selects the next project and the neighbours follow`() = runTest {
        val (vm, store) = viewModel(listOf(a, b, c), selected = "a")
        vm.onAction(ProjectSwitcherAction.OnSwipeToNext)
        assertThat(store.selected.value).isEqualTo("b")
        assertThat(vm.state.value).isEqualTo(ProjectSwitcherState(previous = a, next = c))
    }

    @Test
    fun `swiping down selects the previous project`() = runTest {
        val (vm, store) = viewModel(listOf(a, b, c), selected = "c")
        vm.onAction(ProjectSwitcherAction.OnSwipeToPrevious)
        assertThat(store.selected.value).isEqualTo("b")
    }

    @Test
    fun `swiping past the end does nothing`() = runTest {
        val (vm, store) = viewModel(listOf(a, b, c), selected = "c")
        vm.onAction(ProjectSwitcherAction.OnSwipeToNext)
        assertThat(store.selected.value).isEqualTo("c")
        assertThat(vm.state.value.next).isNull()
    }
}
