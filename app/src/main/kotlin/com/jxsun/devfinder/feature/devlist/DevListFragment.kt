package com.jxsun.devfinder.feature.devlist

import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jxsun.devfinder.R
import com.jxsun.devfinder.base.BaseFragment
import com.jxsun.devfinder.feature.devdetail.DevDetailActivity
import com.jxsun.devfinder.model.GitHubUser
import com.jxsun.devfinder.model.exception.ClientException
import com.jxsun.devfinder.model.exception.NoConnectionException
import com.jxsun.devfinder.model.exception.ServerException
import com.jxsun.devfinder.util.OnRecyclerViewScrollListener
import kotlinx.android.synthetic.main.fragment_dev_list.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class DevListFragment : BaseFragment<DevListUiEvent, DevListUiState, DevListViewModel>() {

    private val devListViewModel: DevListViewModel by viewModel()

    private lateinit var devListAdapter: DevListRecyclerViewAdapter
    private val onScrollListener = OnRecyclerViewScrollListener()

    override fun bindViewModel(): DevListViewModel = devListViewModel

    override fun layoutResource(): Int = R.layout.fragment_dev_list

    override fun onInitUi() {
        (activity as AppCompatActivity?)?.setSupportActionBar(toolbar)

        // setup recycler view
        devListAdapter = DevListRecyclerViewAdapter().apply {
            setClickUserListener {
                showUserDetail(it)
            }
        }
        with(devListRecyclerView) {
            adapter = devListAdapter
            layoutManager = LinearLayoutManager(this.context, RecyclerView.VERTICAL, false)
            addOnScrollListener(onScrollListener)
        }

        // setup scrolling event binding
        onScrollListener.setReloadAction {
            Timber.v("fire load more event")
            devListViewModel.fireEvent(DevListUiEvent.LoadMoreEvent)
        }

        devListViewModel.fireEvent(DevListUiEvent.InitialEvent)
    }

    override fun renderUi(uiState: DevListUiState) {
        Timber.d("uiState: $uiState")
        if (uiState.isLoading) {
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.GONE
        }

        if (uiState.error != null) {
            showError(uiState.error)
        } else {
            devListAdapter.submitList(uiState.devList)
        }
    }

    private fun showError(error: Throwable) {
        context?.let {
            when (error) {
                is ServerException -> R.string.server_error
                is ClientException -> R.string.client_error
                is NoConnectionException -> R.string.connectivity_error
                else -> R.string.unknown_error
            }.let { resId ->
                Toast.makeText(it, getString(resId), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showUserDetail(user: GitHubUser) {
        activity?.let {
            DevDetailActivity.start(it, user)
        }
    }

    override fun onDeinitUi() {
        devListRecyclerView?.adapter = null
    }

    companion object {

        const val TAG = "DevListFragment"

        fun newInstance(): DevListFragment {
            return DevListFragment()
        }
    }
}
