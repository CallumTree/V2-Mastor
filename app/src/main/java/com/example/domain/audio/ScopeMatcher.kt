package com.example.domain.audio

import com.example.data.entity.ScopeElement

enum class MatchConfidence {
    HIGH,
    MEDIUM,
    LOW
}

data class ScopeMatchResult(
    val scopeElement: ScopeElement,
    val finishedItemText: String,
    val suggestedClaimPercent: Double,
    val confidence: MatchConfidence
)

object ScopeMatcher {

    private val STOP_WORDS = setOf(
        "the", "and", "to", "a", "in", "of", "with", "for", "or", "is", "be"
    )

    fun matchFinishedItems(
        finishedItems: List<String>,
        scopeElements: List<ScopeElement>
    ): List<ScopeMatchResult> {
        if (finishedItems.isEmpty() || scopeElements.isEmpty()) return emptyList()

        val results = mutableListOf<ScopeMatchResult>()

        for (finishedItem in finishedItems) {
            val itemTokens = normaliseToTokens(finishedItem)
            val lowerFinishedItem = finishedItem.lowercase()
            val suggestedPercent = parseSuggestedClaimPercent(lowerFinishedItem)

            var bestElement: ScopeElement? = null
            var bestScore = 0

            for (element in scopeElements) {
                val elementText = "${element.description} ${element.locationRoom} ${element.code}"
                val elementTokens = normaliseToTokens(elementText).toSet()

                var score = 0
                for (token in itemTokens) {
                    if (token in elementTokens) {
                        score++
                    }
                }

                val room = element.locationRoom.trim()
                if (room.isNotBlank() && lowerFinishedItem.contains(room.lowercase())) {
                    score += 2
                }

                if (score > bestScore) {
                    bestScore = score
                    bestElement = element
                }
            }

            if (bestElement != null && bestScore >= 1) {
                val confidence = when {
                    bestScore >= 3 -> MatchConfidence.HIGH
                    bestScore == 2 -> MatchConfidence.MEDIUM
                    else -> MatchConfidence.LOW
                }

                results.add(
                    ScopeMatchResult(
                        scopeElement = bestElement,
                        finishedItemText = finishedItem,
                        suggestedClaimPercent = suggestedPercent,
                        confidence = confidence
                    )
                )
            }
        }

        return results
    }

    private fun normaliseToTokens(text: String): List<String> {
        return text
            .lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() && it !in STOP_WORDS }
    }

    private fun parseSuggestedClaimPercent(text: String): Double {
        val lower = text.lowercase()
        return when {
            lower.contains("75%") ||
                lower.contains("nearly") ||
                lower.contains("almost") ||
                lower.contains("substantially") -> 75.0

            lower.contains("50%") ||
                lower.contains("half") -> 50.0

            lower.contains("25%") ||
                lower.contains("started") ||
                lower.contains("begun") ||
                lower.contains("commenced") -> 25.0

            lower.contains("100%") ||
                lower.contains("complete") ||
                lower.contains("done") ||
                lower.contains("finished") ||
                lower.contains("signed off") -> 100.0

            else -> 100.0
        }
    }
}
