package org.baswell.nvdmirror

import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.wait
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.GZIPInputStream

val client: OkHttpClient = OkHttpClient.Builder().connectionPool(ConnectionPool()).build()
const val SleepBufferMSecs = 2000L

fun main() {

    while (true) {
        if (downloadTextFile("https://nvd.nist.gov/feeds/json/cve/1.1/nvdcve-1.1-modified.meta")) {
            break
        } else {
            Thread.sleep(SleepBufferMSecs)
        }
    }

    while (true) {
        if (downloadTextFile("https://nvd.nist.gov/feeds/json/cve/1.1/nvdcve-1.1-recent.meta")) {
            break
        } else {
            Thread.sleep(SleepBufferMSecs)
        }
    }

    for (i in 2002..2021) {
        var fileSaved = false
        while (!fileSaved) {
            if (downloadTextFile("https://nvd.nist.gov/feeds/json/cve/1.1/nvdcve-1.1-$i.meta")) {
                break
            } else {
                Thread.sleep(SleepBufferMSecs)
            }
        }
    }

    while (true) {
        if (downloadBinaryFile("https://nvd.nist.gov/feeds/json/cve/1.1/nvdcve-1.1-modified.json.gz")) {
            break
        } else {
            Thread.sleep(SleepBufferMSecs)
        }
    }

    while (true) {
        if (downloadBinaryFile("https://nvd.nist.gov/feeds/json/cve/1.1/nvdcve-1.1-recent.json.gz")) {
            break
        } else {
            Thread.sleep(SleepBufferMSecs)
        }
    }

    for (i in 2002..2021) {
        var fileSaved = false
        while (!fileSaved) {
            if (downloadBinaryFile("https://nvd.nist.gov/feeds/json/cve/1.1/nvdcve-1.1-$i.json.gz")) {
                break
            } else {
                Thread.sleep(SleepBufferMSecs)
            }
        }
    }

    while (true) {
        if (downloadBinaryFile("https://nvd.nist.gov/feeds/json/cve/1.1/nvdcve-1.1-modified.json.zip")) {
            break
        } else {
            Thread.sleep(SleepBufferMSecs)
        }
    }

    while (true) {
        if (downloadBinaryFile("https://nvd.nist.gov/feeds/json/cve/1.1/nvdcve-1.1-recent.json.zip")) {
            break
        } else {
            Thread.sleep(SleepBufferMSecs)
        }
    }

    for (i in 2002..2021) {
        var fileSaved = false
        while (!fileSaved) {
            if (downloadBinaryFile("https://nvd.nist.gov/feeds/json/cve/1.1/nvdcve-1.1-$i.json.zip")) {
                break
            } else {
                Thread.sleep(SleepBufferMSecs)
            }
        }
    }

    println("git add *")
    var result = ProcessBuilder("git", "add", "*").start().waitFor()
    if (result != 0) {
        throw IOException("Git add failed with result $result")
    }

    println("git commit -m \"Update\"")

    result = ProcessBuilder("git", "commit", "-m", SimpleDateFormat("YYYY-MM-dd:HH:mm:ss").format(Date())).start().waitFor()
    if (result != 0) {
        throw IOException("Git commit failed with result $result")
    }

    println("git push")
    result = ProcessBuilder("git", "push").start().waitFor()
    if (result != 0) {
        throw IOException("Git pushed failed with result $result")
    }


}

fun downloadTextFile(url: String): Boolean {
    println(url)
    val request: Request = Request.Builder().url(url).header("Accept-Encoding", "gzip, deflate").header("Connection", "keep-alive").build()
    return client.newCall(request).execute().use { response ->
        val code = response.code
        if (code != 200) {
            println(response.body!!.string())
            false
        } else {
            FileOutputStream(urlToFileName(url)).use { it.write(GZIPInputStream(response.body!!.byteStream()).readAllBytes()) }
            true
        }
    }
}

fun downloadBinaryFile(url: String): Boolean {
    println(url)
    val request: Request = Request.Builder().url(url).header("Connection", "keep-alive").build()
    return client.newCall(request).execute().use { response ->
        val code = response.code
        if (code != 200) {
            println(response.body!!.string())
            false
        } else {
            FileOutputStream(urlToFileName(url)).use { it.write(response.body!!.byteStream().readAllBytes()) }
            true
        }
    }
}

fun urlToFileName(url: String): String {
    var index = url.lastIndexOf('/')
    return url.substring(index + 1)
}
