package me.bakumon.moneykeeper.ui.common

data class Empty(
        val tipText: String,
        /**
         * @see android.view.Gravity
         * android.view.Gravity#TOP
         * android.view.Gravity#CENTER
         */
        val gravity: Int
)