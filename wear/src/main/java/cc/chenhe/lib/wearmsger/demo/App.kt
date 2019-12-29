package cc.chenhe.lib.wearmsger.demo

import android.app.Application
import cc.chenhe.lib.wearmsger.WM

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        WM.init(
            this,
            WM.Mode.AUTO
        )
    }
}