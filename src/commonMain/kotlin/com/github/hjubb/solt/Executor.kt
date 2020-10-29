package com.github.hjubb.solt

expect class Executor() {
    fun execute(verifier: suspend () -> Unit)
}