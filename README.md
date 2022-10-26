# RxBle: Use Android Bluetooth API in Rx way

A lightweight encapsulation of Android Bluetooth API.

 * Use Android Bluetooth API in Rx way.
 * Support multiple Bluetooth device connection.
 * Async error handling.

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
ble.connectWithState()
.timeout(8, TimeUnit.SECONDS) // Set 8 seconds timeout
.retry(2).filter { it == BluetoothProfile.STATE_CONNECTED }  // Retry twice until STATE_CONNECTED
.firstOrError().flatMap { ble.discoverServices() }  // Combining with discoverServices
.subscribe { }
```

### Observe connection state on change 
```kotlin
ble.connectionState().subscribe { }
```

### Read and write Characteristic or Descriptor
```kotlin
ble.read(characteristic).subscribe { }
ble.write(characteristic).subscribe { }
```
Split ByteArray before `write` when length exceeds mtu.
```kotlin
fun write(
    values: Iterable<ByteArray>, 
    characteristic: BluetoothGattCharacteristic
)
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

## LICENSE

    Copyright (c) 2022-present, TianFeng.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
