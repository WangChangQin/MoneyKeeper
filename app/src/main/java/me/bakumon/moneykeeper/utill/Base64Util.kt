/*
 * Copyright 2018 Bakumon. https://github.com/Bakumon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package me.bakumon.moneykeeper.utill

/**
 * Base64 编码
 *
 * @author Bakumon https://bakumon.me
 */
object Base64Util {

    private val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=".toCharArray()

    fun encode(data: String): String {
        return String(encode(data.toByteArray()))
    }

    private fun encode(data: ByteArray): CharArray {
        val out = CharArray((data.size + 2) / 3 * 4)
        var i = 0
        var index = 0
        while (i < data.size) {
            var quad = false
            var trip = false

            var `val` = 0xFF and data[i].toInt()
            `val` = `val` shl 8
            if (i + 1 < data.size) {
                `val` = `val` or (0xFF and data[i + 1].toInt())
                trip = true
            }
            `val` = `val` shl 8
            if (i + 2 < data.size) {
                `val` = `val` or (0xFF and data[i + 2].toInt())
                quad = true
            }
            out[index + 3] = alphabet[if (quad) `val` and 0x3F else 64]
            `val` = `val` shr 6
            out[index + 2] = alphabet[if (trip) `val` and 0x3F else 64]
            `val` = `val` shr 6
            out[index + 1] = alphabet[`val` and 0x3F]
            `val` = `val` shr 6
            out[index + 0] = alphabet[`val` and 0x3F]
            i += 3
            index += 4
        }
        return out
    }
}
