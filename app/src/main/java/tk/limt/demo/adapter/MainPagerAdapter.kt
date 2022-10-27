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

package tk.limt.demo.adapter

import android.bluetooth.BluetoothDevice
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import tk.limt.demo.ui.main.DeviceFragment
import tk.limt.demo.ui.main.ScanFragment

class MainPagerAdapter(
    context: FragmentActivity,
    val items: MutableList<BluetoothDevice> = ArrayList()
) : FragmentStateAdapter(context), MutableList<BluetoothDevice> by items {

    override fun getItemCount(): Int {
        return items.size + 1
    }

    override fun createFragment(position: Int): Fragment {
        return if (position == 0) ScanFragment.newInstance() else DeviceFragment.newInstance(items[position - 1])
    }
}