package tt.tt.component

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import tt.tt.rx.TTDisposables
import java.lang.reflect.ParameterizedType

/**
 * @author tianfeng
 */
abstract class TTActivity<VB : ViewBinding> : AppCompatActivity() {
    @JvmField
    val log = TTLog(this.javaClass.simpleName)
    val disposables = TTDisposables()
    val context: AppCompatActivity
        get() = this
    protected lateinit var vmp: ViewModelProvider
    protected lateinit var vb: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vmp = ViewModelProvider(this)
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

    @JvmOverloads
    fun toast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, resId, duration).show()
    }

    @JvmOverloads
    fun toast(text: String?, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, text, duration).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.disposeAll()
    }
}