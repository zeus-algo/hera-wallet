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

package network.voi.hera.utils

import com.mitsinsar.peracompactdecimalformat.PeraCompactDecimalFormatBuilder
import com.mitsinsar.peracompactdecimalformat.locals.ChineseLocale
import com.mitsinsar.peracompactdecimalformat.locals.EnglishLocale
import com.mitsinsar.peracompactdecimalformat.locals.FrenchLocale
import com.mitsinsar.peracompactdecimalformat.locals.GermanLocale
import com.mitsinsar.peracompactdecimalformat.locals.ItalianLocale
import com.mitsinsar.peracompactdecimalformat.locals.JapaneseLocale
import com.mitsinsar.peracompactdecimalformat.locals.KoreanLocale
import com.mitsinsar.peracompactdecimalformat.locals.PortugueseLocale
import com.mitsinsar.peracompactdecimalformat.locals.SpanishLocale
import com.mitsinsar.peracompactdecimalformat.locals.TurkishLocale
import com.mitsinsar.peracompactdecimalformat.locals.base.BaseLocale
import com.mitsinsar.peracompactdecimalformat.utils.fractionaldigit.FractionalDigit
import com.mitsinsar.peracompactdecimalformat.utils.toPeraDecimal
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

const val PERCENT_FORMAT = "##0.00'%'"
const val PLUS_SIGN = "+"
const val MINUS_SIGN = "-"

fun getFormatter(
    format: String,
    includeMagnitude: Boolean = false,
    positiveSuffix: String? = null,
    negativeSuffix: String? = null
): DecimalFormat {
    return DecimalFormat(format, DecimalFormatSymbols()).apply {
        roundingMode = RoundingMode.DOWN
        if (positiveSuffix != null) this.positiveSuffix = positiveSuffix
        if (negativeSuffix != null) this.negativeSuffix = negativeSuffix
        if (includeMagnitude) {
            this.positivePrefix = PLUS_SIGN
            this.negativePrefix = MINUS_SIGN
        }
    }
}

fun formatCompactNumber(number: BigDecimal, fractionalDigitCreator: FractionalDigit.FractionalDigitCreator): String {
    return PeraCompactDecimalFormatBuilder.getInstance()
        .setLocale(getPeraCompactDecimalFormatterLocal())
        .setFractionalDigitCreator(fractionalDigitCreator)
        .build()
        .format(number.toPeraDecimal()).formattedNumberWithSuffix
}

// TODO: Create a library function for this
private fun getPeraCompactDecimalFormatterLocal(): BaseLocale {
    return when (Locale.getDefault().language.uppercase()) {
        TurkishLocale.localeConstant -> TurkishLocale
        EnglishLocale.localeConstant -> EnglishLocale
        GermanLocale.localeConstant -> GermanLocale
        ChineseLocale.localeConstant -> ChineseLocale
        FrenchLocale.localeConstant -> FrenchLocale
        ItalianLocale.localeConstant -> ItalianLocale
        JapaneseLocale.localeConstant -> JapaneseLocale
        KoreanLocale.localeConstant -> KoreanLocale
        PortugueseLocale.localeConstant -> PortugueseLocale
        SpanishLocale.localeConstant -> SpanishLocale
        else -> EnglishLocale
    }
}
