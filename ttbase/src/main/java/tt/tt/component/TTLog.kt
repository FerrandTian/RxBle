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

package tt.tt.component

import android.util.Log

/**
 * @author tianfeng
 */
class TTLog(@JvmField var tag: String = "TTLog") {
    @JvmField
    var debug = false

    /**
     * Send a {@link android.util.Log#VERBOSE} log message.
     *
     * @param msg The message you would like logged.
     * @return The number of bytes written.
     */
    fun v(msg: String) = v(tag, msg)

    /**
     * Send a {@link android.util.Log#VERBOSE} log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     * the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @return The number of bytes written.
     */
    fun v(tag: String, msg: String) = if (debug) Log.v(tag, msg) else 0

    /**
     * Send a {@link android.util.Log#VERBOSE} log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param tr An exception to log
     * @return The number of bytes written.
     */
    fun v(msg: String, tr: Throwable) = v(tag, msg, tr)

    /**
     * Send a {@link android.util.Log#VERBOSE} log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     * the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     * @return The number of bytes written.
     */
    fun v(tag: String, msg: String, tr: Throwable) = if (debug) Log.v(tag, msg, tr) else 0

    /**
     * Send a {@link android.util.Log#DEBUG} log message.
     *
     * @param msg The message you would like logged.
     * @return The number of bytes written.
     */
    fun d(msg: String) = d(tag, msg)

    /**
     * Send a {@link android.util.Log#DEBUG} log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     * the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @return The number of bytes written.
     */
    fun d(tag: String, msg: String) = if (debug) Log.d(tag, msg) else 0

    /**
     * Send a {@link android.util.Log#DEBUG} log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param tr An exception to log
     * @return The number of bytes written.
     */
    fun d(msg: String, tr: Throwable) = d(tag, msg, tr)

    /**
     * Send a {@link android.util.Log#DEBUG} log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     * the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     * @return The number of bytes written.
     */
    fun d(tag: String, msg: String, tr: Throwable) = if (debug) Log.d(tag, msg, tr) else 0

    /**
     * Send a {@link android.util.Log#INFO} log message.
     *
     * @param msg The message you would like logged.
     * @return The number of bytes written.
     */
    fun i(msg: String) = i(tag, msg)

    /**
     * Send a {@link android.util.Log#INFO} log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     * the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @return The number of bytes written.
     */
    fun i(tag: String, msg: String) = if (debug) Log.i(tag, msg) else 0

    /**
     * Send a {@link android.util.Log#INFO} log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param tr An exception to log
     * @return The number of bytes written.
     */
    fun i(msg: String, tr: Throwable) = i(tag, msg, tr)

    /**
     * Send a {@link android.util.Log#INFO} log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     * the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     * @return The number of bytes written.
     */
    fun i(tag: String, msg: String, tr: Throwable) = if (debug) Log.i(tag, msg, tr) else 0

    /**
     * Send a {@link android.util.Log#WARN} log message.
     *
     * @param msg The message you would like logged.
     * @return The number of bytes written.
     */
    fun w(msg: String) = w(tag, msg)

    /**
     * Send a {@link android.util.Log#WARN} log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     * the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @return The number of bytes written.
     */
    fun w(tag: String, msg: String) = if (debug) Log.w(tag, msg) else 0

    /**
     * Send a {@link android.util.Log#WARN} log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param tr An exception to log
     * @return The number of bytes written.
     */
    fun w(tr: Throwable) = w(null, tr)

    /**
     * Send a {@link android.util.Log#WARN} log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param tr An exception to log
     * @return The number of bytes written.
     */
    fun w(msg: String?, tr: Throwable) = w(tag, msg, tr)

    /**
     * Send a {@link android.util.Log#WARN} log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     * the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     * @return The number of bytes written.
     */
    fun w(tag: String, msg: String?, tr: Throwable) = if (debug) {
        if (msg != null) Log.w(
            tag, msg, tr
        ) else Log.w(tag, tr)
    } else 0

    /**
     * Send a {@link android.util.Log#ERROR} log message.
     *
     * @param msg The message you would like logged.
     * @return The number of bytes written.
     */
    fun e(msg: String) = e(tag, msg)

    /**
     * Send a {@link android.util.Log#ERROR} log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     * the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @return The number of bytes written.
     */
    fun e(tag: String, msg: String) = if (debug) Log.e(tag, msg) else 0

    /**
     * Send a {@link android.util.Log#ERROR} log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param tr An exception to log
     * @return The number of bytes written.
     */
    fun e(msg: String, tr: Throwable) = e(tag, msg, tr)

    /**
     * Send a {@link android.util.Log#ERROR} log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     * the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     * @return The number of bytes written.
     */
    fun e(tag: String, msg: String, tr: Throwable) = if (debug) Log.e(tag, msg, tr) else 0

    /**
     * Prints the given [msg] to the standard output stream.
     *
     * @param msg the string to print to the target stream.
     */
    fun print(msg: Any?) {
        if (debug) System.out.print(msg)
    }

    /**
     * Prints the given [msg] to the standard output stream.
     *
     * @param msg the string to print to the target stream.
     */
    fun println(msg: Any?) {
        if (debug) kotlin.io.println(msg)
    }
}