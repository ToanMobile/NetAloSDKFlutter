package com.netacom.flutter_sdk

import android.content.Context
import androidx.work.Configuration
import com.netacom.full.ui.sdk.NetAloSDK
import com.netacom.full.ui.sdk.NetAloSdkCore
import com.netacom.full.ui.sdk.SdkCore
import com.netacom.lite.BuildConfig
import com.netacom.lite.entity.ui.theme.NeTheme
import com.netacom.lite.sdk.SdkConfig
import dagger.hilt.android.HiltAndroidApp
import io.flutter.app.FlutterApplication
import io.realm.Realm
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : FlutterApplication(), Configuration.Provider {


    @Inject
    lateinit var netAloSdkCore: NetAloSdkCore

    override fun getWorkManagerConfiguration() =
            Configuration.Builder()
                    .setWorkerFactory(netAloSdkCore.workerFactory)
                    .build()

    private val sdkConfig = SdkConfig(
            appId = 2,
            appKey = "lomokey",
            accountKey = "adminkey",
            isSyncContact = false,
            hidePhone = true,
            hideCreateGroup = true,
            hideAddInfo = true,
            hideInfo = true,
            classMainActivity = MainActivity::class.java.name
    )

    private val sdkTheme = NeTheme(
            mainColor = "#9c5aff",
            subColor = "#9c5aff",
            toolbarDrawable = "#9c5aff"
    )

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        Realm.init(this)
    }

    override fun onCreate() {
        super.onCreate()
        NetAloSDK.initNetAloSDK(
                context = this,
                netAloSdkCore = netAloSdkCore,
                sdkConfig = sdkConfig,
                neTheme = sdkTheme
        )
    }
}