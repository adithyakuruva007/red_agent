package com.inspiredandroid.red.contacts

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.inspiredandroid.red.data.ContactRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.inject

actual class ContactsReader actual constructor() {
    private val context: Context by inject(Context::class.java)

    actual fun isSupported(): Boolean = true

    actual fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    actual suspend fun search(query: String, limit: Int): List<ContactRecord> {
        if (!hasPermission()) return emptyList()
        return withContext(Dispatchers.IO) {
            val contentResolver = context.contentResolver
            val uri = ContactsContract.Contacts.CONTENT_URI

            val selection = if (query.isNotBlank()) {
                "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} LIKE ?"
            } else {
                null
            }
            val selectionArgs = if (query.isNotBlank()) {
                arrayOf("%$query%")
            } else {
                null
            }

            val sortOrder = "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} ASC"

            val cursor = contentResolver.query(
                uri,
                arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY, ContactsContract.Contacts.HAS_PHONE_NUMBER),
                selection,
                selectionArgs,
                sortOrder
            ) ?: return@withContext emptyList()

            val contacts = mutableListOf<ContactRecord>()
            cursor.use {
                val idIdx = it.getColumnIndex(ContactsContract.Contacts._ID)
                val nameIdx = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
                val hasPhoneIdx = it.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)

                while (it.moveToNext() && contacts.size < limit) {
                    val id = it.getString(idIdx)
                    val name = it.getString(nameIdx) ?: continue
                    val hasPhone = it.getInt(hasPhoneIdx) > 0

                    val phoneNumbers = mutableListOf<String>()
                    if (hasPhone) {
                        contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                            arrayOf(id),
                            null
                        )?.use { phoneCursor ->
                            val numberIdx = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            while (phoneCursor.moveToNext()) {
                                val number = phoneCursor.getString(numberIdx)
                                if (!number.isNullOrBlank()) {
                                    phoneNumbers.add(number)
                                }
                            }
                        }
                    }

                    val emails = mutableListOf<String>()
                    contentResolver.query(
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                        arrayOf(ContactsContract.CommonDataKinds.Email.ADDRESS),
                        "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
                        arrayOf(id),
                        null
                    )?.use { emailCursor ->
                        val emailIdx = emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
                        while (emailCursor.moveToNext()) {
                            val email = emailCursor.getString(emailIdx)
                            if (!email.isNullOrBlank()) {
                                emails.add(email)
                            }
                        }
                    }

                    contacts.add(
                        ContactRecord(
                            id = id,
                            displayName = name,
                            phoneNumbers = phoneNumbers,
                            emails = emails
                        )
                    )
                }
            }
            contacts
        }
    }
}
