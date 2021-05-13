// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER
// SKIP_TXT

// FILE: Base.java
public class Base<K> {}

// FILE: Test.java
import org.jetbrains.annotations.Nullable;

class Test extends Base<@Nullable String> {}

// FILE: main.kt
fun takeBaseOfNotNullStrings(x: Base<String>) {}

fun main() {
    val x = takeBaseOfNotNullStrings(<!NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS!>Test()<!>)
}