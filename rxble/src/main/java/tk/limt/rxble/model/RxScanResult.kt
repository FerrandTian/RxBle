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

package tk.limt.rxble.model

open class RxScanResult {

    class ScanResult(
        /**
         * Determines how this callback was triggered. Could be one of {@link
         * ScanSettings#CALLBACK_TYPE_ALL_MATCHES}, {@link ScanSettings#CALLBACK_TYPE_FIRST_MATCH} or
         * {@link ScanSettings#CALLBACK_TYPE_MATCH_LOST}
         */
        val callbackType: Int,

        /**
         * A Bluetooth LE scan result.
         */
        val result: android.bluetooth.le.ScanResult
    ) : RxScanResult()

    class ScanResults(val results: List<android.bluetooth.le.ScanResult>) : RxScanResult()

    companion object {
        fun create(callbackType: Int, result: android.bluetooth.le.ScanResult): RxScanResult =
            ScanResult(callbackType, result)

        fun create(results: List<android.bluetooth.le.ScanResult>): RxScanResult =
            ScanResults(results)
    }
}