package com.inspiredandroid.red.contacts

import com.inspiredandroid.red.data.ContactRecord

actual class ContactsReader actual constructor() {
    actual fun isSupported(): Boolean = false
    actual fun hasPermission(): Boolean = false
    actual suspend fun search(query: String, limit: Int): List<ContactRecord> = emptyList()
}
