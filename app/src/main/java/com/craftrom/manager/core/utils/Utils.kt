package com.craftrom.manager.core.utils

import kotlin.math.abs

open class Utils {
    companion object{

        /**
         * Format a number of tenths-units as a decimal string without using a
         * conversion to float.  E.g. 347 -> "34.7", -99 -> "-9.9"
         */
        fun tenthsToFixedString(x: Int): String {
            val tens = x / 10
            // use Math.abs to avoid "-9.-9" about -99
            return tens.toString() + "." + abs(x - 10 * tens)
        }
    }
}