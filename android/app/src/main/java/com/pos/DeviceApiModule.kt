package com.pos

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import com.facebook.react.bridge.*
import com.vfi.smartpos.deviceservice.aidl.IDeviceService

class DeviceApiModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    companion object {
        // Changed to internal visibility
        @JvmStatic
        var deviceService: IDeviceService? = null

       
    }
    
    private var isFirstRun: Boolean = false
    private val moduleInstance: DeviceApiModule = this

    override fun getName(): String {
        return "DeviceApiModule"
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.e("DeviceApiModule", "${name?.packageName} is disconnected")
            deviceService = null
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.e("DeviceApiModule", "ServiceConnected() [${name?.packageName}]")
            deviceService = IDeviceService.Stub.asInterface(service)
            Log.e("DeviceApiModule", "connected")
            try {
                DeviceHelper.reset()
                DeviceHelper.initDevices()
            } catch (e: RemoteException) {
                e.printStackTrace()
                Log.e("DeviceApiModule", "init devices RemoteException=" + e.message)
            } catch (e: DeviceSecurityException) {
                e.printStackTrace()
                Log.e("DeviceApiModule", "init devices DeviceSecurityException=" + e.message)
            }
            linkToDeath(service)
            if (isFirstRun) {
                isFirstRun = false
            }
        }
    }

    private fun linkToDeath(service: IBinder?) {
        try {
            service?.linkToDeath({
                Log.e("DeviceApiModule", "Service has died")
                deviceService = null
                bindToDeviceService()
            }, 0)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    @ReactMethod
    fun bindToDeviceService() {
        Log.i("DeviceApiModule", "Start service binding, deviceService=" + (deviceService != null))
        if (deviceService == null) {
            val intent = Intent()
            intent.setComponent(ComponentName("com.vfi.smartpos.deviceservice", "com.verifone.smartpos.service.VerifoneDeviceService"))
            if (!reactApplicationContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)) {
                Log.i("DeviceApiModule", "deviceService connection failed")
            } else {
                Log.i("DeviceApiModule", "deviceService successfully connected")
                isFirstRun = true
            }
        }
    }

    @ReactMethod
    fun unbindFromDeviceService() {
        if (deviceService != null) {
            Log.d("DeviceApiModule", "Unbinding service")
            reactApplicationContext.unbindService(serviceConnection)
            deviceService = null
        }
    }

    @ReactMethod
    fun getDeviceId(promise: Promise) {
        try {
            val deviceId = Build.SERIAL
            promise.resolve(deviceId)
        } catch (e: Exception) {
            promise.reject("Get Device ID Error", e)
        }
    }
}
