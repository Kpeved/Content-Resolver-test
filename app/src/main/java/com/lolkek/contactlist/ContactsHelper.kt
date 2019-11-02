package com.lolkek.contactlist

import android.content.ContentResolver
import android.provider.ContactsContract
import com.lolkek.utils.getLongValue
import com.lolkek.utils.getStringValue
import io.reactivex.Single

class ContactsHelper(
    private val contentResolver: ContentResolver
) {

    fun getAllContacts(): Single<Map<Long, ContactModel>> {
        return Single.fromCallable { loadAllContacts() }
            .map { addPhoneNumbers(it) }
            .map { it.filter { !it.value.phoneNumbers.isNullOrEmpty() } }
            .map { it.values.toList().groupBy { it.contactId }.mapValues { mergePhoneNumbers(it.value) } }
    }

    private fun mergePhoneNumbers(contacts: List<ContactModel>): ContactModel {
        val first = contacts.first()
        val phoneNumbers = first.phoneNumbers.toMutableSet()
        contacts.forEachIndexed { i, contactModel ->
            if (i > 0) {
                phoneNumbers.addAll(contactModel.phoneNumbers)
            }
        }
        first.phoneNumbers = phoneNumbers
        return first
    }

    private fun loadAllContacts(): Map<Long, ContactModel> {
        val uri = ContactsContract.Data.CONTENT_URI
        val selection = "${ContactsContract.Data.MIMETYPE} = ? "
        val selectionArgs = arrayOf(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)

        val itemsMap = mutableMapOf<Long, ContactModel>()

        contentResolver.query(
            uri,
            getContactProjection(),
            selection,
            selectionArgs,
            getSortString()
        ).use { cursor ->
            if (cursor?.moveToFirst() == true) {
                do {
                    val id = cursor.getLongValue(ContactsContract.Data.RAW_CONTACT_ID)
                    val contactId = cursor.getLongValue(ContactsContract.Data.CONTACT_ID)
                    val photoUri = cursor.getStringValue(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
                    val name = cursor.getStringValue(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val firstName = cursor.getStringValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME)
                    val surname = cursor.getStringValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME)
                    itemsMap[id] = ContactModel(
                        id = id,
                        contactId = contactId,
                        photoUri = photoUri,
                        firstName = firstName,
                        surname = surname,
                        fullName = name
                    )
                } while (cursor.moveToNext())
            }
        }

        return itemsMap
    }

    private fun getContactProjection() = arrayOf(
        ContactsContract.Data.CONTACT_ID,
        ContactsContract.Data.RAW_CONTACT_ID,
        ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
        ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
        ContactsContract.CommonDataKinds.Phone.NUMBER,
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
        ContactsContract.CommonDataKinds.Phone.PHOTO_URI
    )

    private fun getPhoneProjection() = arrayOf(
        ContactsContract.Data.RAW_CONTACT_ID,
        ContactsContract.CommonDataKinds.Phone.NUMBER
    )

    private fun getSortString() = "${ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME} COLLATE NOCASE"

    private fun addPhoneNumbers(contacts: Map<Long, ContactModel>): Map<Long, ContactModel> {
        val phoneNumbers = mutableMapOf<Long, HashSet<String>>()
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = getPhoneProjection()

        contentResolver
            .query(uri, projection, null, null, null)
            .use { cursor ->
                if (cursor?.moveToFirst() == true) {
                    do {
                        val id = cursor.getLongValue(ContactsContract.Data.RAW_CONTACT_ID)
                        val number = cursor.getStringValue(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            ?: continue
                        if (phoneNumbers[id] == null) {
                            phoneNumbers[id] = HashSet()
                        }

                        phoneNumbers[id]?.add(number.replace(" ", ""))
                    } while (cursor.moveToNext())
                }
            }

        phoneNumbers.forEach { (id, items) ->
            contacts[id]?.phoneNumbers = items.toSet()
        }
        return contacts
    }
}
