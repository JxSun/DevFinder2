package com.jxsun.devfinder.feature.devlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jxsun.devfinder.R
import com.jxsun.devfinder.model.exception.ClientException
import com.jxsun.devfinder.model.exception.NoConnectionException
import com.jxsun.devfinder.model.exception.ServerException
import com.jxsun.devfinder.util.OnRecyclerViewScrollListener
import kotlinx.android.synthetic.main.fragment_dev_list.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class DevListFragment : Fragment() {

    private val devListViewModel: DevListViewModel by viewModel()

    private lateinit var devListAdapter: DevListRecyclerViewAdapter
    private val onScrollListener = OnRecyclerViewScrollListener()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dev_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        (activity as AppCompatActivity?)?.setSupportActionBar(toolbar)

        devListAdapter = DevListRecyclerViewAdapter()
        with(devListRecyclerView) {
            adapter = devListAdapter
            layoutManager = LinearLayoutManager(this.context, RecyclerView.VERTICAL, false)
            addOnScrollListener(onScrollListener)
        }

        onScrollListener.setReloadAction {
            Timber.v("fire load more event")
            devListViewModel.fireEvent(DevListUiEvent.LoadMoreEvent)
        }

        devListViewModel.state.observe(
                viewLifecycleOwner,
                Observer { renderUi(it) }
        )

        devListViewModel.fireEvent(DevListUiEvent.InitialEvent)
    }

    private fun renderUi(uiState: DevListUiState) {
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

    override fun onDestroyView() {
        devListRecyclerView?.adapter = null
        super.onDestroyView()
    }

    companion object {

        const val TAG = "DevListFragment"

        fun newInstance(): DevListFragment {
            return DevListFragment()
        }
    }
}