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
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import tk.limt.demo.R
import tk.limt.demo.adapter.ScanAdapter
import tk.limt.demo.data.DeviceManager
import tk.limt.demo.databinding.ItemScanBinding
import tk.limt.demo.databinding.RefreshRecyclerBinding
import tt.tt.component.TTFragment
import tt.tt.component.TTHolder
import tt.tt.component.TTOnClickListener
import tt.tt.rx.TTObserver
import tt.tt.utils.isBluetoothEnabled
import tt.tt.utils.isLocationEnabled
import tt.tt.utils.permissionGranted
import java.util.concurrent.TimeUnit

class ScanFragment : TTFragment<RefreshRecyclerBinding>(), SwipeRefreshLayout.OnRefreshListener,
    TTOnClickListener<ItemScanBinding, ScanResult>, SearchView.OnQueryTextListener {
    private val adapter: ScanAdapter = ScanAdapter(this)
    private val deviceManager = DeviceManager.instance
    private val launcherPermissions = registerForActivityResult(RequestMultiplePermissions()) {}
    private val launcherBluetooth = registerForActivityResult(StartActivityForResult()) {}
    private val launcherLocation = registerForActivityResult(StartActivityForResult()) {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        (vb.recycler.itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false
        vb.recycler.addItemDecoration(
            DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL)
        )
        vb.recycler.adapter = adapter
        vb.refresh.isRefreshing = true
        onRefresh()
        vb.refresh.setOnRefreshListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search, menu)
        val item = menu.findItem(R.id.search)
        val searchView = item.actionView as SearchView
        searchView.setOnQueryTextListener(this)
    }

    override fun onRefresh() {
        if (checkPermissions() && checkBluetooth() && checkLocation()) {
            deviceManager.scan(
                null,
                ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
            ).map { it.result }.filter { it.device.name != null }.takeUntil(
                Observable.timer(10, TimeUnit.SECONDS)
            ).observeOn(AndroidSchedulers.mainThread()).doAfterTerminate {
                vb.refresh.isRefreshing = false
            }.subscribe(object : TTObserver<ScanResult>(disposables) {
                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    adapter.clear()
                }

                override fun onNext(t: ScanResult) {
                    adapter.put(t)
                }
            })
        } else vb.refresh.isRefreshing = false
    }

    override fun onClick(v: View, h: TTHolder<ItemScanBinding>, t: ScanResult?) {
        if (v == h.vb.connect) {
            t?.let { (ctx as OnTabChangeListener<BluetoothDevice>).onTabChange(it.device, true) }
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        adapter.keyword = newText
        return true
    }

    private fun checkPermissions(): Boolean {
        val permissions: MutableList<String> = ArrayList()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!ctx.permissionGranted(Manifest.permission.BLUETOOTH_SCAN)) {
                permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (!ctx.permissionGranted(Manifest.permission.BLUETOOTH_CONNECT)) {
                permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }
        if (!ctx.permissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
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

    private fun checkLocation(): Boolean {
        if (!ctx.isLocationEnabled) {
            launcherLocation.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            return false
        }
        return true
    }

    companion object {
        @JvmStatic
        fun newInstance(): ScanFragment {
            return ScanFragment()
        }
    }
}