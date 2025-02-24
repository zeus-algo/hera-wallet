/*
 * Copyright 2022 Pera Wallet, LDA
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.algorand.android.utils

import java.math.BigDecimal

private const val MAX_PERCENTAGE = 100.0
private const val PERCENTAGE_PRECISION = 2

infix fun BigDecimal.percentageChangeOf(secondValue: BigDecimal): BigDecimal {
    val firstValue = this
    if (firstValue <= BigDecimal.ZERO) return BigDecimal.ZERO
    val percentageChange = (secondValue.minus(firstValue).div(firstValue).toFloat() * MAX_PERCENTAGE)
    return BigDecimal(percentageChange).setScale(PERCENTAGE_PRECISION, BigDecimal.ROUND_HALF_UP)
}

infix fun BigDecimal.isGreaterThan(other: BigDecimal): Boolean {
    return this.compareTo(other) == 1
}

infix fun BigDecimal.isLesserThan(other: BigDecimal): Boolean {
    return this.compareTo(other) == -1
}

infix fun BigDecimal.isEqualTo(other: BigDecimal): Boolean {
    return this.compareTo(other) == 0
}

fun String?.toBigDecimalOrZero(): BigDecimal {
    return this?.toBigDecimalOrNull() ?: BigDecimal.ZERO
}

fun BigDecimal.isPositive(): Boolean {
    return signum() == 1
}

fun BigDecimal.isNegative(): Boolean {
    return signum() == -1
}

fun BigDecimal.isZero(): Boolean {
    return signum() == 0
}
