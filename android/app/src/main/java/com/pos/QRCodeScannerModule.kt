package com.pos

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView

class QRCodeScannerModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    private var resultCallback: Callback? = null
    private var mScannerView: ZXingScannerView? = null

    override fun getName(): String {
        return "QRCodeScannerModule"
    }

    @ReactMethod
    fun startScanner(callback: Callback) {
        val activity = currentActivity
        if (activity != null) {
            resultCallback = callback

            // Check if the app has camera permission
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_REQUEST_CODE
                )
                callback.invoke("Camera permission is required")
                return
            }

            // Create and set up the ZXingScannerView
            mScannerView = ZXingScannerView(activity)
            val layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            mScannerView?.layoutParams = layoutParams

            val frameLayout = FrameLayout(activity)
            frameLayout.addView(mScannerView)

            // Ensure UI changes are done on the main thread
            activity.runOnUiThread {
                // Add the scanner view to the current activity's layout (use existing activity's root view)
                val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
                rootView.addView(frameLayout)

                // Set scanner view's result handler
                mScannerView?.setResultHandler(object : ZXingScannerView.ResultHandler {
                    override fun handleResult(result: Result) {
                        val scannedQRCode = result.text
                        Log.d("QRCodeScanner", "Scanned QR Code: $scannedQRCode")
                        // Call the callback with the scanned QR code
                        resultCallback?.invoke(scannedQRCode)

                        // Stop the scanner view after scanning
                        mScannerView?.stopCamera()
                    }
                })

                // Start the scanner view
                mScannerView?.startCamera()
            }
        } else {
            callback.invoke("Activity is not available")
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1
    }
}
