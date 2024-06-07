package de.cubbossa.pathfinder.examples

import com.google.gson.JsonParser
import lombok.RequiredArgsConstructor
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture

@RequiredArgsConstructor
class ExamplesFileReader {
    fun getExamples(link: String?): CompletableFuture<Collection<ExampleFile>> {
        return CompletableFuture.supplyAsync {
            try {
                val url = URL(link)
                val http = url.openConnection() as HttpURLConnection

                http.requestMethod = "GET"
                http.setRequestProperty("Accept", "application/vnd.github+json")

                http.connectTimeout = 5000

                val status = http.responseCode
                if (status != 200) {
                    throw RuntimeException("An error occurred while loading example visualizer: $status")
                }

                val `in` = BufferedReader(InputStreamReader(http.inputStream))
                var inputLine: String?
                val content = StringBuilder()
                while ((`in`.readLine().also { inputLine = it }) != null) {
                    content.append(inputLine)
                }
                `in`.close()

                val files: MutableCollection<ExampleFile> = HashSet()
                val array = JsonParser.parseString(content.toString()).asJsonArray
                array.forEach {
                    val obj = it.asJsonObject
                    val name = obj["name"].asString
                    if (!name.endsWith(".yml")) {
                        return@forEach
                    }
                    files.add(ExampleFile(name, obj["download_url"].asString))
                }
                http.disconnect()

                return@supplyAsync files
            } catch (t: Throwable) {
                throw RuntimeException(t)
            }
        }
    }

    fun read(link: String?): CompletableFuture<String> {
        return CompletableFuture.supplyAsync {
            try {
                val url = URL(link)
                val http = url.openConnection() as HttpURLConnection

                val stream = http.inputStream
                if (stream != null) {
                    val writer: Writer = StringWriter()
                    val buffer = CharArray(2048)
                    try {
                        val reader: Reader =
                            BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8))
                        var counter: Int
                        while ((reader.read(buffer).also { counter = it }) != -1) {
                            writer.write(buffer, 0, counter)
                        }
                    } finally {
                        stream.close()
                    }
                    return@supplyAsync writer.toString()
                }
                return@supplyAsync ""
            } catch (t: Throwable) {
                throw RuntimeException(t)
            }
        }
    }

    @JvmRecord
    data class ExampleFile(val name: String, val fetchUrl: String)
}
