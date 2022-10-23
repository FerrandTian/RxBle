package tk.limt.demo.impl

/**
 * @author tianfeng
 */
interface OnTabChangeListener<T> {
    fun onTabChange(item: T, open: Boolean)
}