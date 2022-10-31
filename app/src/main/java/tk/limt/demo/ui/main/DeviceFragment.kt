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

package tk.limt.demo.ui.main

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import tk.limt.demo.R
import tk.limt.demo.adapter.ServiceAdapter
import tk.limt.demo.data.DeviceManager
import tk.limt.demo.databinding.ItemServiceBinding
import tk.limt.demo.databinding.RefreshRecyclerBinding
import tk.limt.demo.impl.OnTabChangeListener
import tt.tt.component.TTFragment
import tt.tt.component.TTHolder
import tt.tt.component.TTOnClickListener
import tt.tt.rx.TTObserver
import tt.tt.rx.TTSingleObserver
import tt.tt.utils.isBluetoothEnabled
import tt.tt.utils.permissionGranted
import tt.tt.utils.toast
import java.util.concurrent.TimeUnit

class DeviceFragment : TTFragment<RefreshRecyclerBinding>(),
    TTOnClickListener<ItemServiceBinding, BluetoothGattService>,
    SwipeRefreshLayout.OnRefreshListener {
    private lateinit var adapter: ServiceAdapter
    private var mnConnect: MenuItem? = null
    private val launcherPermissions = registerForActivityResult(RequestMultiplePermissions()) {}
    private val launcherBluetooth = registerForActivityResult(StartActivityForResult()) {}
    private val manager = DeviceManager.instance
    private lateinit var device: BluetoothDevice

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        device = requireArguments().getParcelable("ARG_DATA")!!
        vb.recycler.addItemDecoration(
            DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL)
        )
        adapter = ServiceAdapter(device.address)
        vb.recycler.adapter = adapter
        manager.obtain(device.address).connectionState().observeOn(
            AndroidSchedulers.mainThread()
        ).subscribe(object : TTObserver<Int>(disposables) {
            override fun onNext(t: Int) {
                updateUiWithData(t)
            }
        })
        vb.refresh.isRefreshing = true
        onRefresh()
        vb.refresh.setOnRefreshListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_device, menu)
        mnConnect = menu.findItem(R.id.connect)
        updateUiWithData(manager.obtain(device.address).connectionState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.connect -> {
                if (manager.isConnected(device.address)) {
                    manager.obtain(device.address).disconnect()
                } else {
                    vb.refresh.isRefreshing = true
                    onRefresh()
                }
                return true
            }
            R.id.close -> {
                manager.close(device.address)
                (activity as OnTabChangeListener<BluetoothDevice>).onTabChange(device, false)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRefresh() {
        if (checkPermissions() && checkBluetooth()) {
            (if (manager.obtain(device.address).isDisconnected) manager.obtain(device.address)
                .connectWithState().timeout(8, TimeUnit.SECONDS).retry(
                2
            ).filter {
                it == BluetoothProfile.STATE_CONNECTED
            }.firstOrError() else Single.just(BluetoothProfile.STATE_CONNECTED)).flatMap {
                manager.obtain(device.address).discoverServices()
            }.observeOn(
                AndroidSchedulers.mainThread()
            ).doAfterTerminate {
                vb.refresh.isRefreshing = false
            }.subscribe(object : TTSingleObserver<List<BluetoothGattService>>(disposables) {
                override fun onSuccess(t: List<BluetoothGattService>) {
                    super.onSuccess(t)
                    adapter.clear()
                    adapter.addAll(t)
                    adapter.notifyDataSetChanged()
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                    e.message?.let { ctx.toast(it) }
                }
            })
        } else vb.refresh.isRefreshing = false
    }

    override fun onClick(
        view: View,
        holder: TTHolder<ItemServiceBinding>,
        item: BluetoothGattService?
    ) {
    }

    fun updateUiWithData(state: Int) {
        when (state) {
            BluetoothProfile.STATE_CONNECTED -> {
                mnConnect?.title = ctx.getString(tt.tt.R.string.tt_disconnect)
            }
            BluetoothProfile.STATE_DISCONNECTED -> {
                mnConnect?.title = ctx.getString(tt.tt.R.string.tt_connect)
            }
        }
    }

    private fun checkPermissions(): Boolean {
        val permissions: MutableList<String> = ArrayList()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!ctx.permissionGranted(Manifest.permission.BLUETOOTH_CONNECT)) {
                permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }
        if (permissions.isNotEmpty()) {
            launcherPermissions.launch(permissions.toTypedArray())
            return false
        }
        return true
    }

    private fun checkBluetooth(): Boolean {
        if (!ctx.isBluetoothEnabled) {
            launcherBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            return false
        }
        return true
    }

    companion object {
        @JvmStatic
        fun newInstance(device: BluetoothDevice): DeviceFragment {
            return DeviceFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("ARG_DATA", device)
                }
            }
        }
    }
}