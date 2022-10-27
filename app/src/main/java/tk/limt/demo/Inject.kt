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

package tk.limt.demo

import android.bluetooth.le.ScanResult
import android.os.Build

/**
 * @author tianfeng
 */
val ScanResult.displayName: String?
    get() {
        var name: String? = scanRecord?.deviceName
        if (name.isNullOrBlank()) name = device.name
        if (name.isNullOrBlank() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            name = device.alias
        }
        return name
    }