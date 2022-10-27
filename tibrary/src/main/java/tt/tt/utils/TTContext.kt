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
import android.content.pm.PackageManager
import android.location.LocationManager
import android.util.TypedValue
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat

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