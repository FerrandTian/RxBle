package tk.limt.demo

/**
 * @author tianfeng
 */
interface OnTabChangeListener<T> {
    fun onTabChange(item: T, open: Boolean)
}