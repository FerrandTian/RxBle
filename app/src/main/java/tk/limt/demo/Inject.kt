package tk.limt.demo

import android.bluetooth.le.ScanResult
import android.os.Build

/**
 * @author tianfeng
 */
val ScanResult.displayName: String?
    get() {
        var name: String? = scanRecord?.deviceName
        if (name.isNullOrBlank()) name = device.name
        if (name.isNullOrBlank() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            name = device.alias
        }
        return name
    }