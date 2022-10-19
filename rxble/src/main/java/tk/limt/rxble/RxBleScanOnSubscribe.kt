package tk.limt.rxble

import android.bluetooth.le.*
import tk.limt.rxble.model.RxScanResult
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import java.lang.Exception

internal class RxBleScanOnSubscribe(
    private val scanner: BluetoothLeScanner,
    private val filters: List<ScanFilter>? = null,
    private val settings: ScanSettings? = ScanSettings.Builder().build()
) : ObservableOnSubscribe<RxScanResult> {
    override fun subscribe(emitter: ObservableEmitter<RxScanResult>) {
        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                emitter.onNext(RxScanResult.create(callbackType, result))
            }

            override fun onBatchScanResults(results: List<ScanResult>) {
                emitter.onNext(RxScanResult.create(results))
            }

            override fun onScanFailed(errorCode: Int) {
                emitter.onError(Throwable("Scan failed, error code: $errorCode"))
            }
        }
        scanner.startScan(filters, settings, callback)
        emitter.setCancellable {
            try {
                scanner.stopScan(callback)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}