package com.github.hjubb.solt

import kotlinx.cli.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import pw.binom.io.*
import pw.binom.io.file.*
import pw.binom.toByteBufferUTF8

val truffleRegex = Regex("import\\s+?(?:(?:(?:[\\w*\\s{},]*)\\s+from\\s+?)|)(?:[\"|'](?!\\.{0,2}\\/)(.*?)[\"|'])[\\s]*?(?:;|\$|)")
val relativeRegex = Regex("import\\s+?(?:(?:(?:[\\w*\\s{},]*)\\s+from\\s+?)|)(?:[\"|'](.*?)[\"|'])[\\s]*?(?:;|\$|)")

fun main(args: Array<String>) {
    val parser = ArgParser("solt")
    val dir by parser.option(ArgType.String, shortName = "d", description = "root directory for file search").required()
    val nonOptimized by parser.option(
        ArgType.Boolean,
        fullName = "no-optimization",
        shortName = "no-opt",
        description = "flag for whether to disable optimization"
    ).default(false)
    val runs by parser.option(ArgType.Int, shortName = "r", description = "how many runs to include in optimization")
        .default(200)
    val ignoreExt by parser.option(
        ArgType.String,
        fullName = "ignore-ext",
        shortName = "i",
        description = "test file extension ending to ignore"
    ).default(".t.sol")
    val isTruffle by parser.option(
        ArgType.Boolean,
        fullName = "truffle",
        description = "flag for whether this project uses truffle style imports (node_modules)"
    ).default(false)

    try {
        parser.parse(args)
    } catch (e: Exception) {
        println(e.message)
        return
    }

    val files = collectFiles(getDir(dir), ignoreExt)
    val deps = if (isTruffle) {
        collectDeps(files.toSet())
    } else {
        emptyList()
    }
    val sol = process(files + deps, nonOptimized, runs)
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

fun collectDeps(files: Set<WrappedFile>): Set<WrappedFile> {
    return files.flatMap {
        val lines = it.getContent().content.lineSequence()
        lines.mapNotNull { line ->
            truffleRegex.matchEntire(line)
        }.flatMap { result ->
            val collected = result.groups[1]?.let { group ->
                val collected = WrappedFile(group.value, File("node_modules" + File.SEPARATOR + group.value))
                collected
            } ?: throw Exception("")
            setOf(collected) + collectDeps("node_modules", collected)
        }
    }.toSet()
}

fun collectDeps(base: String, file: WrappedFile): Set<WrappedFile> {
    val lines = file.getContent().content.lineSequence()
    return lines.mapNotNull { line ->
        relativeRegex.matchEntire(line)
    }.flatMap { result ->
        val collected = result.groups[1]?.let { group ->
            val relativeFile = file.file.relative(group.value)
            val nodePath = relativeFile.path.substringAfter(base + File.SEPARATOR)
            val collected = WrappedFile(nodePath, relativeFile)
            collected
        } ?: throw Exception("")
        setOf(collected) + collectDeps(setOf(collected))
    }.toSet()
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
                files += WrappedFile(path, file)
            }
        }
    }
    return files
}

data class WrappedFile(val path: String, val file: File) {
    fun getContent(): Content {
        val content = file.read().utf8Reader().use {
            it.readText()
        }
        println("collecting: $path")
        return Content(content)
    }
}

fun File.relative(path: String): File =
    path.split(File.SEPARATOR).fold(this.parent!!) { acc, relativePath ->
        when (relativePath) {
            ".." -> acc.parent!!
            "." -> acc
            else -> File(acc, relativePath)
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