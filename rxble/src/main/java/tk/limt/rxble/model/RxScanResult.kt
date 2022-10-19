package tk.limt.rxble.model

internal open class RxScanResult {

    class ScanResult(val callbackType: Int, val result: android.bluetooth.le.ScanResult) :
        RxScanResult()

    class ScanResults(val results: List<android.bluetooth.le.ScanResult>) : RxScanResult()

    companion object {
        fun create(callbackType: Int, result: android.bluetooth.le.ScanResult): RxScanResult =
            ScanResult(callbackType, result)

        fun create(results: List<android.bluetooth.le.ScanResult>): RxScanResult =
            ScanResults(results)
    }
}