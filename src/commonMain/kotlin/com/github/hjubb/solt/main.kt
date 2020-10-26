package com.github.hjubb.solt

import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli
import pw.binom.Environment
import pw.binom.getEnv

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
        if (Environment.getEnv("DEBUG") != null) {
            e.printStackTrace()
        } else {
            println(e.message)
        }
    }
}