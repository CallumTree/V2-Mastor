package com.example.domain.procurement

import com.example.data.entity.ScopeElement

object TradePackageClassifier {

    private val ROOFING_KEYWORDS = listOf(
        "roof", "tile", "slate", "gutter", "flashing", "leadwork", "felt", "ridge", "fascia",
        "soffit", "eaves", "downpipe", "chimney", "soffits", "batten", "underlay"
    )

    private val SCAFFOLDING_KEYWORDS = listOf(
        "scaffold", "access", "hoist", "staging", "tower", "heras", "gantry", "handrail",
        "edge protection", "staging", "debris net"
    )

    private val PAINTING_KEYWORDS = listOf(
        "paint", "decorat", "primer", "emulsion", "varnish", "coat", "stain", "gloss",
        "undercoat", "wallcover", "prep", "sand down", "mist coat"
    )

    private val FENCING_KEYWORDS = listOf(
        "fence", "hoarding", "gate", "boundary", "perimeter", "closeboard", "picket",
        "post and rail", "paling", "trellis"
    )

    private val ELECTRICAL_KEYWORDS = listOf(
        "electric", "wiring", "socket", "light", "m&e", "conduit", "fuse", "consumer unit",
        "pendant", "luminaire", "pat test", "rewire", "pIR", "light fitting"
    )

    private val PLUMBING_KEYWORDS = listOf(
        "plumb", "pipe", "sanitary", "shower", "tap", "valve", "drain", "waterproof",
        "screed", "basin", "wc", "radiator", "boiler", "cistern", "soil pipe", "tray"
    )

    private val JOINERY_KEYWORDS = listOf(
        "joinery", "cabinet", "wood", "timber", "door", "panel", "skirting", "architrave",
        "oak", "carpentry", "window", "frame", "joist", "floorboard", "ironmongery", "kitchen"
    )

    /**
     * Classifies a single ScopeElement into a Trade category string based on keyword matching.
     * Returns "Unclassified" if no confident match is found.
     */
    fun classifyScopeElement(element: ScopeElement): String {
        val textToSearch = "${element.description} ${element.code} ${element.locationRoom} ${element.notes}".lowercase()

        return when {
            ROOFING_KEYWORDS.any { textToSearch.contains(it) } -> "Roofing"
            SCAFFOLDING_KEYWORDS.any { textToSearch.contains(it) } -> "Scaffolding"
            PAINTING_KEYWORDS.any { textToSearch.contains(it) } -> "Painting"
            FENCING_KEYWORDS.any { textToSearch.contains(it) } -> "Fencing"
            ELECTRICAL_KEYWORDS.any { textToSearch.contains(it) } -> "Electrical"
            PLUMBING_KEYWORDS.any { textToSearch.contains(it) } -> "Plumbing"
            JOINERY_KEYWORDS.any { textToSearch.contains(it) } -> "Joinery"
            else -> "Unclassified"
        }
    }

    /**
     * Groups scope elements by classified trade.
     */
    fun groupScopeElementsByTrade(elements: List<ScopeElement>): Map<String, List<ScopeElement>> {
        return elements.groupBy { classifyScopeElement(it) }
    }
}
