package me.bakumon.moneykeeper.ui.about

/**
 * @author Bakumon https://bakumon.me
 */
data class CardWithAction(val content: String, val actionText: String, val action: (() -> Unit))
