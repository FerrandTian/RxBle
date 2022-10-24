package tt.tt.component

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import tt.tt.rx.TTDisposables
import java.lang.reflect.ParameterizedType

/**
 * @author tianfeng
 */
abstract class TTActivity<VB : ViewBinding> : AppCompatActivity() {
    val log = TTLog(this.javaClass.simpleName)
    val disposables = TTDisposables()
    val ctx: AppCompatActivity
        get() = this
    lateinit var vb: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            val type = javaClass.genericSuperclass as ParameterizedType
            val clazzVB = type.actualTypeArguments[0] as Class<VB>
            val inflate = clazzVB.getDeclaredMethod("inflate", LayoutInflater::class.java)
            vb = inflate.invoke(null, layoutInflater) as VB
            setContentView(vb.root)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
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