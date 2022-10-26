/*
 * Copyright (c) 2022-present, TianFeng.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package tt.tt.component

import android.app.Service
import android.content.Context
import tt.tt.rx.TTDisposables

/**
 * @author tianfeng
 */
abstract class TTService : Service() {
    val log = TTLog(this.javaClass.simpleName)
    val ctx: Context
        get() = this
    val disposables = TTDisposables()

    override fun onDestroy() {
        super.onDestroy()
        disposables.disposeAll()
    }
}