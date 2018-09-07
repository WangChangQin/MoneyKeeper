/*
 * Copyright 2018 Bakumon. https://github.com/Bakumon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package me.bakumon.moneykeeper

import android.app.Application
import com.squareup.leakcanary.LeakCanary
import me.drakeet.floo.Floo
import me.drakeet.floo.Target
import me.drakeet.floo.extensions.LogInterceptor
import me.drakeet.floo.extensions.OpenDirectlyHandler
import java.util.*

/**
 * @author Bakumon https://bakumon.me
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        LeakCanary.install(this)
        // Normal app init code...
        val mappings = HashMap<String, Target>(16)
        mappings[Router.Url.URL_HOME] = Target("mk://bakumon.me/home")
        mappings[Router.Url.URL_ADD_RECORD] = Target("mk://bakumon.me/addRecord")
        mappings[Router.Url.URL_TYPE_MANAGE] = Target("mk://bakumon.me/typeManage")
        mappings[Router.Url.URL_TYPE_SORT] = Target("mk://bakumon.me/typeSort")
        mappings[Router.Url.URL_ADD_TYPE] = Target("mk://bakumon.me/addType")
        mappings[Router.Url.URL_STATISTICS] = Target("mk://bakumon.me/statistics")
        mappings[Router.Url.URL_TYPE_RECORDS] = Target("mk://bakumon.me/typeRecords")
        mappings[Router.Url.URL_SETTING] = Target("mk://bakumon.me/setting")
        mappings[Router.Url.URL_ABOUT] = Target("mk://bakumon.me/about")
        mappings[Router.Url.URL_REVIEW] = Target("mk://bakumon.me/review")
        mappings[Router.Url.URL_BACKUP] = Target("mk://bakumon.me/backup")
        mappings[Router.Url.URL_OTHER_SETTING] = Target("mk://bakumon.me/other_setting")
        mappings[Router.Url.URL_ASSETS] = Target("mk://bakumon.me/assets")
        mappings[Router.Url.URL_CHOOSE_ASSETS] = Target("mk://bakumon.me/choose_assets")
        mappings[Router.Url.URL_ADD_ASSETS] = Target("mk://bakumon.me/add_assets")
        mappings[Router.Url.URL_ASSETS_DETAIL] = Target("mk://bakumon.me/assets_detail")

        Floo.configuration()
                .setDebugEnabled(BuildConfig.DEBUG)
                .addRequestInterceptor(PureSchemeInterceptor(getString(R.string.scheme)))
                .addRequestInterceptor(LogInterceptor("Request"))
                .addTargetInterceptor(PureSchemeInterceptor(getString(R.string.scheme)))
                .addTargetInterceptor(LogInterceptor("Target"))
                .addTargetNotFoundHandler(OpenDirectlyHandler())

        Floo.apply(mappings)
    }

    companion object {
        lateinit var instance: App
            private set
    }
}
