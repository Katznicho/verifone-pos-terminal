package com.pos

import android.annotation.SuppressLint
import android.app.Application
import android.os.RemoteException
import android.util.Log
import com.vfi.smartpos.deviceservice.aidl.*

object DeviceHelper {
    private var beeper: IBeeper? = null
    private var deviceInfo: IDeviceInfo? = null
    private var insertCardReader: IInsertCardReader? = null
    private var lastCameraId = 1
    private var led: ILed? = null
    private var magCardReader: IMagCardReader? = null
    private var pboc: IPBOC? = null
    private var pinpad: IPinpad? = null
    private var printer: IPrinter? = null
    private var rfCardReader: IRFCardReader? = null
    private var scanner: IScanner? = null
    private var serialPort: ISerialPort? = null
    private var iemv: IEMV? = null

    @SuppressLint("NewApi")
    @Throws(RemoteException::class, DeviceSecurityException::class)
    fun initDevices() {
        if (DeviceApiModule.deviceService == null) {
            Log.i(AppConstant.TAG, "initDevices deviceService == null")
            throw RemoteException()
        } else {
            try {
                pinpad = getPinpad()
                pboc = getPBOC()
                printer = getPrinter()
                deviceInfo = getDeviceInfo()
                serialPort = getSerialPort()
                scanner = getScanner(0)
                magCardReader = getMagCardReader()
                insertCardReader = getInsertCardReader()
                rfCardReader = getRFCardReader()
            } catch (e: RemoteException) {
                Log.i(AppConstant.TAG, "deviceService RemoteException")
                e.printStackTrace()
                throw e
            }
        }
    }

    fun rebindDeviceService() {
        Log.e(AppConstant.TAG, "rebindDeviceService")
        reset()
    }

    @Throws(RemoteException::class)
    fun checkState(): Boolean {
        return if (DeviceApiModule.deviceService == null) {
            Log.e(AppConstant.TAG, "checkState deviceService == null")
            false
        } else {
            true
        }
    }

    @SuppressLint("NewApi")
    @Throws(RemoteException::class, DeviceSecurityException::class)
    fun getPinpad(): IPinpad? {
        pinpad?.let { return it }
        if (!checkState()) return null
        return try {
            pinpad = DeviceApiModule.deviceService?.getPinpad(5)
            pinpad
        } catch (e: RemoteException) {
            throw RemoteException("Get Pinpad service failed, Please try again later")
        } catch (e: SecurityException) {
            e.printStackTrace()
            throw DeviceSecurityException(e.message ?: "Unknown error")
        }
    }

    @Throws(RemoteException::class, DeviceSecurityException::class)
    fun getPBOC(): IPBOC? {
        pboc?.let { return it }
        if (!checkState()) return null
        return try {
            pboc = DeviceApiModule.deviceService?.getPBOC()
            pboc
        } catch (e: RemoteException) {
            throw RemoteException("Get PBOC service failed, Please try again later")
        } catch (e: SecurityException) {
            e.printStackTrace()
            throw DeviceSecurityException(e.message ?: "Unknown error")
        }
    }

    @Throws(RemoteException::class, DeviceSecurityException::class)
    fun getPrinter(): IPrinter? {
        printer?.let { return it }
        if (!checkState()) return null
        return try {
            printer = DeviceApiModule.deviceService?.getPrinter()
            printer
        } catch (e: RemoteException) {
            throw RemoteException("Get Printer service failed, Please try again later")
        } catch (e: SecurityException) {
            e.printStackTrace()
            throw DeviceSecurityException(e.message ?: "Unknown error")
        }
    }

    @Throws(RemoteException::class, DeviceSecurityException::class)
    fun getDeviceInfo(): IDeviceInfo? {
        deviceInfo?.let { return it }
        if (!checkState()) return null
        return try {
            deviceInfo = DeviceApiModule.deviceService?.getDeviceInfo()
            deviceInfo
        } catch (e: RemoteException) {
            throw RemoteException("Get deviceInfo service failed, Please try again later")
        } catch (e: SecurityException) {
            e.printStackTrace()
            throw DeviceSecurityException(e.message ?: "Unknown error")
        }
    }

    @Throws(RemoteException::class, DeviceSecurityException::class)
    fun getRFCardReader(): IRFCardReader? {
        rfCardReader?.let { return it }
        if (!checkState()) return null
        return try {
            rfCardReader = DeviceApiModule.deviceService?.getRFCardReader()
            rfCardReader
        } catch (e: RemoteException) {
            throw RemoteException("Get RFcard reader service failed, Please try again later")
        } catch (e: SecurityException) {
            e.printStackTrace()
            throw DeviceSecurityException(e.message ?: "Unknown error")
        }
    }

    @Throws(RemoteException::class, DeviceSecurityException::class)
    fun getInsertCardReader(): IInsertCardReader? {
        insertCardReader?.let { return it }
        if (!checkState()) return null
        return try {
            insertCardReader = DeviceApiModule.deviceService?.getInsertCardReader(0)
            insertCardReader
        } catch (e: RemoteException) {
            throw RemoteException("Get InsertCard reader service failed, Please try again later")
        } catch (e: SecurityException) {
            e.printStackTrace()
            throw DeviceSecurityException(e.message ?: "Unknown error")
        }
    }

    @Throws(RemoteException::class, DeviceSecurityException::class)
    fun getIemv(): IEMV? {
        iemv?.let { return it }
        if (!checkState()) return null
        return try {
            iemv = DeviceApiModule.deviceService?.getEMV()
            iemv
        } catch (e: RemoteException) {
            throw RemoteException("Getting EMV failed.")
        } catch (e: SecurityException) {
            e.printStackTrace()
            throw DeviceSecurityException(e.message ?: "Unknown error")
        }
    }

    @Throws(RemoteException::class, DeviceSecurityException::class)
    fun getMagCardReader(): IMagCardReader? {
        magCardReader?.let { return it }
        if (!checkState()) return null
        return try {
            magCardReader = DeviceApiModule.deviceService?.getMagCardReader()
            magCardReader
        } catch (e: RemoteException) {
            throw RemoteException("Get Magcard reader service failed, Please try again later")
        } catch (e: SecurityException) {
            e.printStackTrace()
            throw DeviceSecurityException(e.message ?: "Unknown error")
        }
    }

    @Throws(RemoteException::class, DeviceSecurityException::class)
    fun getScanner(cameraId: Int): IScanner? {
        if (scanner != null && lastCameraId == cameraId) return scanner
        if (!checkState()) return null
        return try {
            lastCameraId = cameraId
            scanner = DeviceApiModule.deviceService?.getScanner(cameraId)
            scanner
        } catch (e: RemoteException) {
            throw RemoteException("Get Scanner service failed, Please try again later")
        } catch (e: SecurityException) {
            e.printStackTrace()
            throw DeviceSecurityException(e.message ?: "Unknown error")
        }
    }

    @Throws(RemoteException::class, DeviceSecurityException::class)
    fun getBeeper(): IBeeper? {
        beeper?.let { return it }
        if (!checkState()) return null
        return try {
            beeper = DeviceApiModule.deviceService?.getBeeper()
            beeper
        } catch (e: RemoteException) {
            throw RemoteException("Get Beeper service failed, Please try again later")
        } catch (e: SecurityException) {
            e.printStackTrace()
            throw DeviceSecurityException(e.message ?: "Unknown error")
        }
    }

    @Throws(RemoteException::class, DeviceSecurityException::class)
    fun getLed(): ILed? {
        led?.let { return it }
        if (!checkState()) return null
        return try {
            led = DeviceApiModule.deviceService?.getLed()
            led
        } catch (e: RemoteException) {
            Log.i(AppConstant.TAG, "getLed(), RemoteException=${e.message}")
            throw RemoteException("Get Led service failed, Please try again later")
        } catch (e: SecurityException) {
            e.printStackTrace()
            Log.i(AppConstant.TAG, "getLed(), SecurityException=${e.message}")
            throw DeviceSecurityException(e.message ?: "Unknown error")
        }
    }

    @Throws(RemoteException::class, DeviceSecurityException::class)
    fun getSerialPort(): ISerialPort? {
        serialPort?.let { return it }
        if (!checkState()) return null
        return try {
            serialPort = DeviceApiModule.deviceService?.getSerialPort("usb-rs232")
            serialPort
        } catch (e: RemoteException) {
            throw RemoteException("Get serialPort service failed, Please try again later")
        } catch (e: SecurityException) {
            e.printStackTrace()
            throw DeviceSecurityException(e.message ?: "Unknown error")
        }
    }

    fun reset() {
        pinpad = null
        pboc = null
        printer = null
        deviceInfo = null
        serialPort = null
        scanner = null
        magCardReader = null
        insertCardReader = null
        rfCardReader = null
    }
}
