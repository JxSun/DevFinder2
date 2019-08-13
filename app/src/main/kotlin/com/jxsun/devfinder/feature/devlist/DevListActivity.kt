package com.jxsun.devfinder.feature.devlist

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jxsun.devfinder.R
import com.jxsun.devfinder.util.extension.transaction

class DevListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dev_list)

        if (supportFragmentManager.findFragmentByTag(DevListFragment.TAG) == null) {
            supportFragmentManager.transaction {
                it.replace(R.id.contentFrame, DevListFragment.newInstance(), DevListFragment.TAG)
            }
        }
    }
}
