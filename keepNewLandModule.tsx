package com.pos

import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Callback
import com.newland.sdk.ModuleManage
import com.newland.sdk.module.light.LightColor



import com.newland.sdk.module.scanner.ScannerModule
import com.newland.sdk.module.scanner.ScannerListener
import com.newland.sdk.module.scanner.ScannerType
import com.newland.sdk.module.scanner.ScannerExtParams
import com.newland.sdk.utils.ISOUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import com.newland.sdk.module.printer.Alignment
import com.newland.sdk.module.printer.EnFontSize
import com.newland.sdk.module.printer.ErrorCode
import com.newland.sdk.module.printer.FontScale
import com.newland.sdk.module.printer.ImageFormat
import com.newland.sdk.module.printer.PrintListener
import com.newland.sdk.module.printer.PrintScriptUtil
import com.newland.sdk.module.printer.PrinterStatus
import com.newland.sdk.module.printer.TextFormat
import com.newland.sdk.module.printer.ZhFontSize

// //new imports
import com.newland.sdk.module.rfcard.RFCardType
// import com.newland.sdk.ModuleManage
// import com.newland.sdk.module.light.LightColor
import com.newland.sdk.module.rfcard.RFCardModule
import com.newland.sdk.module.rfcard.RFCardPowerOnExtParams
// import com.newland.sdk.module.rfcard.RFCardType
import com.newland.sdk.module.rfcard.RFKeyMode
import com.newland.sdk.module.rfcard.RFResult 
import com.newland.sdk.mtype.log.DeviceLogger
import com.newland.sdk.mtype.log.DeviceLoggerFactory.init
//new imports




class NewLandModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    private val moduleManage: ModuleManage = ModuleManage.getInstance()
    private lateinit var rfCardModule: RFCardModule

    init {
        try {
            moduleManage.init(reactContext)
            rfCardModule = moduleManage.rfCardModule
        } catch (e: Exception) {
            Log.e("NewLandModule", "Failed to initialize RFCardModule: ${e.message}")
        }
    }

    override fun getName(): String {
        return "NewLandModule"
    }

    @ReactMethod
    fun showToast(message: String) {
        Toast.makeText(reactApplicationContext, message, Toast.LENGTH_SHORT).show()
    }

    // NFC and scanner functionality (existing code) ...

    // New printer functionality
@ReactMethod
fun initializeTRAPrinter() {
    moduleManage.init(reactApplicationContext)
    val printerModule = moduleManage.printerModule
    val printScriptUtil = printerModule.getPrintScriptUtil(reactApplicationContext)

    if (printerModule.status != PrinterStatus.NORMAL) {
        Log.e("NewLandModule", "Check printer status")
        //log printer status
        Log.d("NewLandModule", "Printer status: ${printerModule.status}")
        showToast("Check printer")
        // return
    }else {
        try {
        val fontName = "FreeMonoBold.ttf"
        printScriptUtil.addFont(reactApplicationContext, fontName)

        val leftAlignFormat = TextFormat().apply {
            alignment = Alignment.LEFT
            fontScale = FontScale.ORINARY
            zhFontSize = ZhFontSize.FONT_24x24USER
            enFontSize = EnFontSize.FONT_12x24A
            isLinefeed = true
        }

        val titleFormat = TextFormat().apply {
            alignment = Alignment.CENTER
            fontScale = FontScale.ORINARY
            zhFontSize = ZhFontSize.FONT_24x24USER
            enFontSize = EnFontSize.FONT_12x24A
            isLinefeed = true
        }

        val receiptTitle = "TAX INVOICE"
        printScriptUtil.addText(titleFormat, receiptTitle)
        printScriptUtil.setLineSpacing(20)

        // Dummy data for the station
        val stationName = "Station XYZ"
        val stationPin = "1234567890"
        val stationPhone = "+254712345678"
        printScriptUtil.addFormattedText("", stationName, 15, 12)
        printScriptUtil.addFormattedText("TRA PIN:", stationPin, 15, 12)
        printScriptUtil.addFormattedText("TEL:", stationPhone, 15, 12)
        printScriptUtil.setLineSpacing(5)

        // Dummy data for the transaction
        val transactionId = "TXN123456789"
        val buyerPin = "789123456"
        val buyerName = "John Doe"
        printScriptUtil.addFormattedText("TRANSACTION ID:", transactionId, 15, 12)
        printScriptUtil.addFormattedText("BUYER PIN:", buyerPin, 15, 12)
        printScriptUtil.addFormattedText("BUYER NAME:", buyerName, 15, 12)

        // Dummy receipt content (items, amounts, etc.)
        printScriptUtil.addFormattedText("ITEM 1:", "Product A", 15, 12)
        printScriptUtil.addFormattedText("Amount:", "KES 500.00", 15, 12)
        printScriptUtil.addFormattedText("ITEM 2:", "Product B", 15, 12)
        printScriptUtil.addFormattedText("Amount:", "KES 200.00", 15, 12)
        printScriptUtil.addFormattedText("TOTAL:", "KES 700.00", 15, 12)

        printScriptUtil.addDottedLine()

        // Generate and add the QR code (dummy URL)
        val qrCodeUrl = "https://example.com/transaction/$transactionId"
        val qrCodeBitmap = generateQRCodeBitmap(qrCodeUrl)
        val formatQRCode = ImageFormat().apply {
            alignment = Alignment.CENTER
            width = 200
            height = 200
        }
        printScriptUtil.addImage(formatQRCode, qrCodeBitmap)

        // Final message
        printScriptUtil.addText(titleFormat, "Thank You Visit Again")
        printScriptUtil.addText(titleFormat, "Powered by Pesapal")
        printScriptUtil.setLineSpacing(40)
        printScriptUtil.addDottedLine()

        // Print action
        printScriptUtil.print(object : PrintListener {
            override fun onSuccess() {
                showToast("Print successful")
            }

            override fun onError(error: ErrorCode, msg: String) {
                showToast("Print error: $msg : errorCode $error")
            }
        })
    } catch (ex: Exception) {
        Log.e("NewLandModule", "Print exception: ${ex.message}")
    }
    }
}


    // Extension function to add formatted text
fun PrintScriptUtil.addFormattedText(leftText: String, rightText: String, maxLeftWidth: Int, maxRightWidth: Int) {
    val totalWidth = maxLeftWidth + maxRightWidth
    val spaceBetween = totalWidth - leftText.length - rightText.length
    val formattedText = when {
        spaceBetween > 0 -> "$leftText${" ".repeat(spaceBetween)}$rightText"
        else -> "$leftText $rightText".take(totalWidth)
    }
    this.addText(
        TextFormat().apply {
            alignment = Alignment.LEFT
            fontScale = FontScale.ORINARY
            zhFontSize = ZhFontSize.FONT_24x24USER
            enFontSize = EnFontSize.FONT_12x24A
            isLinefeed = true
        }, formattedText
    )
}



    private fun generateQRCodeBitmap(url: String): Bitmap {
    // Placeholder return to avoid compilation error
    return Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
}

  @ReactMethod
fun authenticateM1CardDefault(callback: Callback) {
    try {
        // Define default NFC keys (Key A and Key B)
        val defaultKeyA = byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte())
        val defaultKeyB = byteArrayOf(0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte())

        // Define default block and UID (modify as per your application's logic)
        val defaultBlock = 4 // Example default block
        val defaultUid = byteArrayOf(0x04.toByte(), 0xAB.toByte(), 0xCD.toByte(), 0xEF.toByte()) // Example UID

        // Attempt to authenticate using Key A
        try {
            val resultA = rfCardModule.m1Authenticate(RFKeyMode.KEYA_0X00, defaultKeyA, defaultBlock, defaultUid)
            callback.invoke(null, "Authentication successful with Key A")
            return
        } catch (e: Exception) {
            Log.e("NewLandModule", "Authentication with Key A failed: ${e.message}")
        }

        // If Key A fails, attempt Key B
        try {
            val resultB = rfCardModule.m1Authenticate(RFKeyMode.KEYB_0X01, defaultKeyB, defaultBlock, defaultUid)
            callback.invoke(null, "Authentication successful with Key B")
            return
        } catch (e: Exception) {
            Log.e("NewLandModule", "Authentication with Key B failed: ${e.message}")
        }

        // If both keys fail
        callback.invoke("Authentication failed with default keys", null)
    } catch (e: Exception) {
        Log.e("NewLandModule", "Authentication failed: ${e.message}")
        callback.invoke("Authentication failed: ${e.message}", null)
    }
}


    @ReactMethod
    fun readM1CardBlock(block: Int, callback: Callback) {
        try {
            val data = rfCardModule.m1ReadBlockData(block)
            callback.invoke(null, ISOUtils.hexString(data))
        } catch (e: Exception) {
            Log.e("NewLandModule", "Read block data failed: ${e.message}")
            callback.invoke("Read block data failed: ${e.message}", null)
        }
    }

    @ReactMethod
    fun writeM1CardBlock(block: Int, data: String, callback: Callback) {
        try {
            val dataBytes = data.toByteArray()
            val result = rfCardModule.m1WriteBlockData(block, dataBytes)
            callback.invoke(null, result)
        } catch (e: Exception) {
            Log.e("NewLandModule", "Write block data failed: ${e.message}")
            callback.invoke("Write block data failed: ${e.message}", null)
        }
    }

    @ReactMethod
    fun powerOffCard(callback: Callback) {
        try {
            rfCardModule.powerOff()
            callback.invoke(null, true)
        } catch (e: Exception) {
            Log.e("NewLandModule", "Power off failed: ${e.message}")
            callback.invoke("Power off failed: ${e.message}", null)
        }
    }

//     @ReactMethod
// fun powerOnCard(callback: Callback) {
//     try {
//         val cardInfo = rfCardModule.powerOn(RFCardType.M1, RFCardPowerOnExtParams())
//         callback.invoke(null, ISOUtils.hexString(cardInfo.uid))
//     } catch (e: Exception) {
//         Log.e("NewLandModule", "Power on failed: ${e.message}")
//         callback.invoke("Power on failed: ${e.message}", null)
//     }
// }

private fun handleCardError(e: Exception, callback: Callback) {
    Log.e("NewLandModule", "Error: ${e.message}")
    callback.invoke(e.message, null)
}


}


// Add at the top or bottom of NewLandModule.kt
data class TraTaxInvoiceResponse(
    val station: Station?,
    val pin: String?,
    val phone: String?,
    val reference: String?,
    val buyer: Buyer?,
    val paymentMethod: String?,
    val timestamp: String?,
    val pump: Int?,
    val nozzle: Int?,
    val grade: Grade?,
    val vehicleNo: String?,
    val price: String?,
    val volume: String?,
    val total: String?,
    val rctvNumber: String?,
    val deviceSerialNumber: String?
)

data class Station(val name: String?, val pin: String?, val phone: String?)
data class Buyer(val pin: String?, val name: String?)
data class Grade(val name: String?, val abbrev: String?)




