package io.gitlab.arturbosch.detekt.api

import io.github.detekt.psi.FullQualifiedNameGuesser
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtFile

/**
 * Primary use case for an AnnotationExcluder is to decide if a KtElement should be
 * excluded from further analysis. This is done by checking if a special annotation
 * is present over the element.
 */
class AnnotationExcluder(
    root: KtFile,
    excludes: List<String>,
) {
    private val excludes = excludes.map {
        it.removePrefix("*").removeSuffix("*")
    }

    private val fullQualifiedNameGuesser = FullQualifiedNameGuesser(root)

    @Deprecated("Use AnnotationExclude(KtFile, List<String>) instead")
    constructor(root: KtFile, excludes: SplitPattern) : this(root, excludes.mapAll { it })

    /**
     * Is true if any given annotation name is declared in the SplitPattern
     * which basically describes entries to exclude.
     */
    fun shouldExclude(annotations: List<KtAnnotationEntry>): Boolean =
        annotations.firstOrNull(::isExcluded) != null

    private fun isExcluded(annotation: KtAnnotationEntry): Boolean {
        val annotationText = annotation.typeReference?.text?.ifEmpty { null } ?: return false
        /*
         We can't know if the annotationText is a full qualified name or not. We can have these cases:
         @Component
         @Component.Factory
         @dagger.Component.Factory
         For that reason we use a heuristic here: If the first character is lower case we assume it's a package name
         */
        val possibleNames = if (!annotationText.first().isLowerCase()) {
            fullQualifiedNameGuesser.getFullQualifiedName(annotationText) + annotationText
        } else {
            listOf(
                annotationText
                    .split(".")
                    .dropWhile { it.first().isLowerCase() }
                    .joinToString(".")
                    .ifEmpty { annotationText },
                annotationText
            )
        }
        return excludes.any { exclude -> possibleNames.contains(exclude) }
    }
}
