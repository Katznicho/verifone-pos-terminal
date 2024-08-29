package com.pos

class DeviceSecurityException(message: String) : Exception(message) {
    companion object {
        private const val serialVersionUID: Long = 1
    }
}
