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

package tt.tt.component

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import tt.tt.rx.TTDisposables

/**
 * @author tianfeng
 */
class TTHolder<VB : ViewBinding>(val vb: VB) : RecyclerView.ViewHolder(vb.root) {
    val disposables = TTDisposables()

    companion object {
        fun type(clazz: Class<out ViewBinding>?): Int {
            return clazz?.hashCode() ?: RecyclerView.INVALID_TYPE
        }
    }
}