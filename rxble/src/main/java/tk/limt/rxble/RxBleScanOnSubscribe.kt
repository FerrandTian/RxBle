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

package tk.limt.rxble

import android.bluetooth.le.*
import tk.limt.rxble.model.RxScanResult
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import tk.limt.rxble.model.ScanFailedException
import java.lang.Exception

internal class RxBleScanOnSubscribe(
    private val scanner: BluetoothLeScanner,
    private val filters: List<ScanFilter>? = null,
    private val settings: ScanSettings? = ScanSettings.Builder().build()
) : ObservableOnSubscribe<RxScanResult> {
    override fun subscribe(emitter: ObservableEmitter<RxScanResult>) {
        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                emitter.onNext(RxScanResult.create(callbackType, result))
            }

            override fun onBatchScanResults(results: List<ScanResult>) {
                emitter.onNext(RxScanResult.create(results))
            }

            override fun onScanFailed(errorCode: Int) {
                emitter.onError(
                    ScanFailedException(
                        errorCode,
                        "Scan failed, error code: $errorCode"
                    )
                )
            }
        }
        scanner.startScan(filters, settings, callback)
        emitter.setCancellable {
            try {
                scanner.stopScan(callback)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}