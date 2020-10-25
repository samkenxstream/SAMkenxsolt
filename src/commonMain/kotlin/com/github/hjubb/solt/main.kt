package com.github.hjubb.solt

import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli

val nodeRegex =
    Regex("import\\s+?(?:(?:(?:[\\w*\\s{},]*)\\s+from\\s+?)|)(?:[\"|'](?!\\.{0,2}\\/)(.*?)[\"|'])[\\s]*?(?:;|\$|)")
val relativeRegex =
    Regex("import\\s+?(?:(?:(?:[\\w*\\s{},]*)\\s+from\\s+?)|)(?:[\"|'](\\.{0,2}\\/.*?)[\"|'])[\\s]*?(?:;|\$|)")

@ExperimentalCli
fun main(args: Array<String>) {
    val parser = ArgParser("solt")

    parser.subcommands(Writer(), Verifier())

    try {
        parser.parse(args)
    } catch (e: Exception) {
        println(e.message)
    }
}