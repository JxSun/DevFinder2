package com.jxsun.devfinder.feature.devdetail

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jxsun.devfinder.R
import com.jxsun.devfinder.model.GitHubUser
import com.jxsun.devfinder.util.extension.transaction

class DevDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dev_detail)

        supportFragmentManager.transaction {
            it.replace(R.id.contentFrame, DevDetailFragment.newInstance(getBundledUser()), DevDetailFragment.TAG)
        }
    }

    companion object {
        private const val KEY_USER = "user"

        private fun Activity.getBundledUser(): GitHubUser {
            return intent.getParcelableExtra(KEY_USER)
        }

        fun start(activity: Activity, user: GitHubUser) {
            activity.startActivity(
                    Intent(activity, DevDetailActivity::class.java)
                            .putExtra(KEY_USER, user)
            )
        }
    }
}