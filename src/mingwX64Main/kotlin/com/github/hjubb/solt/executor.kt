package com.github.hjubb.solt

import kotlinx.coroutines.runBlocking

actual class Executor {
    actual fun execute(verifier: suspend () -> Unit) = runBlocking {
        verifier()
    }

}