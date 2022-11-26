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

package tt.tt.rx

import io.reactivex.rxjava3.disposables.Disposable

/**
 * @author tianfeng
 */
class TTDisposables {
    private val set: MutableSet<Disposable> = HashSet()

    fun add(d: Disposable) = set.add(d)

    fun dispose(d: Disposable?) = set.remove(d).also {
        d?.dispose()
    }

    fun disposeAll() {
        set.removeAll {
            it.dispose()
            true
        }
    }
}