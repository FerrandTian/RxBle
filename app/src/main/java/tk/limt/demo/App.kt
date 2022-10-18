package tk.limt.demo

import android.app.Application
import tk.limt.rxble.RxBleManager

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        RxBleManager.instance.init(this)
    }
}