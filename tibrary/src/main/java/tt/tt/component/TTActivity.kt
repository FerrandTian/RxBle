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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import tt.tt.rx.TTDisposables
import java.lang.reflect.ParameterizedType

/**
 * @author tianfeng
 */
abstract class TTActivity<B : ViewBinding> : AppCompatActivity() {
    @JvmField
    val log = TTLog(this.javaClass.simpleName)

    @JvmField
    val disposables = TTDisposables()

    @JvmField
    val ctx  = this
    lateinit var vmp: ViewModelProvider
    lateinit var vb: B

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vmp = ViewModelProvider(this)
        try {
            val type = javaClass.genericSuperclass as ParameterizedType
            val clazzVB = type.actualTypeArguments[0] as Class<B>
            val inflate = clazzVB.getDeclaredMethod("inflate", LayoutInflater::class.java)
            vb = inflate.invoke(null, layoutInflater) as B
            setContentView(vb.root)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.disposeAll()
    }
}