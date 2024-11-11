package com.pos

import android.util.Log
import android.widget.Toast
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Callback
import com.newland.sdk.ModuleManage
import com.newland.sdk.module.light.LightColor
import com.newland.sdk.module.rfcard.RFCardPowerOnExtParams
import com.newland.sdk.module.rfcard.RFCardType
import com.newland.sdk.module.rfcard.RFResult
import com.newland.sdk.module.scanner.Scanner
import com.newland.sdk.module.scanner.ScannerListener
import com.newland.sdk.module.scanner.ScannerType
import com.newland.sdk.module.scanner.ScannerExtParams
import com.newland.sdk.utils.ISOUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class NewLandModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    private val scanner: Scanner? = ModuleManage.getInstance().scannerModule

    override fun getName(): String {
        return "NewLandModule"
    }

    @ReactMethod
    fun showToast(message: String) {
        Log.d("NewLandModule", "showToast called with message: $message")
        Toast.makeText(reactApplicationContext, message, Toast.LENGTH_SHORT).show()
    }

    @ReactMethod
    fun initNfcAuth(callback: Callback) {
        Log.d("NewLandModule", "initNfcAuth called")
        GlobalScope.launch(Dispatchers.IO) {
            val isManuallyLoggedIn = false
            if (isManuallyLoggedIn) return@launch

            val moduleManage = ModuleManage.getInstance()

            try {
                Log.d("NewLandModule", "Initializing NFC module")
                moduleManage.init(reactApplicationContext)
            } catch (e: Exception) {
                Log.e("NewLandModule", "Error initializing NFC module: ${e.message}")
                callback.invoke("Error initializing NFC module: ${e.message}")
                return@launch
            }

            val cardTypeList: MutableList<RFCardType> = ArrayList()
            cardTypeList.add(RFCardType.ACARD)
            cardTypeList.add(RFCardType.BCARD)
            cardTypeList.add(RFCardType.M1CARD)
            cardTypeList.add(RFCardType.M0CARD)
            cardTypeList.add(RFCardType.FELICA_CARD)

            val timeout = 60
            val rfCardPowerOnExtParams: RFCardPowerOnExtParams? = null
            val rfCardModule = try {
                Log.d("NewLandModule", "Getting RFCardModule")
                moduleManage.rfCardModule
            } catch (e: NullPointerException) {
                Log.e("NewLandModule", "Error getting RFCardModule: ${e.message}")
                callback.invoke("Error getting RFCardModule: ${e.message}")
                return@launch
            }

            var retryCount = 0
            val maxRetries = 3
            var callbackInvoked = false
            while (retryCount < maxRetries) {
                try {
                    Log.d("NewLandModule", "Attempting to power on RF card")
                    val rfResult: RFResult = rfCardModule.powerOn(
                        cardTypeList.toTypedArray(),
                        timeout,
                        rfCardPowerOnExtParams
                    )
                    withContext(Dispatchers.Main) {
                        Log.d("NewLandModule", "RF card powered on, processing result")
                        handleRfResult(rfResult, callback)
                        callbackInvoked = true
                    }
                    break
                } catch (e: Exception) {
                    retryCount++
                    if (retryCount >= maxRetries && !callbackInvoked) {
                        Log.e("NewLandModule", "Failed to authenticate NFC card after $maxRetries attempts: ${e.message}")
                        withContext(Dispatchers.Main) {
                            callback.invoke("Failed to authenticate NFC card after $maxRetries attempts: ${e.message}")
                            callbackInvoked = true
                        }
                    }
                }
            }
        }
    }

    private fun handleRfResult(rfResult: RFResult, callback: Callback) {
        Log.d("NewLandModule", "Handling NFC authentication result")
        if (rfResult.snr == null) {
            Log.d("NewLandModule", "RF SN is null")
            callback.invoke("RF SN: null")
        } else {
            val snrHex = ISOUtils.hexString(rfResult.snr)
            Log.d("NewLandModule", "RF SN: $snrHex")
            callback.invoke("RF SN: $snrHex")

            if (rfResult.rfcardType != null) {
                Log.d("NewLandModule", "RF Card Type: ${rfResult.rfcardType}")
                callback.invoke("RF Card Type: ${rfResult.rfcardType}")

                if (rfResult.rfcardType == RFCardType.M1CARD) {
                    val moduleManage = ModuleManage.getInstance()
                    Log.d("NewLandModule", "M1 Card detected, playing buzzer and blinking lights")
                    moduleManage.buzzerModule.play(1, 1, 1)
                    moduleManage.indicatorLightModule.blinkLight(
                        arrayOf(LightColor.RED, LightColor.BLUE, LightColor.YELLOW), 1, 1
                    )
                }
            }
        }
    }

    @ReactMethod
    fun startScanner(callback: Callback) {
        val scannerExtParams = ScannerExtParams()
        scannerExtParams.isOnce = true

        scanner?.startScan(
            reactApplicationContext,
            ScannerType.FRONT,
            null,
            10,
            object : ScannerListener {
                override fun onTimeout() {
                    callback.invoke("Scanner timeout")
                }

                override fun onResponse(scanResults: Array<String>) {
                    if (scanResults.isNotEmpty()) {
                        val scannedQRCode = scanResults[0]
                        callback.invoke(scannedQRCode)
                    } else {
                        callback.invoke("No QR Code found")
                    }
                }

                override fun onFinish() {
                    // Optional: Notify scan finish
                }

                override fun onError(errorCode: Int, message: String) {
                    callback.invoke("Scanner error: $message")
                }

                override fun onCancel() {
                    callback.invoke("Scanner cancelled")
                }
            },
            scannerExtParams
        )
    }
}
