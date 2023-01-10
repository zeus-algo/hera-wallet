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

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.moshi.Moshi
import org.json.JSONObject

private const val JSON_INDENT = 4

fun getFormattedJsonArrayString(json: String): String {
    return try {
        JSONObject(json).toString(JSON_INDENT)
    } catch (exception: Exception) {
        recordException(exception)
        "{}"
    }
}

inline fun <reified T> Gson.fromJson(json: String): T? {
    return try {
        fromJson<T>(json, object : TypeToken<T>() {}.type)
    } catch (exception: Exception) {
        null
    }
}

inline fun <reified T> Moshi.fromJson(json: String): T? {
    return try {
        with(adapter(T::class.java)) { fromJson(json) }
    } catch (exception: Exception) {
        null
    }
}
