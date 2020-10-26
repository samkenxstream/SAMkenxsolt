package com.github.hjubb.solt

import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import kotlinx.cli.default
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import pw.binom.io.file.*
import pw.binom.io.readText
import pw.binom.io.use
import pw.binom.io.utf8Reader
import pw.binom.toByteBufferUTF8

@ExperimentalCli
class Writer : Subcommand("write", "Generates a solc-input.json file for verification") {

    private val file by argument(ArgType.String, fullName = "file", description = "root directory for file search")

    private val nonOptimized by option(
        ArgType.Boolean,
        fullName = "no-optimization",
        shortName = "no-opt",
        description = "flag for whether to disable optimization"
    ).default(false)

    private val runs by option(ArgType.Int, shortName = "r", description = "how many runs to include in optimization")
        .default(200)

    private val isNpm by option(
        ArgType.Boolean,
        fullName = "npm",
        description = "flag for whether this project uses npm style imports (node_modules)"
    ).default(false)

    private val outputFile by option(
        ArgType.String,
        fullName = "output",
        shortName = "o",
        description = "file to write your standard json output to"
    )

    override fun execute() {

        val current = File(".${File.SEPARATOR}")
        val initial = current.relative(file) // hack to get always "./" type path
        val baseFolder = initial.path.stripLeadingSeparator().substringBefore(File.SEPARATOR)

        val files = collectFiles(baseFolder, initial, mutableSetOf())

        val deps = if (isNpm) {
            collectDeps(files.toSet(), mutableSetOf())
        } else {
            emptySet()
        }

        val sol = process(files + deps, nonOptimized, runs)
        val solString = Json {
            // any Json config here
        }.encodeToString(sol)

        // get the output file's name
        val filename = outputFile ?: "solc-input-${initial.nameWithoutExtension.toLowerCase()}.json"
        File(filename).write(append = false).use { channel ->
            channel.write(solString.toByteBufferUTF8())
        }
        println("file written to: $filename")
    }

    fun process(files: List<WrappedFile>, nonOptimized: Boolean, runs: Int): BigSolInput {
        val content = files.asSequence().associate {
            println("collecting: ${it.path}")
            it.path to it.getContent()
        }
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

    fun collectDeps(files: Set<WrappedFile>, deps: MutableSet<WrappedFile>): Set<WrappedFile> {
        return files.flatMap {
            val lines = it.getContent().content.lineSequence()
            lines.mapNotNull { line ->
                nodeRegex.matchEntire(line)
            }.flatMap { result ->
                val collected = result.groups[1]!!.let { group ->
                    val collected = WrappedFile(group.value, File("node_modules" + File.SEPARATOR + group.value))
                    collected
                }
                if (deps.contains(collected)) {
                    deps
                } else {
                    deps += collected
                    collectDeps("node_modules", collected, false, deps)
                }
            }
        }.toSet()
    }

    fun collectDeps(
        base: String,
        file: WrappedFile,
        includeBase: Boolean,
        fileSet: MutableSet<WrappedFile>
    ): Set<WrappedFile> {
        val lines = file.getContent().content.lineSequence()
        return lines.mapNotNull { line ->
            relativeRegex.matchEntire(line)
        }.flatMap { result ->
            val collected = result.groups[1]!!.let { group ->
                val relativeFile = file.file.relative(group.value)
                val basedPath = if (includeBase) {
                    relativeFile.path
                } else {
                    relativeFile.path.substringAfter(base + File.SEPARATOR)
                }
                val collected = WrappedFile(basedPath, relativeFile)
                collected
            }
            if (fileSet.contains(collected)) {
                fileSet
            } else {
                fileSet += collected
                collectDeps(base, collected, includeBase, fileSet)
            }
        }.toSet()
    }

    fun collectFiles(baseFolder: String, initial: File, fileSet: MutableSet<WrappedFile>): MutableList<WrappedFile> {
        val wrapped = WrappedFile(initial.path, initial)
        val files = mutableListOf(wrapped)
        files += collectDeps(baseFolder, wrapped, true, fileSet)
        return files
    }

    fun File.relative(path: String): File =
        path.split(File.SEPARATOR).fold(this.parent!!) { acc, relativePath ->
            when (relativePath) {
                ".." -> acc.parent!!
                "." -> acc
                else -> File(acc, relativePath)
            }
        }

    fun String.stripLeadingSeparator() = this.substringAfter(File.SEPARATOR)

    data class WrappedFile(val path: String, val file: File) {
        fun getContent(): Content {
            val content = file.read().use { channel ->
                channel.utf8Reader().readText()
            }
            return Content(content)
        }
    }

    fun getDir(dir: String): File {
        return File(dir)
    }

}