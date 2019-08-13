package com.jxsun.devfinder.feature.devlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.jxsun.devfinder.R
import com.jxsun.devfinder.model.GitHubUser
import com.jxsun.devfinder.util.GlideApp
import kotlinx.android.synthetic.main.item_dev_list.view.*

typealias ClickUserListener = (GitHubUser) -> Unit

class DevListRecyclerViewAdapter :
    ListAdapter<GitHubUser, DevListRecyclerViewAdapter.ViewHolder>(PlantDiffCallback()) {

    private var clickUserListener: ClickUserListener? = null

    fun setClickUserListener(listener: ClickUserListener) {
        clickUserListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dev_list, parent, false)
            .run {
                ViewHolder(this)
            }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user, clickUserListener)
    }

    class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        private val avatar: ImageView = itemView.avatar
        private val login: TextView = itemView.loginName
        private val staffBadge: TextView = itemView.staffBadge

        private val clickListenerWrapper = OnClickListenerWrapper()

        init {
            // Setup a onClickListener's wrapper to avoid instantiating new onClickListener instances frequently.
            itemView.setOnClickListener(clickListenerWrapper)
        }

        fun bind(user: GitHubUser, clickListener: ClickUserListener?) {
            login.text = user.loginName
            GlideApp.with(itemView.context)
                .load(user.avatarUrl)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(avatar)
            staffBadge.visibility = if (user.siteAdmin) View.VISIBLE else View.GONE
            clickListenerWrapper.bind(clickListener, user)
        }
    }

    /**
     * An internal [View.OnClickListener] wrapper to help redirect the click events to the
     * associated [ClickUserListener].
     */
    private class OnClickListenerWrapper : View.OnClickListener {
        private var user: GitHubUser? = null
        private var realListener: ClickUserListener? = null

        override fun onClick(v: View?) {
            user?.let { realListener?.invoke(it) }
        }

        fun bind(listener: ClickUserListener?, user: GitHubUser?) {
            this.user = user
            this.realListener = listener
        }
    }
}

private class PlantDiffCallback : DiffUtil.ItemCallback<GitHubUser>() {

    override fun areItemsTheSame(oldItem: GitHubUser, newItem: GitHubUser): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: GitHubUser, newItem: GitHubUser): Boolean {
        return oldItem == newItem
    }
}
