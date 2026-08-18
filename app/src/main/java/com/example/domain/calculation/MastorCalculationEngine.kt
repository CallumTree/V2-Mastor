package com.example.domain.calculation

import com.example.data.entity.Project
import com.example.data.entity.ScopeElement
import com.example.data.entity.Valuation
import com.example.data.entity.VariationOrder
import com.example.data.entity.WorkOrder
import kotlin.math.roundToLong

/**
 * Audit Trace Step explaining how a financial figure was calculated.
 * Satisfies Principle 5: "Every calculated figure must be traceable back to the source data that produced it."
 */
data class CalculationTraceStep(
    val title: String,
    val formula: String,
    val inputsDescription: String,
    val resultFormatted: String
)

/**
 * Calculated domain representation of a Scope Element.
 * Cost is derived live (qty * rate).
 */
data class CalculatedScopeElement(
    val entity: ScopeElement,
    val baseCost: Double,
    val claimedBaseValue: Double
)

/**
 * Calculated domain representation of a Work Order.
 * Revenue, cost, and % complete are derived live on every query.
 */
data class CalculatedWorkOrder(
    val entity: WorkOrder,
    val totalBaseCost: Double,
    val claimedBaseValue: Double,
    val percentComplete: Double,
    val totalRevenueWithUplifts: Double,
    val scopeElementsCount: Int,
    val traceSteps: List<CalculationTraceStep>
)

/**
 * Calculated domain representation of a Valuation.
 * Section totals, uplift amounts, and Grand Invoice Total are derived live.
 */
data class CalculatedValuation(
    val entity: Valuation,
    val scopeBaseClaimedTotal: Double,
    val voBaseClaimedTotal: Double,
    val subtotalBaseClaimed: Double,
    val uplift1Percent: Double,
    val uplift1Amount: Double,
    val uplift2Percent: Double,
    val uplift2Amount: Double,
    val grandInvoiceTotal: Double,
    val claimedScopeCount: Int,
    val claimedVoCount: Int,
    val traceSteps: List<CalculationTraceStep>
)

/**
 * Single source of truth calculation engine for all financial logic across Mastor.
 * No calculated figure is stored in SQLite tables; all are generated through this engine.
 */
object MastorCalculationEngine {

    /**
     * Round to 2 decimal places for financial GBP amounts.
     */
    fun roundMoney(amount: Double): Double {
        return (amount * 100.0).roundToLong() / 100.0
    }

    /**
     * Formats financial amounts as standard UK currency string (£X,XXX.XX).
     */
    fun formatCurrency(amount: Double): String {
        val rounded = roundMoney(amount)
        val isNegative = rounded < 0
        val absAmount = kotlin.math.abs(rounded)
        val stringVal = String.format(java.util.Locale.UK, "%,.2f", absAmount)
        return if (isNegative) "-£$stringVal" else "£$stringVal"
    }

    /**
     * Calculate single Scope Element figures.
     */
    fun calculateScopeElement(scopeElement: ScopeElement): CalculatedScopeElement {
        val baseCost = roundMoney(scopeElement.qty * scopeElement.rate)
        val claimedBaseValue = roundMoney(baseCost * (scopeElement.claimPercent / 100.0))
        return CalculatedScopeElement(
            entity = scopeElement,
            baseCost = baseCost,
            claimedBaseValue = claimedBaseValue
        )
    }

    /**
     * Calculate central project uplifts applied to a base amount.
     */
    fun calculateProjectUplifts(
        baseAmount: Double,
        uplift1Percent: Double,
        uplift2Percent: Double
    ): Triple<Double, Double, Double> {
        val u1Amount = roundMoney(baseAmount * (uplift1Percent / 100.0))
        val subtotal1 = baseAmount + u1Amount
        val u2Amount = roundMoney(subtotal1 * (uplift2Percent / 100.0))
        val total = roundMoney(subtotal1 + u2Amount)
        return Triple(u1Amount, u2Amount, total)
    }

    /**
     * Calculate Work Order figures derived live from its Scope Elements and Project Uplifts.
     */
    fun calculateWorkOrder(
        workOrder: WorkOrder,
        scopeElements: List<ScopeElement>,
        project: Project
    ): CalculatedWorkOrder {
        val calculatedScopes = scopeElements.map { calculateScopeElement(it) }
        val totalBaseCost = roundMoney(calculatedScopes.sumOf { it.baseCost })
        val claimedBaseValue = roundMoney(calculatedScopes.sumOf { it.claimedBaseValue })

        val percentComplete = if (totalBaseCost > 0.0) {
            roundMoney((claimedBaseValue / totalBaseCost) * 100.0)
        } else {
            0.0
        }

        val (_, _, revenueWithUplifts) = calculateProjectUplifts(
            baseAmount = claimedBaseValue,
            uplift1Percent = project.uplift1Percent,
            uplift2Percent = project.uplift2Percent
        )

        val traceSteps = listOf(
            CalculationTraceStep(
                title = "Total Work Order Base Cost",
                formula = "Sum(Scope Element qty × rate)",
                inputsDescription = "${scopeElements.size} Scope Elements listed",
                resultFormatted = formatCurrency(totalBaseCost)
            ),
            CalculationTraceStep(
                title = "Claimed Base Value",
                formula = "Sum(Base Cost × claim_percent)",
                inputsDescription = "Claimed scope items sum",
                resultFormatted = formatCurrency(claimedBaseValue)
            ),
            CalculationTraceStep(
                title = "Work Order % Complete",
                formula = "(Claimed Base Value ÷ Total Base Cost) × 100",
                inputsDescription = "${formatCurrency(claimedBaseValue)} ÷ ${formatCurrency(totalBaseCost)}",
                resultFormatted = "$percentComplete%"
            ),
            CalculationTraceStep(
                title = "Work Order Revenue",
                formula = "Claimed Base Value + Central Uplifts (${project.uplift1Percent}% + ${project.uplift2Percent}%)",
                inputsDescription = "Project uplift applied centrally to claimed base",
                resultFormatted = formatCurrency(revenueWithUplifts)
            )
        )

        return CalculatedWorkOrder(
            entity = workOrder,
            totalBaseCost = totalBaseCost,
            claimedBaseValue = claimedBaseValue,
            percentComplete = percentComplete,
            totalRevenueWithUplifts = revenueWithUplifts,
            scopeElementsCount = scopeElements.size,
            traceSteps = traceSteps
        )
    }

    /**
     * Calculate Valuation live total derived from associated Scope Elements, Variation Orders, and Project Uplifts.
     * Implements Section 4: Value to Date - Previously Certified = This Valuation
     */
    fun calculateValuation(
        valuation: Valuation,
        scopeElements: List<ScopeElement>,
        variationOrders: List<VariationOrder>,
        project: Project
    ): CalculatedValuation {
        // Filter elements attached to this valuation
        val valScopes = scopeElements.filter { it.currentValuationId == valuation.id }
        val valVos = variationOrders.filter { it.currentValuationId == valuation.id && it.tick }

        // This valuation increment = (percent_claimed_to_date - previously_certified_percent) * baseCost
        val scopeBaseClaimedTotal = roundMoney(
            valScopes.sumOf { elem ->
                val baseCost = elem.qty * elem.rate
                val incrementPercent = (elem.claimPercent - elem.previouslyCertifiedPercent).coerceAtLeast(0.0)
                roundMoney(baseCost * (incrementPercent / 100.0))
            }
        )

        val voBaseClaimedTotal = roundMoney(
            valVos.sumOf { vo ->
                val baseCost = vo.qty * vo.rate
                val voClaimPercent = if (vo.tick) (if (vo.claimPercent <= 0.0) 100.0 else vo.claimPercent) else 0.0
                val incrementPercent = (voClaimPercent - vo.previouslyCertifiedPercent).coerceAtLeast(0.0)
                roundMoney(baseCost * (incrementPercent / 100.0))
            }
        )

        val subtotalBaseClaimed = roundMoney(scopeBaseClaimedTotal + voBaseClaimedTotal)

        val (u1Amount, u2Amount, grandInvoiceTotal) = calculateProjectUplifts(
            baseAmount = subtotalBaseClaimed,
            uplift1Percent = project.uplift1Percent,
            uplift2Percent = project.uplift2Percent
        )

        val traceSteps = listOf(
            CalculationTraceStep(
                title = "Scope Elements Base Claimed",
                formula = "Sum(Scope Element qty × rate × claim_percent)",
                inputsDescription = "${valScopes.size} Scope Elements attached to Valuation ${valuation.valuationNumber}",
                resultFormatted = formatCurrency(scopeBaseClaimedTotal)
            ),
            CalculationTraceStep(
                title = "Variation Orders Base Claimed",
                formula = "Sum(VO qty × rate where tick = true)",
                inputsDescription = "${valVos.size} Variation Orders attached & ticked",
                resultFormatted = formatCurrency(voBaseClaimedTotal)
            ),
            CalculationTraceStep(
                title = "Subtotal Base Claimed",
                formula = "Scope Claimed + VO Claimed",
                inputsDescription = "${formatCurrency(scopeBaseClaimedTotal)} + ${formatCurrency(voBaseClaimedTotal)}",
                resultFormatted = formatCurrency(subtotalBaseClaimed)
            ),
            CalculationTraceStep(
                title = "Uplift 1 (${project.uplift1Percent}%)",
                formula = "Subtotal Base Claimed × ${project.uplift1Percent}%",
                inputsDescription = "Centrally stored project uplift 1",
                resultFormatted = formatCurrency(u1Amount)
            ),
            CalculationTraceStep(
                title = "Uplift 2 (${project.uplift2Percent}%)",
                formula = "(Subtotal Base + Uplift 1) × ${project.uplift2Percent}%",
                inputsDescription = "Centrally stored project uplift 2",
                resultFormatted = formatCurrency(u2Amount)
            ),
            CalculationTraceStep(
                title = "Grand Invoice Total",
                formula = "Subtotal Base + Uplift 1 + Uplift 2",
                inputsDescription = "Final live calculated valuation invoice amount",
                resultFormatted = formatCurrency(grandInvoiceTotal)
            )
        )

        return CalculatedValuation(
            entity = valuation,
            scopeBaseClaimedTotal = scopeBaseClaimedTotal,
            voBaseClaimedTotal = voBaseClaimedTotal,
            subtotalBaseClaimed = subtotalBaseClaimed,
            uplift1Percent = project.uplift1Percent,
            uplift1Amount = u1Amount,
            uplift2Percent = project.uplift2Percent,
            uplift2Amount = u2Amount,
            grandInvoiceTotal = grandInvoiceTotal,
            claimedScopeCount = valScopes.size,
            claimedVoCount = valVos.size,
            traceSteps = traceSteps
        )
    }
}
