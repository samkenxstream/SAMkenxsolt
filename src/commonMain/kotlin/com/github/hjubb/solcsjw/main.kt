package com.github.hjubb.solcsjw

import kotlinx.cli.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import pw.binom.io.BufferedInput
import pw.binom.io.file.*
import pw.binom.io.readText
import pw.binom.io.utf8Reader
import pw.binom.toByteBufferUTF8

fun main(args: Array<String>) {
    val parser = ArgParser("solc-sjw")
    val dir by parser.option(ArgType.String, shortName = "d", description = "root directory for file search").required()
    val `no-optimization` by parser.option(ArgType.Boolean, shortName = "no-opt", description = "flag for whether to disable optimization").default(false)
    val runs by parser.option(ArgType.Int, shortName = "r", description = "how many runs to include in optimization").default(200)
    val `ignore-ext` by parser.option(ArgType.String, shortName = "i", description = "test file extension ending to ignore").default(".t.sol")

    try {
        parser.parse(args)
    } catch (e: Exception) {
        println(e.message)
        return
    }

    val files = collectFiles(getDir(dir), `ignore-ext`)
    val sol = process(files, `no-optimization`, runs)
    val solString = Json {
        // any Json config here
    }.encodeToString(sol)
    File("solc-input.json").write(append = false).write(solString.toByteBufferUTF8())
    println("file written to: solc-input.json")
}

fun process(files: List<WrappedFile>, nonOptimized: Boolean, runs: Int): BigSolInput {
    val content = files.asSequence().associate { it.path to it.getContent() }
    return BigSolInput(language = "Solidity",
            sources = content,
            settings = buildJsonObject {
                put("metadata", buildJsonObject {
                    put("useLiteralContent", true)
                })
                if (!nonOptimized) {
                    put("optimizer", buildJsonObject {
                        put("enabled", true)
                        put("runs", runs)
                    })
                }
                put("outputSelection", buildJsonObject {
                    put("*", buildJsonObject {
                        put("*", buildJsonArray {
                            add("abi")
                            add("evm.bytecode")
                            add("evm.deployedBytecode")
                            add("evm.methodIdentifiers")
                        })
                        put("", buildJsonArray {
                            add("id")
                            add("ast")
                        })
                    })
                })
            }
    )
}

fun collectFiles(dir: File, testExt: String): List<WrappedFile> {
    val files = mutableListOf<WrappedFile>()
    val regex = Regex("(?:\\.[\\\\|/])?([\\w|\\W]+\\.sol)")
    for (file in dir.iterator()) {
        if (file.isDirectory) {
            files += collectFiles(file, testExt)
        }
        regex.matchEntire(file.path)?.groups?.get(1)?.let { matchGroup ->
            val path = matchGroup.value
            if (!path.endsWith(testExt)) {
                println("collecting: $file")
                files += WrappedFile(path, file)
            }
        }
    }
    return files
}

data class WrappedFile(val path: String, val file: File) {
    fun getContent(): Content {
        val content = BufferedInput(file.read()).utf8Reader().readText()
        return Content(content)
    }
}

fun getDir(dir: String): File {
    return File(dir)
}

@Serializable
data class Content(val content: String)

@Serializable
data class BigSolInput(
        val language: String,
        val sources: Map<String, Content>,
        val settings: JsonObject
)