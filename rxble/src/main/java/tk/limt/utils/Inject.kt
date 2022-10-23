package tk.limt.utils

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor

fun BluetoothGattCharacteristic.support(property: Int) = property and properties != 0

val BluetoothGattCharacteristic.supportNotification: Boolean
    get() = (support(BluetoothGattCharacteristic.PROPERTY_NOTIFY) || support(
        BluetoothGattCharacteristic.PROPERTY_INDICATE
    )) && descriptors.isNotEmpty()

val BluetoothGattCharacteristic.notificationEnabled: Boolean
    get() = supportNotification && descriptors[0].value != null && !BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE.contentEquals(
        descriptors[0].value
    )