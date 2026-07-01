@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.ownstd.project.storage

import com.russhwolf.settings.Settings
import kotlinx.cinterop.*
import platform.CoreFoundation.*
import platform.Security.*

internal class KeychainSettings(
    private val service: String = "com.ownstd.project.storage"
) : Settings {

    override val keys: Set<String> get() = enumerateKeys()
    override val size: Int get() = keys.size
    override fun clear() = keys.forEach { remove(it) }

    override fun remove(key: String) {
        memScoped {
            val query = buildBaseQuery(key)
            SecItemDelete(query)
            CFRelease(query)
        }
    }

    override fun hasKey(key: String): Boolean = readRaw(key) != null

    override fun putString(key: String, value: String) = writeRaw(key, value)
    override fun getString(key: String, defaultValue: String): String = readRaw(key) ?: defaultValue
    override fun getStringOrNull(key: String): String? = readRaw(key)

    override fun putLong(key: String, value: Long) = writeRaw(key, value.toString())
    override fun getLong(key: String, defaultValue: Long): Long = readRaw(key)?.toLongOrNull() ?: defaultValue
    override fun getLongOrNull(key: String): Long? = readRaw(key)?.toLongOrNull()

    override fun putInt(key: String, value: Int) = writeRaw(key, value.toString())
    override fun getInt(key: String, defaultValue: Int): Int = readRaw(key)?.toIntOrNull() ?: defaultValue
    override fun getIntOrNull(key: String): Int? = readRaw(key)?.toIntOrNull()

    override fun putFloat(key: String, value: Float) = writeRaw(key, value.toString())
    override fun getFloat(key: String, defaultValue: Float): Float = readRaw(key)?.toFloatOrNull() ?: defaultValue
    override fun getFloatOrNull(key: String): Float? = readRaw(key)?.toFloatOrNull()

    override fun putDouble(key: String, value: Double) = writeRaw(key, value.toString())
    override fun getDouble(key: String, defaultValue: Double): Double = readRaw(key)?.toDoubleOrNull() ?: defaultValue
    override fun getDoubleOrNull(key: String): Double? = readRaw(key)?.toDoubleOrNull()

    override fun putBoolean(key: String, value: Boolean) = writeRaw(key, value.toString())
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean = readRaw(key)?.toBooleanStrictOrNull() ?: defaultValue
    override fun getBooleanOrNull(key: String): Boolean? = readRaw(key)?.toBooleanStrictOrNull()

    private fun readRaw(key: String): String? = memScoped {
        val query = buildBaseQuery(key)
        CFDictionarySetValue(query, kSecReturnData, kCFBooleanTrue)
        CFDictionarySetValue(query, kSecMatchLimit, kSecMatchLimitOne)

        val resultRef = alloc<CFTypeRefVar>()
        val status = SecItemCopyMatching(query, resultRef.ptr)
        CFRelease(query)

        if (status != errSecSuccess) return null
        val data = resultRef.value as? CFDataRef ?: return null
        val length = CFDataGetLength(data).toInt()
        val ptr = CFDataGetBytePtr(data) ?: return null
        val bytes = ptr.readBytes(length)
        CFRelease(data)
        bytes.decodeToString()
    }

    private fun writeRaw(key: String, value: String) = memScoped {
        val bytes = value.encodeToByteArray()
        val cfData = bytes.usePinned { pinned ->
            CFDataCreate(null, pinned.addressOf(0).reinterpret(), bytes.size.toLong())!!
        }

        val searchQuery = buildBaseQuery(key)
        val exists = SecItemCopyMatching(searchQuery, null) == errSecSuccess

        if (exists) {
            val update = CFDictionaryCreateMutable(null, 1, null, null)!!
            CFDictionarySetValue(update, kSecValueData, cfData)
            SecItemUpdate(searchQuery, update)
            CFRelease(update)
        } else {
            CFDictionarySetValue(searchQuery, kSecValueData, cfData)
            CFDictionarySetValue(searchQuery, kSecAttrAccessible, kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly)
            SecItemAdd(searchQuery, null)
        }

        CFRelease(searchQuery)
        CFRelease(cfData)
    }

    private fun buildBaseQuery(key: String): CFMutableDictionaryRef = memScoped {
        val query = CFDictionaryCreateMutable(null, 4, null, null)!!
        CFDictionarySetValue(query, kSecClass, kSecClassGenericPassword)
        CFDictionarySetValue(query, kSecAttrService, CFStringCreateWithCString(null, service, kCFStringEncodingUTF8))
        CFDictionarySetValue(query, kSecAttrAccount, CFStringCreateWithCString(null, key, kCFStringEncodingUTF8))
        query
    }

    private fun enumerateKeys(): Set<String> = memScoped {
        val query = CFDictionaryCreateMutable(null, 5, null, null)!!
        CFDictionarySetValue(query, kSecClass, kSecClassGenericPassword)
        CFDictionarySetValue(query, kSecAttrService, CFStringCreateWithCString(null, service, kCFStringEncodingUTF8))
        CFDictionarySetValue(query, kSecReturnAttributes, kCFBooleanTrue)
        CFDictionarySetValue(query, kSecMatchLimit, kSecMatchLimitAll)

        val resultRef = alloc<CFTypeRefVar>()
        val status = SecItemCopyMatching(query, resultRef.ptr)
        CFRelease(query)

        if (status != errSecSuccess) return emptySet()
        val array = resultRef.value as? CFArrayRef ?: return emptySet()
        val count = CFArrayGetCount(array).toInt()

        val keys = mutableSetOf<String>()
        for (i in 0 until count) {
            val dict = CFArrayGetValueAtIndex(array, i.toLong()) as? CFDictionaryRef ?: continue
            val account = CFDictionaryGetValue(dict, kSecAttrAccount) as? CFStringRef ?: continue
            CFStringGetCStringPtr(account, kCFStringEncodingUTF8)?.toKString()?.let { keys.add(it) }
        }
        CFRelease(array)
        keys
    }
}
