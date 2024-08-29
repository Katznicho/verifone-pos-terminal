package com.pos;
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import android.util.Log
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.Promise
import android.content.ServiceConnection;

class CalendarModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    // add to CalendarModule.kt
override fun getName() = "CalendarModule"



@ReactMethod
  fun createCalendarEvent(name: String, location: String, callback: Callback) {
      val eventId = "12345"
      callback.invoke(eventId)
  }

  @ReactMethod
fun createCalendarEventWithPromise(name: String, location: String, promise: Promise) {
    try {
        val eventId = "12345"
        promise.resolve(eventId)
    } catch (e: Throwable) {
        promise.reject("Create Event Error", e)
    }
}


}