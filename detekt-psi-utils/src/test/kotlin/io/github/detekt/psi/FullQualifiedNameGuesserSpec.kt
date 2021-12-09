package io.github.detekt.psi

import io.github.detekt.test.utils.compileContentForTest
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class FullQualifiedNameGuesserSpec : Spek({

    describe("FullQualifiedNameGuesser") {
        context("With package") {
            val sut by memoized {
                val root = compileContentForTest(
                    """
                    package foo

                    import kotlin.jvm.JvmField
                    import kotlin.jvm.JvmStatic as Static
                    """.trimIndent()
                )

                FullQualifiedNameGuesser(root)
            }

            it("import") {
                assertThat(sut.getFullQualifiedName("JvmField"))
                    .containsExactlyInAnyOrder("kotlin.jvm.JvmField")
            }

            it("import with alias") {
                assertThat(sut.getFullQualifiedName("Static"))
                    .containsExactlyInAnyOrder("kotlin.jvm.JvmStatic")
            }

            it("import with alias but using real name") {
                assertThat(sut.getFullQualifiedName("JvmStatic"))
                    .containsExactlyInAnyOrder("foo.JvmStatic")
            }

            it("no import but maybe kotlin") {
                assertThat(sut.getFullQualifiedName("Result"))
                    .containsExactlyInAnyOrder("foo.Result", "kotlin.Result")
            }

            it("no import but not kotlin") {
                assertThat(sut.getFullQualifiedName("Asdf"))
                    .containsExactlyInAnyOrder("foo.Asdf")
            }

            it("import with subclass") {
                assertThat(sut.getFullQualifiedName("JvmField.Factory"))
                    .containsExactlyInAnyOrder("kotlin.jvm.JvmField.Factory")
            }

            it("alias-import with subclass") {
                assertThat(sut.getFullQualifiedName("Static.Factory"))
                    .containsExactlyInAnyOrder("kotlin.jvm.JvmStatic.Factory")
            }
        }

        context("Without package") {
            val sut by memoized {
                val root = compileContentForTest(
                    """
                    import kotlin.jvm.JvmField
                    """.trimIndent()
                )

                FullQualifiedNameGuesser(root)
            }

            it("no import but maybe kotlin") {
                assertThat(sut.getFullQualifiedName("Result"))
                    .containsExactlyInAnyOrder("kotlin.Result")
            }

            it("no import and not kotlin") {
                assertThat(sut.getFullQualifiedName("Asdf"))
                    .isEmpty()
            }
        }
    }
})
