package cc.chenhe.lib.wearmsger

import android.app.Application

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        WM.init(this, WM.Mode.AUTO)
    }
}