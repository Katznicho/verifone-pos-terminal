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
import com.newland.sdk.module.rfcard.RFCardPowerOnExtParams
import com.newland.sdk.module.rfcard.RFCardType
import com.newland.sdk.module.rfcard.RFResult
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




class NewLandModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    private val moduleManage: ModuleManage = ModuleManage.getInstance()

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

    // if (printerModule.status != PrinterStatus.NORMAL) {
    //     Log.e("NewLandModule", "Check printer status")
    //     //log printer status
    //     Log.d("NewLandModule", "Printer status: ${printerModule.status}")
    //     showToast("Check printer")
    //     // return
    // }else {
    //     try {
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
//     }
// }


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

// private fun showMessage(message: String, messageType: Int) {
//         Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
//         Log.d("Scanner fragment", message)
//     }


    // private fun generateQRCodeBitmap(url: String): Bitmap {
    //     // Implement QR code generation logic here
    //     // Placeholder function for generating a QR code Bitmap from a URL string
    // }

    private fun generateQRCodeBitmap(url: String): Bitmap {
    // Placeholder return to avoid compilation error
    return Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
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

