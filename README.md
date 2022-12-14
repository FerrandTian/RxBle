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
    implementation 'com.github.FerrandTian:RxBle:1.0.4'
    
    // or
    implementation 'com.github.FerrandTian.RxBle:rxble:1.0.4'
    implementation 'com.github.FerrandTian.RxBle:ttbase:1.0.4'
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
val ble: RxBle = RxBleManager.instance.obtain(address)
```

### Connect and discover services

```kotlin
ble.connect()   // JUST connect or reconnect

ble.connectWithState()
.timeout(8, TimeUnit.SECONDS) // Set 8 seconds timeout
.retry(2).filter { it == BluetoothProfile.STATE_CONNECTED }  // Retry twice until STATE_CONNECTED
.firstOrError().flatMap { ble.discoverServices() }  // Combining with discoverServices
.subscribe { }

ble.connectWithServices().subscribe { }
```

### Observe connection state on change

```kotlin
ble.connectionState().subscribe { }
```

### Read and write Characteristic or Descriptor

```kotlin
ble.read(characteristic).subscribe { }
ble.write(characteristic).subscribe { }
ble.read(descriptor).subscribe { }
ble.write(descriptor).subscribe { }
```

Split ByteArray before `write` when length exceeds mtu.

```kotlin
fun write(
    values: Iterable<ByteArray>, 
    characteristic: BluetoothGattCharacteristic
) { }
fun reliableWrite(
    values: Iterable<ByteArray>,
    characteristic: BluetoothGattCharacteristic
) { }
```

### Enable or disable notification

```kotlin
ble.setNotification(descriptor).subscribe(object : SingleObserver<BluetoothGattDescriptor> {
    override fun onSubscribe(d: Disposable) {
        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE // or disable value
    }
    override fun onSuccess(t: BluetoothGattDescriptor) {}
    override fun onError(e: Throwable) {}
})
```

### Observe Characteristic on change

```kotlin
ble.characteristic(uuid).subscribe { }
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
