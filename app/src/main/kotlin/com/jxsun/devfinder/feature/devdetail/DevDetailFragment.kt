package com.jxsun.devfinder.feature.devdetail

import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.jxsun.devfinder.R
import com.jxsun.devfinder.base.BaseFragment
import com.jxsun.devfinder.model.GitHubUser
import com.jxsun.devfinder.model.exception.ClientException
import com.jxsun.devfinder.model.exception.NoConnectionException
import com.jxsun.devfinder.model.exception.ServerException
import com.jxsun.devfinder.util.GlideApp
import kotlinx.android.synthetic.main.fragment_dev_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class DevDetailFragment : BaseFragment<DevDetailUiEvent, DevDetailUiState, DevDetailViewModel>() {

    private val devDetailViewModel: DevDetailViewModel by viewModel()

    override fun layoutResource(): Int = R.layout.fragment_dev_detail

    override fun bindViewModel(): DevDetailViewModel = devDetailViewModel

    override fun onInitUi() {
        backBtn.setOnClickListener {
            activity?.finish()
        }

        getBundledUser()?.let {
            devDetailViewModel.fireEvent(DevDetailUiEvent.GetUserDetailEvent(login = it.loginName))
        } ?: run {
            Timber.w("no given user data!!")
            activity?.finish()
        }
    }

    override fun renderUi(uiState: DevDetailUiState) {
        Timber.d("uiState: $uiState")
        if (uiState.isLoading) {
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.GONE
        }

        if (uiState.error != null) {
            showError(uiState.error)
        } else {
            uiState.userDetail?.let {
                GlideApp.with(this)
                        .load(it.avatarUrl)
                        .into(avatar)

                name.text = it.name
                bio.text = it.bio
                login.text = it.loginName
                staffBadge.visibility = if (it.siteAdmin) View.VISIBLE else View.GONE
                place.text = it.location
                blogUrl.text = it.blog
            }
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

    override fun onDeinitUi() {
    }

    companion object {

        const val TAG = "DevDetailFragment"
        private const val KEY_USER = "user"

        private fun Fragment.getBundledUser(): GitHubUser? {
            return arguments?.getParcelable(KEY_USER)
        }

        fun newInstance(user: GitHubUser) = DevDetailFragment().apply {
            arguments = bundleOf(KEY_USER to user)
        }
    }
}