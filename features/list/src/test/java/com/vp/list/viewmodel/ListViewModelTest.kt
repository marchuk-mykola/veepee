import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.vp.core.models.ListItem
import com.vp.list.model.ListState
import com.vp.list.model.SearchResponse
import com.vp.list.model.SearchResult
import com.vp.list.service.SearchService
import com.vp.list.viewmodel.ListViewModel
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import org.mockito.Mockito.`when`

class ListViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var viewModel: ListViewModel
    private lateinit var searchService: SearchService
    private lateinit var observer: Observer<SearchResult>
    private lateinit var mockCall: Call<SearchResponse>

    @Before
    fun setUp() {
        searchService = Mockito.mock(SearchService::class.java)
        viewModel = ListViewModel(searchService)
        observer = Mockito.mock(Observer::class.java) as Observer<SearchResult>
        viewModel.observeMovies().observeForever(observer)
        mockCall = Mockito.mock(Call::class.java) as Call<SearchResponse>

        `when`(searchService.search(anyString(), anyInt())).thenReturn(mockCall)
    }

    @Test
    fun testSearchInProgress() {
        // When: A search is initiated
        viewModel.searchMoviesByTitle("test", 1)

        // Then: Verify the initial state is IN_PROGRESS
        Mockito.verify(observer).onChanged(SearchResult.inProgress())
    }

    @Test
    fun testSearchSuccess() {
        // Given: A successful response from the API
        val dummyItems = listOf(ListItem("Title1", "Year1", "imdbID1", "Poster1"))
        val dummyResponse = SearchResponse("True", dummyItems)
        val call = Mockito.mock(Call::class.java) as Call<SearchResponse>
        `when`(searchService.search(anyString(), anyInt())).thenReturn(call)

        Mockito.doAnswer {
            val callback: Callback<SearchResponse> = it.getArgument(0)
            callback.onResponse(call, Response.success(dummyResponse))
            null
        }.`when`(call).enqueue(Mockito.any())

        // When: The search method is called
        viewModel.searchMoviesByTitle("test", 1)

        // Then: Verify the state is updated to SUCCESS
        Mockito.verify(observer).onChanged(SearchResult.success(dummyItems, 0))
    }

    @Test
    fun testSearchError() {
        // Given: An error response from the API
        val call = Mockito.mock(Call::class.java) as Call<SearchResponse>
        `when`(searchService.search(anyString(), anyInt())).thenReturn(call)

        Mockito.doAnswer {
            val callback: Callback<SearchResponse> = it.getArgument(0)
            callback.onFailure(call, Throwable())
            null
        }.`when`(call).enqueue(Mockito.any())

        // When: The search method is called
        viewModel.searchMoviesByTitle("test", 1)

        // Then: Verify the state is updated to ERROR
        Mockito.verify(observer).onChanged(SearchResult.error())
    }

    @Test
    fun testClearingPreviousResults() {
        // Given: An initial search is performed
        viewModel.searchMoviesByTitle("test", 1)

        // When: A new search term is used
        viewModel.searchMoviesByTitle("new test", 1)

        // Then: Verify the LiveData is updated twice indicating clearing of previous results
        Mockito.verify(observer, Mockito.times(2)).onChanged(SearchResult.inProgress())
    }

    @Test
    fun testViewModelUpdatesStateAfterFetchingData() {
        // Given: A successful response from the API
        val dummyItems = listOf(ListItem("Title1", "Year1", "imdbID1", "Poster1"))
        val dummyResponse = SearchResponse("True", dummyItems)
        val call = Mockito.mock(Call::class.java) as Call<SearchResponse>
        `when`(searchService.search(anyString(), anyInt())).thenReturn(call)

        Mockito.doAnswer {
            val callback: Callback<SearchResponse> = it.getArgument(0)
            callback.onResponse(call, Response.success(dummyResponse))
            null
        }.`when`(call).enqueue(Mockito.any())

        // When: The search method is called
        viewModel.searchMoviesByTitle("test", 1)

        // Then: Verify the initial and final states of the LiveData
        val captor = ArgumentCaptor.forClass(SearchResult::class.java)
        Mockito.verify(observer, Mockito.atLeastOnce()).onChanged(captor.capture())
        val capturedValues = captor.allValues
        assertTrue("Initial state should be IN_PROGRESS",
            capturedValues.first().listState == ListState.IN_PROGRESS)
        assertTrue("Final state should be LOADED",
            capturedValues.last().listState == ListState.LOADED)
    }
}
