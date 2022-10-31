/*
 * Copyright (C) 2022 TianFeng
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:JvmName("TTContext")

package tt.tt.utils

import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.TypedValue
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import tt.tt.constant.TTMime
import java.io.File

fun Context.hasSystemFeature(featureName: String) = packageManager.hasSystemFeature(featureName)

val Context.isBluetoothEnabled: Boolean
    get() = getSystemService(BluetoothManager::class.java)?.adapter?.isEnabled == true

val Context.isLocationEnabled: Boolean
    get() = getSystemService(LocationManager::class.java)?.let {
        it.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                it.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    } ?: false

fun Context.permissionGranted(permission: String) = ActivityCompat.checkSelfPermission(
    this, permission
) == PackageManager.PERMISSION_GRANTED

fun Context.startAppDetails() = startActivity(
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    )
)

fun Context.startHome() = startActivity(
    Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
)

fun Context.startInstall(authority: String, apkFile: File) {
    val intent = Intent(Intent.ACTION_VIEW)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val apkUri = FileProvider.getUriForFile(this, authority, apkFile)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.setDataAndType(apkUri, TTMime.APK)
    } else {
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val uri = Uri.fromFile(apkFile)
        intent.setDataAndType(uri, TTMime.APK)
    }
    startActivity(intent)
}

fun Context.clearCache() {
    delete(cacheDir)
    if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) delete(externalCacheDir)
}

fun Context.dp2px(value: Float) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics
)

fun Context.sp2px(value: Float) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_SP, value, resources.displayMetrics
)

fun Context.pt2px(value: Float) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_PT, value, resources.displayMetrics
)

fun Context.in2px(value: Float) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_IN, value, resources.displayMetrics
)

fun Context.mm2px(value: Float) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_MM, value, resources.displayMetrics
)

fun Context.toast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, resId, duration).show()
}

fun Context.toast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, duration).show()
}