@file:JvmName("TTApp")

package tt.tt.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.webkit.MimeTypeMap
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import tt.tt.constant.TTMime
import java.io.File

fun installApk(context: Context, authority: String?, apkFile: File?) {
    val intent = Intent(Intent.ACTION_VIEW)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val apkUri = FileProvider.getUriForFile(context, authority!!, apkFile!!)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.setDataAndType(apkUri, TTMime.APK)
    } else {
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val uri = Uri.fromFile(apkFile)
        intent.setDataAndType(uri, TTMime.APK)
    }
    context.startActivity(intent)
}

fun startAppDetailsSettings(context: Context) = context.startActivity(
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    )
)

@SuppressLint("BatteryLife", "QueryPermissionsNeeded")
fun requestIgnoreBatteryOptimizations(context: Context) {
    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
        Uri.parse("package:${context.packageName}")
    )
    when (Build.BRAND.lowercase()) {
        "xiaomi", "redmi" -> {
            intent.component =
                ComponentName.unflattenFromString("com.miui.powerkeeper/.ui.HiddenAppsConfigActivity")
            intent.putExtra("package_name", context.packageName)
        }
        "vivo", "iqoo" -> intent.component =
            ComponentName.unflattenFromString("com.iqoo.powersaving/.PowerSavingManagerActivity")
        "oppo", "huawei", "honor" -> {
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.data = Uri.fromParts("package", context.packageName, null)
        }
    }
    try {
        val resolvers =
            context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        if (resolvers.isNotEmpty()) {
            context.startActivity(intent)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun hasGps(context: Context): Boolean =
    context.packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)

fun isLocationEnabled(context: Context): Boolean {
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val gps = manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    val network = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    return gps || network
}

fun startLocationEnable(activity: Activity, requestCode: Int) =
    activity.startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), requestCode)

@SuppressLint("MissingPermission")
fun isBluetoothEnabled(context: Context): Boolean {
    val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val adapter: BluetoothAdapter? = manager.adapter
    return adapter?.isEnabled ?: false
}

fun startBluetoothEnable(activity: Activity, requestCode: Int) =
    activity.startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), requestCode)

fun startImageChooser(activity: Activity, requestCode: Int): Unit {

    var intent = Intent()
    intent.addCategory(Intent.CATEGORY_OPENABLE)
    intent.type = TTMime.IMAGE
    if (Build.VERSION.SDK_INT < 19) {
        intent.action = Intent.ACTION_GET_CONTENT
    } else {
        intent.action = Intent.ACTION_OPEN_DOCUMENT
    }
    activity.startActivityForResult(intent, requestCode)
//    activity.startActivityForResult(
//        Intent(Intent.ACTION_PICK).setDataAndType(
//            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//            TTMime.IMAGE
//        ), requestCode
//    )
}

fun startImageCrop(activity: Activity, uri: Uri, output: Uri?, requestCode: Int) {
    val intent = Intent("com.android.camera.action.CROP")
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    intent.setDataAndType(uri, TTMime.IMAGE)
    intent.putExtra("crop", "true")
    intent.putExtra("aspectX", 1)
    intent.putExtra("aspectY", 1)
    intent.putExtra("outputX", 150)
    intent.putExtra("outputY", 150)
    intent.putExtra("noFaceDetection", true)
    if (output != null) {
        intent.putExtra("return-data", false)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, output)
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val resolveInfos = activity.packageManager.queryIntentActivities(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
            for (info in resolveInfos) {
                activity.grantUriPermission(
                    info.activityInfo.packageName,
                    output,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            }
        }
    } else {
        intent.putExtra("return-data", true)
    }
    activity.startActivityForResult(intent, requestCode)
}

fun startSend(activity: Activity, authority: String?, file: File?, title: String?) {
    var intent = Intent(Intent.ACTION_SEND)
    var uri = Uri.fromFile(file)
    val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
    val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        uri = FileProvider.getUriForFile(activity, authority!!, file!!)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    intent.type = mimeType
    intent.putExtra(Intent.EXTRA_STREAM, uri)
    intent = Intent.createChooser(intent, title)
    activity.startActivity(intent)
}

fun startFileContent(activity: Activity, type: String?, requestCode: Int) {
    val intent = Intent(Intent.ACTION_GET_CONTENT)
    intent.type = type
    intent.addCategory(Intent.CATEGORY_OPENABLE)
    activity.startActivityForResult(intent, requestCode)
}

fun startHome(context: Context) =
    context.startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME))

fun startWifiSettings(context: Context) =
    context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))

fun restart(context: Context, cls: Class<*>?) {
    val starter = Intent(context, cls)
    starter.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    context.startActivity(starter)
}