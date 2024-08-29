package com.pos

import android.content.Context
import android.os.Build

object Utility {

    @JvmStatic
    fun byte2HexStr(byteArray: ByteArray, offset: Int, length: Int): String {
        if (byteArray.isEmpty()) {
            return ""
        }
        val stringBuilder = StringBuilder()
        for (i in offset until (offset + length)) {
            val hexString = Integer.toHexString(byteArray[i].toInt() and 0xFF)
            stringBuilder.append(if (hexString.length == 1) "0$hexString" else hexString)
        }
        return stringBuilder.toString().toUpperCase().trim()
    }

    @JvmStatic
    fun byte2HexStr(byteArray: ByteArray): String {
        if (byteArray.isEmpty()) {
            return ""
        }
        val stringBuilder = StringBuilder()
        for (b in byteArray) {
            val hexString = Integer.toHexString(b.toInt() and 0xFF)
            stringBuilder.append(if (hexString.length == 1) "0$hexString" else hexString)
        }
        return stringBuilder.toString().toUpperCase().trim()
    }

    @JvmStatic
    fun hexStr2Byte(hexString: String?): ByteArray {
        if (hexString.isNullOrEmpty()) {
            return byteArrayOf(0)
        }
        val hexStrTrimmed = hexString.replace(" ", "")
        var hexStr = hexStrTrimmed
        val len = hexStrTrimmed.length
        if (len % 2 == 1) {
            hexStr = hexStrTrimmed + "0"
        }
        val result = ByteArray(hexStr.length / 2)
        var i = 0
        while (i < hexStr.length) {
            val highChar = hexStr[i]
            val lowChar = hexStr[i + 1]
            val high = charToInt(highChar)
            val low = charToInt(lowChar)
            result[i / 2] = (high * 16 + low).toByte()
            i += 2
        }
        return result
    }

    @JvmStatic
    fun hexStr2Byte(hexString: String?, beginIndex: Int, length: Int): ByteArray {
        if (hexString.isNullOrEmpty()) {
            return byteArrayOf(0)
        }
        var len = length
        if (len > hexString.length) {
            len = hexString.length
        }
        var hexStr = hexString
        if (len % 2 == 1) {
            hexStr += "0"
            len++
        }
        val result = ByteArray(len / 2)
        for (i in beginIndex until len step 2) {
            val s = hexStr.substring(i, i + 2)
            val v = s.toInt(16)
            result[i / 2] = v.toByte()
        }
        return result
    }

    @JvmStatic
    fun hex2Dec(hex: Int): Byte {
        return ((hex / 10) * 16 + hex % 10).toByte()
    }

    @JvmStatic
    fun dec2Int(dec: Byte): Int {
        var high = (dec.toInt() and 0x7F) shr 4
        if (dec.toInt() and 0x80 != 0) {
            high += 8
        }
        return high * 10 + (dec.toInt() and 0x0F)
    }

    @JvmStatic
    fun charToInt(c: Char): Int {
        return when {
            c in '0'..'9' -> c - '0'
            c in 'a'..'f' -> c - 'a' + 10
            c in 'A'..'F' -> c - 'A' + 10
            else -> 0
        }
    }

    @JvmStatic
    fun getDeviceId(context: Context): String {
        var uniqueId = Build.SERIAL
        val deviceId = GetDeviceId(context)
        if (uniqueId.isNullOrEmpty()) {
            uniqueId = deviceId.getUniqueId()
        }
        return uniqueId
    }
}
