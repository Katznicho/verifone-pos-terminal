package com.pos

import android.annotation.SuppressLint
import android.telephony.TelephonyManager
import android.content.Context

class GetDeviceId(private val context: Context) {

    private lateinit var telephonyManager: TelephonyManager

    @SuppressLint("MissingPermission")
    fun getUniqueId(): String? {
        telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        /*
         * getDeviceId() returns the unique device ID.
         * For example, the IMEI for GSM and the MEID or ESN for CDMA phones.
         */
        val deviceId: String? = telephonyManager.deviceId
        /*
         * getSubscriberId() returns the unique subscriber ID,
         * For example, the IMSI for a GSM phone.
         */
        val subscriberId: String? = telephonyManager.subscriberId

        return deviceId
    }
}
