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

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import tt.tt.rx.TTDisposables
import java.lang.reflect.ParameterizedType

/**
 * @author tianfeng
 */
abstract class TTFragment<B : ViewBinding> : Fragment() {
    @JvmField
    val log = TTLog(this.javaClass.simpleName)

    @JvmField
    val disposables = TTDisposables()

    @get:JvmName("ctx")
    val ctx: FragmentActivity
        get() = requireActivity()
    lateinit var vmp: ViewModelProvider
    lateinit var vb: B

    override fun onAttach(context: Context) {
        super.onAttach(context)
        vmp = ViewModelProvider(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        try {
            val type = javaClass.genericSuperclass as ParameterizedType
            val clazzVB = type.actualTypeArguments[0] as Class<B>
            val inflate = clazzVB.getDeclaredMethod(
                "inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java
            )
            vb = inflate.invoke(null, inflater, container, false) as B
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return vb.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposables.disposeAll()
    }
}