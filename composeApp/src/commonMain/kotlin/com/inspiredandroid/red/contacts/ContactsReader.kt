package com.inspiredandroid.red.contacts

import com.inspiredandroid.red.data.ContactRecord

expect class ContactsReader() {
    fun isSupported(): Boolean
    fun hasPermission(): Boolean
    suspend fun search(query: String, limit: Int): List<ContactRecord>
}
