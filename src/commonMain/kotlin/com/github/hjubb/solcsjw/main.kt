package com.github.hjubb.solcsjw

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.required
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
    val optimized by parser.option(ArgType.Boolean, shortName = "opt", description = "flag for whether to enable optimization").default(true)
    val runs by parser.option(ArgType.Int, shortName = "r", description = "how many runs to include in optimization").default(200)
    parser.parse(args)
    val files = collectFiles(getDir(dir))
    val sol = process(files, optimized, runs)
    val solString = Json {
        // any Json config here
    }.encodeToString(sol)
    File("solc-input.json").write(append = false).write(solString.toByteBufferUTF8())
    println("file written to: solc-input.json")
}

fun process(files: List<WrappedFile>, optimized: Boolean, runs: Int): BigSolInput {
    val content = files.asSequence().associate { it.path to it.getContent() }
    return BigSolInput(language = "Solidity",
            sources = content,
            settings = buildJsonObject {
                put("metadata", buildJsonObject {
                    put("useLiteralContent", true)
                })
                if (optimized) {
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

fun collectFiles(dir: File): List<WrappedFile> {
    val files = mutableListOf<WrappedFile>()
    val regex = Regex("(?:\\.[\\\\|/])?([\\w|\\W]+\\.sol)")
    for (file in dir.iterator()) {
        if (file.isDirectory) {
            files += collectFiles(file)
        }
        regex.matchEntire(file.path)?.groups?.get(1)?.let { matchGroup ->
            val path = matchGroup.value
            if (!path.endsWith(".t.sol")) {
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