package tt.tt.component

import android.util.Log

/**
 * @author tianfeng
 */
class TTLog(val tag: String = "TTLog") {
    var debug = false

    /**
     * 封装Log.v()
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     * the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    fun v(tag: String?, msg: String) {
        if (debug) Log.v(tag, msg)
    }

    /**
     * 封装Log.v()，使用默认的TAG
     *
     * @param msg The message you would like logged.
     */
    fun v(msg: String) {
        if (debug) Log.v(tag, msg)
    }

    /**
     * 封装Log.d()
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     * the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    fun d(tag: String?, msg: String) {
        if (debug) Log.d(tag, msg)
    }

    /**
     * 封装Log.d()，使用默认的TAG
     *
     * @param msg The message you would like logged.
     */
    fun d(msg: String) {
        if (debug) Log.d(tag, msg)
    }

    /**
     * 封装Log.i()
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     * the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    fun i(tag: String?, msg: String) {
        if (debug) Log.i(tag, msg)
    }

    /**
     * 封装Log.i()，使用默认的TAG
     *
     * @param msg The message you would like logged.
     */
    fun i(msg: String) {
        if (debug) Log.i(tag, msg)
    }

    /**
     * 封装Log.w()
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     * the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    fun w(tag: String?, msg: String) {
        if (debug) Log.w(tag, msg)
    }

    /**
     * 封装Log.w()，使用默认的TAG
     *
     * @param msg The message you would like logged.
     */
    fun w(msg: String) {
        if (debug) Log.w(tag, msg)
    }

    /**
     * 封装Log.e()
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     * the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    fun e(tag: String?, msg: String) {
        if (debug) Log.e(tag, msg)
    }

    /**
     * 封装Log.e()，使用默认的TAG
     *
     * @param msg The message you would like logged.
     */
    fun e(msg: String) {
        if (debug) Log.e(tag, msg)
    }

    /**
     * Send a [Log.ERROR] log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     * the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param t  An exception to log
     */
    fun e(tag: String?, msg: String?, t: Throwable?) {
        if (debug) Log.e(tag, msg, t)
    }

    /**
     * 封装System.out.print()
     *
     * @param msg the string to print to the target stream.
     */
    fun print(msg: Any?) {
        if (debug) kotlin.io.print(msg)
    }

    /**
     * 封装System.out.println()
     *
     * @param msg the string to print to the target stream.
     */
    fun println(msg: Any?) {
        if (debug) kotlin.io.println(msg)
    }
}