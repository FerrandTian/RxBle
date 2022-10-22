package tk.limt.utils

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor

fun ByteArray.hex() = if (this.isEmpty()) "" else {
    val buf = StringBuilder(size * 2)
    for (b in this) buf.append(String.format("%02x", b.toInt() and 0xff))
    buf.toString()
}

fun ByteArray.string() = this.decodeToString().trimEnd(Char.MIN_VALUE)

fun BluetoothGattCharacteristic.support(property: Int) = property and properties != 0

val BluetoothGattCharacteristic.supportNotification: Boolean
    get() = (support(BluetoothGattCharacteristic.PROPERTY_NOTIFY) || support(
        BluetoothGattCharacteristic.PROPERTY_INDICATE
    )) && descriptors.isNotEmpty()

val BluetoothGattCharacteristic.notificationEnabled: Boolean
    get() = supportNotification && descriptors[0].value != null && !BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE.contentEquals(
        descriptors[0].value
    )