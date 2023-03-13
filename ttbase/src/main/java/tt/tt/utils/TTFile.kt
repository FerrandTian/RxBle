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

@file:JvmName("TTFile")

package tt.tt.utils

import android.graphics.Bitmap
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import java.lang.StringBuilder
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * @author tianfeng
 */
private const val BUF_SIZE = 8192

fun File.isNotEmpty(): Boolean = exists() && isFile && length() > 0

fun File.append(line: CharSequence) {
    OutputStreamWriter(FileOutputStream(this, true)).apply {
        append(line)
        append("\r\n")
        flush()
        close()
    }
}

fun File.append(lines: Iterable<CharSequence>) {
    OutputStreamWriter(FileOutputStream(this, true)).apply {
        lines.forEach {
            append(it)
        }
        append("\r\n")
        flush()
        close()
    }
}

fun mkdir(dir: File): Boolean = if (!dir.exists()) dir.mkdirs() else true

fun create(file: File): Boolean {
    if (!file.exists()) {
        try {
            val parent = file.parentFile
            var parentCreated = true
            if (parent != null && !parent.exists()) {
                parentCreated = parent.mkdirs()
            }
            if (parentCreated) {
                return file.createNewFile()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }
    return true
}

fun bytes(file: File): ByteArray? {
    var input: RandomAccessFile? = null
    var bytes: ByteArray? = null
    try {
        input = RandomAccessFile(file, "r")
        bytes = ByteArray(input.length().toInt())
        input.readFully(bytes)
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        close(input)
    }
    return bytes
}

fun string(file: File): String {
    val builder = StringBuilder()
    var input: FileInputStream? = null
    var reader: BufferedReader? = null
    try {
        input = FileInputStream(file)
        reader = BufferedReader(InputStreamReader(input))
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            builder.append(line)
            builder.append("\r\n")
        }
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        close(reader)
        close(input)
    }
    return builder.toString()
}

fun copy(src: File, dest: File): Boolean {
    var input: InputStream? = null
    var output: OutputStream? = null
    try {
        input = FileInputStream(src)
        create(dest)
        output = FileOutputStream(dest)
        val buffer = ByteArray(BUF_SIZE)
        var len: Int
        while (input.read(buffer).also { len = it } != -1) {
            output.write(buffer, 0, len)
        }
        output.flush()
        close(output)
        close(input)
        return true
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        close(output)
        close(input)
    }
    return false
}

fun zip(dest: File, vararg filesOrDirs: File): Boolean {
    var output: ZipOutputStream? = null
    try {
        create(dest)
        output = ZipOutputStream(FileOutputStream(dest))
        for (f in filesOrDirs) zip(output, f, f.name)
        output.flush()
        close(output)
        return true
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        close(output)
    }
    return false
}

fun zip(dest: File, method: Int, level: Int, comment: String?, vararg filesOrDirs: File): Boolean {
    var output: ZipOutputStream? = null
    try {
        create(dest)
        output = ZipOutputStream(FileOutputStream(dest))
        output.setMethod(method)
        output.setLevel(level)
        output.setComment(comment)
        for (f in filesOrDirs) {
            zip(output, f, f.name)
        }
        output.flush()
        close(output)
        return true
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        close(output)
    }
    return false
}

@Throws(IOException::class)
private fun zip(output: ZipOutputStream, fileOrDir: File, name: String) {
    if (fileOrDir.isDirectory) {
        val filesOrDirs = fileOrDir.listFiles()
        if (filesOrDirs != null) {
            if (filesOrDirs.isEmpty()) {
                output.putNextEntry(ZipEntry(name + File.separator))
            } else {
                for (f in filesOrDirs) {
                    zip(output, f, name + File.separator + f.name)
                }
            }
        }
    } else {
        output.putNextEntry(ZipEntry(name))
        val input = BufferedInputStream(FileInputStream(fileOrDir))
        val buffer = ByteArray(BUF_SIZE)
        var len: Int
        while (input.read(buffer).also { len = it } != -1) {
            output.write(buffer, 0, len)
        }
        close(input)
    }
}

fun unzip(src: File, dir: File): Boolean {
    if (!src.exists() || src.length() <= 0L) return false
    var zip: ZipInputStream? = null
    var input: BufferedInputStream? = null
    var output: BufferedOutputStream? = null
    try {
        val zipFile = ZipFile(src)
        zip = ZipInputStream(FileInputStream(src))
        var entry: ZipEntry? = null
        while (zip.nextEntry.also { entry = it } != null) {
            val file = File(dir, entry!!.name)
            if (src == file || !file.canonicalPath.startsWith(dir.canonicalPath)) {
                continue
            }
            if (entry!!.isDirectory) {
                mkdir(file)
            } else {
                create(file)
                input = BufferedInputStream(zipFile.getInputStream(entry))
                output = BufferedOutputStream(FileOutputStream(file))
                val buffer = ByteArray(BUF_SIZE)
                var len: Int
                while (input.read(buffer).also { len = it } != -1) {
                    output.write(buffer, 0, len)
                }
                output.flush()
                close(output)
                close(input)
            }
        }
        close(zip)
        return true
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        close(output)
        close(input)
        close(zip)
    }
    return false
}

fun write(dest: File, input: InputStream): ObservableOnSubscribe<Long> {
    return ObservableOnSubscribe { emitter ->
        var length: Long = 0
        var lastMs = System.currentTimeMillis()
        var currentMs = lastMs
        emitter.onNext(length)
        var output: OutputStream? = null
        try {
            create(dest)
            output = BufferedOutputStream(FileOutputStream(dest))
            val bytes = ByteArray(BUF_SIZE)
            var len: Int
            while (input.read(bytes, 0, BUF_SIZE).also { len = it } != -1) {
                output.write(bytes, 0, len)
                length += len.toLong()
                currentMs = System.currentTimeMillis()
                if (currentMs - lastMs > 32) {
                    emitter.onNext(length)
                    lastMs = currentMs
                }
            }
            output.flush()
            close(output)
            close(input)
            if (currentMs != lastMs) {
                emitter.onNext(length)
            }
            emitter.onComplete()
        } catch (e: IOException) {
            e.printStackTrace()
            emitter.onError(e)
        } finally {
            close(output)
            close(input)
        }
    }
}

fun save(dest: File, input: InputStream): Boolean {
    var output: OutputStream? = null
    try {
        create(dest)
        output = FileOutputStream(dest)
        val buffer = ByteArray(BUF_SIZE)
        var len: Int
        while (input.read(buffer).also { len = it } != -1) {
            output.write(buffer, 0, len)
        }
        output.flush()
        close(output)
        close(input)
        return true
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        close(output)
        close(input)
    }
    return false
}

fun save(dest: File, bytes: ByteArray, append: Boolean): Boolean {
    var output: OutputStream? = null
    try {
        create(dest)
        output = FileOutputStream(dest, append)
        output.write(bytes, 0, bytes.size)
        output.flush()
        close(output)
        return true
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        close(output)
    }
    return false
}

fun save(dest: File, list: Iterable<ByteArray>, append: Boolean): Boolean {
    var output: OutputStream? = null
    try {
        create(dest)
        output = FileOutputStream(dest, append)
        list.forEach {
            output.write(it, 0, it.size)
        }
        output.flush()
        close(output)
        return true
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        close(output)
    }
    return false
}

fun save(dest: File, bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int): Boolean {
    var output: OutputStream? = null
    try {
        create(dest)
        output = BufferedOutputStream(FileOutputStream(dest))
        bitmap.compress(format, quality, output)
        output.flush()
        close(output)
        return true
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        close(output)
    }
    return false
}

fun delete(dirOrFile: File?): Boolean {
    if (dirOrFile != null) if (dirOrFile.isDirectory) {
        val children = dirOrFile.list()
        if (children != null)
            for (child in children) if (!delete(File(dirOrFile, child))) return false
    } else return dirOrFile.delete()
    return true
}

fun close(closeable: Closeable?) {
    try {
        closeable?.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}