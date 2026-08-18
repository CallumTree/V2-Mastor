package com.example.domain.boq

data class ParsedBoqResult(
    val projectReference: String? = null,
    val workOrders: List<ParsedWorkOrder> = emptyList()
)

data class ParsedWorkOrder(
    val id: String = "pwo_" + System.currentTimeMillis() + "_" + (100..999).random(),
    val woRef: String,
    val description: String,
    val workType: String = "Internal Works",
    val customer: String = "Mayfair Heritage Holdings",
    val responsibleParty: String = "Apex Interiors Ltd",
    val scopeElements: List<ParsedScopeElement> = emptyList()
)

data class ParsedScopeElement(
    val id: String = "pse_" + System.currentTimeMillis() + "_" + (1000..9999).random(),
    val woRef: String,
    val code: String,
    val locationRoom: String,
    val description: String,
    val qty: Double,
    val units: String,
    val rate: Double, // BASE RATE ONLY
    val confidence: String = "HIGH", // "HIGH", "MEDIUM", "LOW"
    val flagReason: String? = null
)
