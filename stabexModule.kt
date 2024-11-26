package com.pos

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.vfi.smartpos.deviceservice.aidl.IDeviceService
import com.vfi.smartpos.deviceservice.aidl.IPrinter
import com.vfi.smartpos.deviceservice.aidl.IMagCardReader
import com.vfi.smartpos.deviceservice.aidl.IScanner
import com.vfi.smartpos.deviceservice.aidl.IRFCardReader
import com.vfi.smartpos.deviceservice.aidl.IBeeper
import com.vfi.smartpos.deviceservice.aidl.ISmartCardReader

import com.pos.Utility.hexStr2Byte
import com.pos.Utility.byte2HexStr

class StabexModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    private val TAG = "StabexModule"
    private var idevice: IDeviceService? = null
    private var printer: IPrinter? = null
    private var msr: IMagCardReader? = null
    private var scanner: IScanner? = null
    private var irfCardReader: IRFCardReader? = null
    private var mIBeeper: IBeeper? = null
    private var iSmartCardReader: ISmartCardReader? = null


    companion object {
        const val S50_CARD = 0x00
        const val S70_CARD = 0x01
        const val PRO_CARD = 0x02
        const val S50_PRO_CARD = 0x03
        const val S70_PRO_CARD = 0x04
        const val CPU_CARD = 0x05
    }
    

    private val eventEmitter: DeviceEventManagerModule.RCTDeviceEventEmitter?
        get() = reactApplicationContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)

    private val eventListeners = mutableSetOf<String>()

    init {
        bindService()
    }

    override fun getName(): String {
        return "StabexModule"
    }

    private fun bindService() {
        val intent = Intent().apply {
            action = "com.vfi.smartpos.device_service"
            `package` = "com.vfi.smartpos.deviceservice"
        }
        val isSucc = reactApplicationContext.applicationContext.bindService(intent, object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                idevice = IDeviceService.Stub.asInterface(service)
                try {
                    mIBeeper = idevice?.getBeeper()
                    printer = idevice?.getPrinter()
                    msr = idevice?.getMagCardReader()
                    scanner = idevice?.getScanner(0)
                    irfCardReader = idevice?.getRFCardReader()
                    iSmartCardReader = idevice?.getSmartCardReader(0)
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                idevice = null
            }
        }, Context.BIND_AUTO_CREATE)
        if (!isSucc) {
            Log.i(TAG, "Service binding failed!")
        }
    }

    @ReactMethod
    fun doPrintString() {
        printer?.let {
            try {
                val format = Bundle().apply {
                    putInt("FontSize", 24) // Smaller font for details
                }
                val headingFormat = Bundle().apply {
                    putInt("FontSize", 40) // Large font for the header
                    putInt("Alignment", 1) // Center alignment
                }

                val dataHeader = "STABEX"
                val dataToPrint = "\nAGENT-ID: 38624222\n" +
                        "LOCATION: STABEX, KLA\n" +
                        "AGENT   : 25600000000\n" +
                        "\nITEM    : WITHDRAW\n" +
                        "CUST NO : 123456789\n" +
                        "CUSTOMER: NICHOLAS KATENDE\n" +
                        "PHONE   : 25600000000\n" +
                        "DATE    : 13-Aug-2024\n" +
                        "TIME    : 00:08:57\n" +
                        "REF     : 9827264254234\n" +
                        "RECIEPT : 9827264254234\n" +
                        "TOKEN   : \n" +
                        "STATUS  : TRANSACTION SUCCESSFUL\n" +
                        "\nAMOUNT UGX : 10000.00\n" +
                        "FEE UGX    : 0\n" +
                        "EXCISE  UGX: 0\n" +
                        "TOTAL UGX  : 10000.00\n"

                val footerPrint = "\nFor Inquiries call No.s below\n" +
                        "0800150150,0800250250\n" +
                        "0800350350\n\n" +
                        "POWERED BY STABEX\n"

                it.addText(headingFormat, dataHeader)
                it.feedLine(1)
                it.addText(format, dataToPrint)
                it.feedLine(1)
                it.addText(format, footerPrint)
                it.feedLine(3)

                it.startPrint(MyPrinterListener())
            } catch (e: RemoteException) {
                e.printStackTrace()
                Log.e(TAG, "Error printing: ${e.message}")
            }
        } ?: Log.e(TAG, "Printer instance is null")
    }

    @ReactMethod
    fun doMsr() {
        msr?.let {
            try {
                it.searchCard(60, MyMsrListener())
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    @ReactMethod
    fun doScan() {
        scanner?.let {
            try {
                it.startScan(Bundle(), 30, MyScanListener())
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

@ReactMethod
fun doRFID() {
    irfCardReader?.let {
        try {
            // Start searching for the card with the specified listener and timeout
            it.searchCard(MyRFSearchListener(), 20)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    } ?: run {
        // Handle the scenario where RFCardReader instance is not available
        sendEvent("RFCardReaderError", "RFCardReader instance is null")
    }
}

@ReactMethod
fun activateCard(driver: String) {
    irfCardReader?.let { rfCardReader ->
        try {
            val responseData = ByteArray(256) // Allocate a byte array for the response data
            val resultCode = rfCardReader.activate(driver, responseData)
            Log.i(TAG, "Card activation result: $resultCode")
            
            if (resultCode == 0) {
                val responseHex = responseData.joinToString(separator = "") { byte -> "%02X".format(byte) }
                sendEvent("RFCardActivated", "Card activated successfully. Response: $responseHex")
            } else {
                sendEvent("RFCardActivateError", "Failed to activate card, error code: $resultCode")
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
            sendEvent("RFCardActivateException", "Exception occurred during card activation: ${e.message}")
        }
    } ?: run {
        Log.e(TAG, "RFCardReader instance is null")
        sendEvent("RFCardReaderError", "RFCardReader instance is null")
    }
}


    private inner class MyPrinterListener : com.vfi.smartpos.deviceservice.aidl.PrinterListener.Stub() {
        @Throws(RemoteException::class)
        override fun onError(error: Int) {
            sendEvent("PrinterError", "Print error, errno: $error")
        }

        @Throws(RemoteException::class)
        override fun onFinish() {
            sendEvent("PrinterFinish", "Print finished")
        }
    }

  private inner class MyMsrListener : com.vfi.smartpos.deviceservice.aidl.MagCardListener.Stub() {
    @Throws(RemoteException::class)
    override fun onError(error: Int, message: String) {
        sendEvent("MSRError", "Magnetic card error, code: $error ($message)")
    }

    @Throws(RemoteException::class)
    override fun onSuccess(track: Bundle) {
        val track1 = track.getString("track1") ?: "No Track 1 data"
        val track2 = track.getString("track2") ?: "No Track 2 data"
        val track3 = track.getString("track3") ?: "No Track 3 data"

        val trackData = "Track 1: $track1\nTrack 2: $track2\nTrack 3: $track3"
        Log.i(TAG, "Card read successfully: $trackData")  // Log to Android terminal
        sendEvent("MSRSuccess", trackData)  // Send to React Native
    }

    @Throws(RemoteException::class)
    override fun onTimeout() {
        sendEvent("MSRTimeout", "Magnetic card read timeout")
    }
}


    private inner class MyScanListener : com.vfi.smartpos.deviceservice.aidl.ScannerListener.Stub() {
        @Throws(RemoteException::class)
        override fun onSuccess(barcode: String) {
            sendEvent("ScanSuccess", "Scan result: $barcode")
        }

        @Throws(RemoteException::class)
        override fun onError(error: Int, message: String) {
            sendEvent("ScanError", "Scan error, code: $error, message: $message")
        }

        @Throws(RemoteException::class)
        override fun onTimeout() {
            sendEvent("ScanTimeout", "Scan timeout")
        }

        @Throws(RemoteException::class)
        override fun onCancel() {
            sendEvent("ScanCancel", "Scan canceled")
        }
    }

    private inner class MyRFSearchListener : com.vfi.smartpos.deviceservice.aidl.RFSearchListener.Stub() {
        @Throws(RemoteException::class)
        override fun onCardPass(cardType: Int) {
            val cardTypeName = when (cardType) {
                0x00 -> "S50_CARD (MIFARE card)"
                0x01 -> "S70_CARD (MIFARE card)"
                0x02 -> "PRO_CARD"
                0x03 -> "S50_PRO_CARD"
                0x04 -> "S70_PRO_CARD"
                0x05 -> "CPU_CARD (Contactless card)"
                else -> "Unknown Card Type"
            }
    
            Log.i(TAG, "Card detected, type: $cardTypeName ($cardType)")
            readRFData()  
            sendEvent("RFCardPass", "Card detected, type: $cardTypeName")
        }
    
        @Throws(RemoteException::class)
        override fun onFail(error: Int, message: String) {
            sendEvent("RFSearchFail", "RF card search failed, error: $error, message: $message")
        }
    }
    

    private fun sendEvent(eventName: String, message: String) {
        val reactContext = reactApplicationContext
        if (reactContext.hasActiveCatalystInstance()) {
            eventEmitter?.emit(eventName, message)
        }
    }

    @ReactMethod
    fun addListener(eventName: String) {
        eventListeners.add(eventName)
    }

    @ReactMethod
    fun removeListeners(count: Int) {
        eventListeners.clear()
    }



    fun readRFData() {
        irfCardReader?.let { rfCardReader ->
            val key = ByteArray(6) { 0xFF.toByte() }
            val defaultKeyB = hexStr2Byte("FFFFFFFFFFFF")
            var cardNumber: String? = null
    
            try {
                for (i in 0 until 2) {
                    // Attempt to authenticate sector using the default key B
                    val ret = rfCardReader.authSector(i, 1, defaultKeyB)
    
                    if (ret != 0) {
                        Log.d(TAG, "Sector $i authentication failed: $ret")
                        continue
                    }
    
                    Log.d(TAG, "Sector $i authentication successful: $ret")
    
                    // Read data from blocks of the authenticated sector
                    val sectorData = readBlocks(i * 4)
                    Log.d("CombinedSectorData", sectorData)
    
                    // Convert sector data from hex to ASCII
                    val sectorDataInAscii = hexToAscii(sectorData)
                    Log.d(TAG, "Sector data in ASCII: $sectorDataInAscii")
    
                    // Extract card number from the sector data
                    val extractedCardNumber = extractCardNumber(sectorDataInAscii)
                    if (!extractedCardNumber.isNullOrEmpty()) {
                        cardNumber = extractedCardNumber
                        break // Exit loop if a valid card number is found
                    }
                }
    
                // Send event with the card number if found, or an error event if not
                if (cardNumber != null) {
                    Log.d(TAG, "Extracted card number: $cardNumber")
                    sendEvent("RFCardNumber", cardNumber) // Sending only the card number without prefix
                } else {
                    sendEvent("RFCardNumberError", "Card number not found")
                }
            } catch (e: RemoteException) {
                // Handle RemoteException and send error event
                e.printStackTrace()
                sendEvent("RFCardReaderException", "Exception occurred during card reading: ${e.message}")
            }
        } ?: run {
            // Handle case where RFCardReader instance is null
            Log.e(TAG, "RFCardReader instance is null")
            sendEvent("RFCardReaderError", "RFCardReader instance is null")
        }
    }
    


private fun readBlocks(blockNumberOffset: Int): String {
    val sectorData = StringBuilder()
    for (j in 0 until 4) {
     

        val buffer = ByteArray(16)
        val ret = irfCardReader?.readBlock(j + blockNumberOffset, buffer)

        if (ret == 0) {
            sectorData.append(byte2HexStr(buffer))
            Log.d(TAG, "Read data for Block(${j + blockNumberOffset}): success: ${byte2HexStr(buffer)}")
        } else {
            Log.d(TAG, "Read data failed for Block(${j + blockNumberOffset}): $ret")
        }
    }
    return sectorData.toString()
}

private fun extractCardNumber(input: String): String? {
    val regex = "SU\\d+".toRegex()
    val matchResult = regex.find(input)
    return matchResult?.value
}

private fun hexToAscii(hexString: String): String {
    val asciiStringBuilder = StringBuilder()
    for (i in hexString.indices step 2) {
        val hexByte = hexString.substring(i, i + 2)
        val decimal = hexByte.toInt(16)
        asciiStringBuilder.append(decimal.toChar())
    }
    return asciiStringBuilder.toString()
}

}
