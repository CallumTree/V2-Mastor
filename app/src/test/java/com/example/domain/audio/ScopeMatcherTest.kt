package com.example.domain.audio

import com.example.data.entity.ScopeElement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScopeMatcherTest {

    private fun createScopeElement(
        id: String = "elem-1",
        woRef: String = "WO-001",
        locationRoom: String = "Kitchen",
        code: String = "K-01",
        description: String = "Install base kitchen cabinets and quartz worktops",
        qty: Double = 1.0,
        units: String = "nr",
        rate: Double = 1500.0
    ): ScopeElement {
        return ScopeElement(
            id = id,
            woRef = woRef,
            locationRoom = locationRoom,
            code = code,
            description = description,
            qty = qty,
            units = units,
            rate = rate
        )
    }

    @Test
    fun exactDescriptionMatch_returnsHighConfidence() {
        val element = createScopeElement(
            description = "Install base kitchen cabinets and quartz worktops",
            locationRoom = "Ground Floor",
            code = "CAB-01"
        )

        val finishedItems = listOf("Installed base kitchen cabinets and quartz worktops")
        val results = ScopeMatcher.matchFinishedItems(finishedItems, listOf(element))

        assertEquals(1, results.size)
        val match = results[0]
        assertEquals(element.id, match.scopeElement.id)
        assertEquals(MatchConfidence.HIGH, match.confidence)
        assertEquals(100.0, match.suggestedClaimPercent, 0.001)
    }

    @Test
    fun roomNameInFinishedItem_boostsCorrectScopeElement() {
        val kitchenCabinet = createScopeElement(
            id = "elem-kitchen",
            description = "Wall cabinets",
            locationRoom = "Kitchen",
            code = "CAB-01"
        )
        val bathroomCabinet = createScopeElement(
            id = "elem-bathroom",
            description = "Wall cabinets",
            locationRoom = "Bathroom",
            code = "CAB-02"
        )

        val finishedItems = listOf("Fitted wall cabinets in Kitchen")
        val results = ScopeMatcher.matchFinishedItems(
            finishedItems,
            listOf(kitchenCabinet, bathroomCabinet)
        )

        assertEquals(1, results.size)
        val match = results[0]
        assertEquals("elem-kitchen", match.scopeElement.id)
    }

    @Test
    fun nearlyComplete_extracts75Percent() {
        val element = createScopeElement(
            description = "Timber floorboard replacement",
            locationRoom = "Lounge",
            code = "FLR-01"
        )

        val finishedItems = listOf("Timber floorboard replacement nearly complete")
        val results = ScopeMatcher.matchFinishedItems(finishedItems, listOf(element))

        assertEquals(1, results.size)
        val match = results[0]
        assertEquals(75.0, match.suggestedClaimPercent, 0.001)
    }

    @Test
    fun textWithNoMatchingTokens_returnsEmptyList() {
        val element = createScopeElement(
            description = "Structural steel beam RSJ installation",
            locationRoom = "Basement",
            code = "STL-01"
        )

        val finishedItems = listOf("Garden fence painting and turf laying")
        val results = ScopeMatcher.matchFinishedItems(finishedItems, listOf(element))

        assertTrue(results.isEmpty())
    }
}
