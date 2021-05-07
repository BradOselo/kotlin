// WITH_RUNTIME
// IGNORE_BACKEND: JS, JS_IR, JVM
// !LANGUAGE: +SuspendFunctionAsSupertype

import kotlin.coroutines.*

var failure: String? = "FAIL"

class TopLevel1: suspend () -> Int {
    override suspend fun invoke(): Int {
        failure = null
        return 42
    }
}

class TopLevel2: suspend (String) -> Int {
    override suspend fun invoke(p: String): Int {
        failure = null
        return p.length
    }
}

class Outer {
    class Nested1: suspend () -> Int {
        override suspend fun invoke(): Int {
            failure = null
            return 42
        }
    }

    class Nested2: suspend (String) -> Int {
        override suspend fun invoke(p: String): Int {
            failure = null
            return p.length
        }
    }
}

fun objectLiteral1(): Boolean {
    failure = "FAIL OBJECT LITERAL 1"
    val o1: suspend () -> Int = object : suspend () -> Int {
        override suspend fun invoke(): Int {
            failure = null
            return 42
        }
    }
    o1.startCoroutine(Continuation(EmptyCoroutineContext) { it.getOrThrow() })
    return failure != null
}

fun objectLiteral2(): Boolean {
    failure = "FAIL OBJECT LITERAL 2"
    val o2: suspend (String) -> Int = object : suspend (String) -> Int {
        override suspend fun invoke(p: String): Int {
            failure = null
            return p.length
        }
    }
    o2.startCoroutine("Hello", Continuation(EmptyCoroutineContext) { it.getOrThrow() })
    return failure != null
}

fun topLevelClass1(): Boolean {
    failure = "FAIL TOP LEVEL CLASS 1"
    TopLevel1().startCoroutine(Continuation(EmptyCoroutineContext) { it.getOrThrow() })
    return failure != null
}

fun topLevelClass2(): Boolean {
    failure = "FAIL TOP LEVEL CLASS 2"
    TopLevel2().startCoroutine("Hello", Continuation(EmptyCoroutineContext) { it.getOrThrow() })
    return failure != null
}

fun nestedClass1(): Boolean {
    failure = "FAIL NESTED CLASS 1"
    Outer.Nested1().startCoroutine(Continuation(EmptyCoroutineContext) { it.getOrThrow() })
    return failure != null
}

fun nestedClass2(): Boolean {
    failure = "FAIL NESTED CLASS 2"
    Outer.Nested2().startCoroutine("Hello", Continuation(EmptyCoroutineContext) { it.getOrThrow() })
    return failure != null
}

fun localClass1(): Boolean {
    failure = "FAIL LOCAL CLASS 1"
    class Local : suspend () -> Int {
        override suspend fun invoke(): Int {
            failure = null
            return 42
        }
    }
    Local().startCoroutine(Continuation(EmptyCoroutineContext) { it.getOrThrow() })
    return failure != null
}

fun localClass2(): Boolean {
    fun foo(): Boolean {
        fun bar(): Boolean {
            failure = "FAIL LOCAL CLASS 2"
            class Local : suspend (String) -> Int {
                override suspend fun invoke(p: String): Int {
                    failure = null
                    return p.length
                }
            }
            Local().startCoroutine("Hello", Continuation(EmptyCoroutineContext) { it.getOrThrow() })
            return failure != null
        }
        return bar()
    }
    return foo()
}

fun box(): String {
    objectLiteral1() && objectLiteral2()
            && topLevelClass1() && topLevelClass2()
            && nestedClass1() && nestedClass2()
            && localClass1() && localClass2()

    return failure ?: "OK"
}
