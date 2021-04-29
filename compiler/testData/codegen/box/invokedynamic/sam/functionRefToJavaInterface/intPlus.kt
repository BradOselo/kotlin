// TARGET_BACKEND: JVM
// WITH_RUNTIME
// FULL_JDK

fun box(): String {
    val map = HashMap<String, Int>()
    map["a"] = 1
    map.merge("a", 2, Int::plus)
    val t = map["a"]
    return if (t == 3) "OK" else "failed: t=$t"
}
