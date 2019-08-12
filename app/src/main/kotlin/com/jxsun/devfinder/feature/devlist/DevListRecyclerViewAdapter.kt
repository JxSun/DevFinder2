package com.jxsun.devfinder.feature.devlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jxsun.devfinder.R
import com.jxsun.devfinder.model.GitHubUser
import com.jxsun.devfinder.util.GlideApp
import kotlinx.android.synthetic.main.item_dev_list.view.*

class DevListRecyclerViewAdapter : ListAdapter<GitHubUser, DevListRecyclerViewAdapter.ViewHolder>(PlantDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return LayoutInflater.from(parent.context)
                .inflate(R.layout.item_dev_list, parent, false)
                .run {
                    ViewHolder(this)
                }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user)
    }

    class ViewHolder(
            itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        private val avatar: ImageView = itemView.avatar
        private val login: TextView = itemView.loginName
        private val staffBadge: TextView = itemView.staffBadge

        fun bind(user: GitHubUser) {
            login.text = user.loginName
            GlideApp.with(itemView.context)
                    .load(user.avatarUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(avatar)
            if (user.siteAdmin) {
                staffBadge.visibility = View.VISIBLE
            } else {
                staffBadge.visibility = View.GONE
            }
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