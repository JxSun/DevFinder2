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
import kotlinx.android.synthetic.main.item_dev_list_title.view.*

private const val ITEM_TYPE_TITLE = 0
private const val ITEM_TYPE_USER = 1

typealias ClickUserListener = (GitHubUser) -> Unit

class DevListRecyclerViewAdapter :
    ListAdapter<GitHubUser, RecyclerView.ViewHolder>(PlantDiffCallback()) {

    private var clickUserListener: ClickUserListener? = null

    // To be used like a placeholder for the title item. See the explanation in the below
    // overridden submitList().
    private val dummyUser = GitHubUser(id = 0, loginName = "", avatarUrl = "", siteAdmin = false)

    fun setClickUserListener(listener: ClickUserListener) {
        clickUserListener = listener
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> ITEM_TYPE_TITLE
            else -> ITEM_TYPE_USER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ITEM_TYPE_TITLE -> {
                inflater.inflate(R.layout.item_dev_list_title, parent, false)
                    .run {
                        TitleViewHolder(this)
                    }
            }
            ITEM_TYPE_USER -> {
                inflater.inflate(R.layout.item_dev_list, parent, false)
                    .run {
                        UserViewHolder(this)
                    }
            }
            else -> throw IllegalStateException("Unknown item view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == ITEM_TYPE_USER) {
            val user = getItem(position)
            (holder as UserViewHolder).bind(user, clickUserListener)
        }
    }

    override fun submitList(list: List<GitHubUser>?) {
        // Because we have a title item which doesn't not belong to the calculation result of
        // the business logic. To ensure the underlying AsyncListDiffer can correctly trigger data
        // change events, we have to insert a dummy data as a placeholder for the title item into
        // the list before submitting to AsyncListDiffer.
        super.submitList(list?.toMutableList()?.apply {
            add(0, dummyUser)
        })
    }

    class TitleViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.usersTitle

        init {
            title.text = itemView.context.getString(R.string.users)
        }
    }

    class UserViewHolder(
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
