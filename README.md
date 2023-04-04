# RxBle: Use Android Bluetooth API in Rx way

[![](https://jitpack.io/v/FerrandTian/RxBle.svg)](https://jitpack.io/#FerrandTian/RxBle)

A lightweight encapsulation of Android Bluetooth API.

 * Use Android Bluetooth API in Rx way.
 * Support multiple Bluetooth device connection.
 * Async error handling.

### Preview

![Preview_1](https://github.com/FerrandTian/RxBle/raw/main/Screenshot_1.jpg)
![Preview_2](https://github.com/FerrandTian/RxBle/raw/main/Screenshot_2.jpg)

### Gradle settings

If you don't have this already, add it to your **root** build.gradle file:
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

Then you can add the dependency to your **app** build.gradle file:
```
dependencies {
    ...
    implementation 'com.github.FerrandTian:RxBle:1.0.6'
    
    // Or
    implementation 'com.github.FerrandTian.RxBle:rxble:1.0.6'
    
    // Optional
    implementation 'com.github.FerrandTian.RxBle:ttbase:1.0.6'
}
```

### Init

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        RxBleManager.init(this)
    }
}
```

### Scan

```kotlin
RxBleManager.instance.scan(
    null,
    ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
)   // Original scan parameters are supported
.filter { it.device.name != null }  // Use Rx filter function
.takeUntil(Observable.timer(10, TimeUnit.SECONDS)) // Unsubscribe after 10 seconds
.observeOn(AndroidSchedulers.mainThread())
.subscribe { }
```

### Create

```kotlin
val ble: RxBle = RxBleManager.instance.create(address)
```

### Connect and discover services

```kotlin
ble.connect()   // JUST connect or reconnect

ble.connectWithState()
.timeout(8, TimeUnit.SECONDS)   // Set 8 seconds timeout
.retry(2).lastOrError()         // Retry twice until STATE_CONNECTED
.flatMap { ble.discoverServices() }  // Combining with discoverServices
.subscribe { }

ble.connectWithServices().subscribe { }
```

### Observe connection state on change

```kotlin
ble.connectionState().subscribe { }
```

### Read and write Characteristic or Descriptor

This method is thread-safe. Use this method to execute write operation sequentially.

```kotlin
fun writeWithQueue(
    characteristic: BluetoothGattCharacteristic,
    value: ByteArray,
    writeType: Int = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT,
)
fun writeWithQueue(
    descriptor: BluetoothGattDescriptor,
    value: ByteArray,
)
```

NOT thread-safe.

```kotlin
ble.read(characteristic).subscribe { }
ble.write(
    characteristic, bytes, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
).subscribe { }
ble.reliableWrite(iterator).subscribe { }

ble.read(descriptor).subscribe { }
ble.write(descriptor, bytes).subscribe { }
```

### Enable or disable notification

```kotlin
ble.setNotification(
    descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE   // or disable value
).subscribe { }
```

### Observe Characteristic on change

```kotlin
ble.characteristic(uuid).subscribe { }
```

### Disconnect or close connection

```kotlin
ble.disconnectWithState().subscribe { }     // Call connect() to reconnect
ble.disconnect()     // Call connect() to reconnect

ble.close()     // Dispose all resources and close this Bluetooth GATT client
```

## LICENSE

    Copyright (C) 2022 TianFeng
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
    http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
