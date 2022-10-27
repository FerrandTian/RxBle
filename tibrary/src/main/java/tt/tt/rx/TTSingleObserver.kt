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

import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable

/**
 * @author tianfeng
 */
abstract class TTSingleObserver<T>(var set: TTDisposables? = null) : SingleObserver<T> {
    var disposable: Disposable? = null

    override fun onSubscribe(d: Disposable) {
        disposable = d
        set?.add(d)
    }

    override fun onSuccess(t: T) {
        dispose()
    }

    override fun onError(e: Throwable) {
        e.printStackTrace()
        dispose()
    }

    fun dispose() {
        set?.dispose(disposable)
        disposable?.dispose()
        disposable = null
    }
}