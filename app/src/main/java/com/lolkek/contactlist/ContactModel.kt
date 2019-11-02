package com.lolkek.contactlist

data class ContactModel(
    val id: Long,
    val contactId: Long,
    val photoUri: String?,
    val firstName: String?,
    val surname: String?,
    val fullName: String?,
    var phoneNumbers: Set<String> = emptySet()
)