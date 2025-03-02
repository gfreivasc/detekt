package io.gitlab.arturbosch.detekt.rules.bugs

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.api.internal.ActiveByDefault
import io.gitlab.arturbosch.detekt.api.internal.RequiresTypeResolution
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtPostfixExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.callUtil.getType
import org.jetbrains.kotlin.types.typeUtil.TypeNullability
import org.jetbrains.kotlin.types.typeUtil.nullability

/**
 * Reports unsafe calls on nullable types. These calls will throw a NullPointerException in case
 * the nullable value is null. Kotlin provides many ways to work with nullable types to increase
 * null safety. Guard the code appropriately to prevent NullPointerExceptions.
 *
 * <noncompliant>
 * fun foo(str: String?) {
 *     println(str!!.length)
 * }
 * </noncompliant>
 *
 * <compliant>
 * fun foo(str: String?) {
 *     println(str?.length)
 * }
 * </compliant>
 */
@RequiresTypeResolution
@ActiveByDefault(since = "1.2.0")
class UnsafeCallOnNullableType(config: Config = Config.empty) : Rule(config) {
    override val issue: Issue = Issue(
        "UnsafeCallOnNullableType",
        Severity.Defect,
        "Unsafe calls on nullable types detected. These calls will throw a NullPointerException in case " +
            "the nullable value is null.",
        Debt.TWENTY_MINS
    )

    override fun visitPostfixExpression(expression: KtPostfixExpression) {
        super.visitPostfixExpression(expression)
        if (bindingContext == BindingContext.EMPTY) return
        if (expression.operationToken == KtTokens.EXCLEXCL &&
            expression.baseExpression?.getType(bindingContext)?.nullability() == TypeNullability.NULLABLE
        ) {
            report(
                CodeSmell(
                    issue,
                    Entity.from(expression),
                    "Calling !! on a nullable type will throw a " +
                        "NullPointerException at runtime in case the value is null. It should be avoided."
                )
            )
        }
    }
}
