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

    downloadTextFile("https://nvd.nist.gov/feeds/json/cve/1.1/nvdcve-1.1-modified.meta")
    downloadTextFile("https://nvd.nist.gov/feeds/json/cve/1.1/nvdcve-1.1-recent.meta")
    for (i in 2002..2021) {
        downloadTextFile("https://nvd.nist.gov/feeds/json/cve/1.1/nvdcve-1.1-$i.meta")
    }

    downloadBinaryFile("https://nvd.nist.gov/feeds/json/cve/1.1/nvdcve-1.1-modified.json.gz")
    downloadBinaryFile("https://nvd.nist.gov/feeds/json/cve/1.1/nvdcve-1.1-recent.json.gz")
    for (i in 2002..2021) {
        downloadBinaryFile("https://nvd.nist.gov/feeds/json/cve/1.1/nvdcve-1.1-$i.json.gz")
    }

    downloadBinaryFile("https://nvd.nist.gov/feeds/json/cve/1.1/nvdcve-1.1-modified.json.zip")
    downloadBinaryFile("https://nvd.nist.gov/feeds/json/cve/1.1/nvdcve-1.1-recent.json.zip")
    for (i in 2002..2021) {
        downloadBinaryFile("https://nvd.nist.gov/feeds/json/cve/1.1/nvdcve-1.1-$i.json.zip")
    }

    val timestamp = SimpleDateFormat("YYYY-MM-dd:HH:mm:ss").format(Date())

    FileWriter("README.md").use { it.write("This site mirrors the NVD JSON Feeds site at https://nvd.nist.gov/vuln/data-feeds#JSON_FEED. It was last synchronized at $timestamp.") }

    println("git add *")
    var result = ProcessBuilder("git", "add", "*").start().waitFor()
    if (result != 0) {
        throw IOException("Git add failed with result $result")
    }

    println("git commit -m \"Update\"")

    result = ProcessBuilder("git", "commit", "-m", timestamp).start().waitFor()
    if (result != 0) {
        throw IOException("Git commit failed with result $result")
    }

    println("git push")
    result = ProcessBuilder("git", "push").start().waitFor()
    if (result != 0) {
        throw IOException("Git pushed failed with result $result")
    }
}

fun downloadTextFile(url: String) {
    println(url)
    while (true) {
        val request: Request = Request.Builder().url(url).header("Accept-Encoding", "gzip, deflate").header("Connection", "keep-alive").build()
        val fileSaved = client.newCall(request).execute().use { response ->
            val code = response.code
            if (code != 200) {
                println(response.body!!.string())
                false
            } else {
                FileOutputStream(urlToFileName(url)).use { it.write(GZIPInputStream(response.body!!.byteStream()).readAllBytes()) }
                true
            }
        }

        if (fileSaved) {
            break
        } else {
            Thread.sleep(SleepBufferMSecs)
        }
    }
}

fun downloadBinaryFile(url: String) {
    println(url)
    while (true) {
        val request: Request = Request.Builder().url(url).header("Connection", "keep-alive").build()
        val fileSaved =  client.newCall(request).execute().use { response ->
            val code = response.code
            if (code != 200) {
                println(response.body!!.string())
                false
            } else {
                FileOutputStream(urlToFileName(url)).use { it.write(response.body!!.byteStream().readAllBytes()) }
                true
            }
        }

        if (fileSaved) {
            break
        } else {
            Thread.sleep(SleepBufferMSecs)
        }
    }
}

fun urlToFileName(url: String): String {
    var index = url.lastIndexOf('/')
    return url.substring(index + 1)
}
