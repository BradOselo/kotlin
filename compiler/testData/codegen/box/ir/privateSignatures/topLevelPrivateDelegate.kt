// IGNORE_BACKEND: JVM, JVM_IR
// IGNORE_BACKEND_FIR: JVM_IR
// MODULE: lib
// FILE: l1.kt

package lib

private val d by lazy { "O" }

fun o() = d


// FILE: l2.kt

package lib

private val d by lazy { "K" }

fun k() = d


// MODULE: main(lib)
// FILE: main.kt

package main

import lib.*

fun box(): String = o() + k()