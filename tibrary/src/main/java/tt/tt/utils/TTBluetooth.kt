@file:JvmName("TTBluetooth")

package tt.tt.utils

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService

fun getNotifyService(gatt: BluetoothGatt): BluetoothGattService? {
    if (gatt.services.isNotEmpty()) {
        for (service in gatt.services) {
            if (service.type == BluetoothGattService.SERVICE_TYPE_PRIMARY) {
                for (ch in service.characteristics) {
                    if (ch.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
                        return service
                    }
                }
            }
        }
    }
    return null
}

fun getCharacteristic(service: BluetoothGattService, properties: Int): BluetoothGattCharacteristic? {
    for (ch in service.characteristics) if (ch.properties and properties > 0) return ch
    return null
}

fun setCharacteristicNotification(
    gatt: BluetoothGatt,
    ch: BluetoothGattCharacteristic,
    enabled: Boolean
): Boolean {
    var result = false
    try {
        result = gatt.setCharacteristicNotification(ch, enabled)
        if (result && ch.descriptors.isNotEmpty()) {
            ch.descriptors[0].value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            result = gatt.writeDescriptor(ch.descriptors[0])
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return result
}

fun refresh(gatt: BluetoothGatt): Boolean {
    try {
        val refresh = BluetoothGatt::class.java.getMethod("refresh")
        val result = refresh.invoke(gatt)
        return result != null && result as Boolean
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return false
}

fun close(gatt: BluetoothGatt?) {
    try {
        gatt?.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}