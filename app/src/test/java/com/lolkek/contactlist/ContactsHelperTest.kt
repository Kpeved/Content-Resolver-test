package com.lolkek.contactlist

import android.content.ContentResolver
import android.provider.ContactsContract
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.same
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.fakes.RoboCursor

@RunWith(RobolectricTestRunner::class)
class ContactsHelperTest {

    private lateinit var allContactsRoboCursor: RoboCursor
    private lateinit var phoneNumbersRoboCursor: RoboCursor
    private lateinit var contentResolver: ContentResolver

    @Before
    fun setUp() {
        allContactsRoboCursor = RoboCursor()
        phoneNumbersRoboCursor = RoboCursor()

        contentResolver = mock {
            on {
                query(
                    same(ContactsContract.CommonDataKinds.Phone.CONTENT_URI),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull()
                )
            } doReturn phoneNumbersRoboCursor
            on {
                query(
                    same(ContactsContract.Data.CONTENT_URI),
                    anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull()
                )
            } doReturn allContactsRoboCursor
        }
        allContactsRoboCursor.setColumnNames(CONTACTS_COLUMNS)
        phoneNumbersRoboCursor.setColumnNames(PHONES_COLUMNS)
    }

    @Test
    fun `get full contacts list`() {
        allContactsRoboCursor.setResults(
            arrayOf(
                CONTACT_1_FULL,
                CONTACT_2_FULL,
                CONTACT_3_EMPTY,
                CONTACT_4_EMPTY,
                CONTACT_5_THE_SAME_CONTACT_ID
            )
        )
        phoneNumbersRoboCursor.setResults(
            arrayOf(
                PHONE_1_CONTACT_1,
                PHONE_1_CONTACT_3_OK,
                PHONE_2_CONTACT_1,
                PHONE_2_CONTACT_2_OK,
                PHONE_1_CONTACT_2_EMPTY,
                PHONE_1_CONTACT_4_EMPTY,
                PHONE_1_CONTACT_5_OK
            )
        )
        val testObserver = ContactsHelper(contentResolver).getAllContacts().test()
        testObserver.assertValue { it.size == 3 }
    }

    @Test
    fun `get 1 contact with 2 phones`() {
        allContactsRoboCursor.setResults(arrayOf(CONTACT_1_FULL))
        phoneNumbersRoboCursor.setResults(
            arrayOf(
                PHONE_1_CONTACT_1,
                PHONE_2_CONTACT_1
            )
        )
        val testObserver = ContactsHelper(contentResolver).getAllContacts().test()


        testObserver.assertValue { it.size == 1 }
        testObserver.assertValue { it[CONTACT_1_ID]?.contactId == CONTACT_1_ID }
        testObserver.assertValue { it[CONTACT_1_ID]?.phoneNumbers?.size == 2 }
    }

    @Test
    fun `get 2 contacts with same phones`() {
        allContactsRoboCursor.setResults(arrayOf(CONTACT_1_FULL, CONTACT_1_FULL))
        phoneNumbersRoboCursor.setResults(
            arrayOf(
                PHONE_1_CONTACT_1,
                PHONE_2_CONTACT_1
            )
        )
        val testObserver = ContactsHelper(contentResolver).getAllContacts().test()


        testObserver.assertValue { it.size == 1 }
        testObserver.assertValue { it[CONTACT_1_ID]?.contactId == CONTACT_1_ID }
        testObserver.assertValue { it[CONTACT_1_ID]?.phoneNumbers?.size == 2 }
    }

    @Test
    fun `get contacts without phones`() {
        allContactsRoboCursor.setResults(
            arrayOf(
                CONTACT_1_FULL,
                CONTACT_2_FULL,
                CONTACT_3_EMPTY,
                CONTACT_4_EMPTY
            )
        )
        phoneNumbersRoboCursor.setResults(arrayOf())
        val testObserver = ContactsHelper(contentResolver).getAllContacts().test()
        testObserver.assertValue { it.isEmpty() }
    }

    @Test
    fun `get contact without name but with phone`() {
        allContactsRoboCursor.setResults(arrayOf(CONTACT_3_EMPTY))
        phoneNumbersRoboCursor.setResults(arrayOf(PHONE_1_CONTACT_3_OK))
        val testObserver = ContactsHelper(contentResolver).getAllContacts().test()
        testObserver.assertValue { it.size == 1 }
        testObserver.assertValue { it[CONTACT_3_ID] != null }
    }

    @Test
    fun `get contact without name and without phone`() {
        allContactsRoboCursor.setResults(arrayOf(CONTACT_4_EMPTY))
        phoneNumbersRoboCursor.setResults(arrayOf(PHONE_1_CONTACT_4_EMPTY))
        val testObserver = ContactsHelper(contentResolver).getAllContacts().test()
        testObserver.assertValue { it.isEmpty() }
    }

    companion object MockData {
        private val CONTACTS_COLUMNS = listOf(
            ContactsContract.Data.RAW_CONTACT_ID,
            ContactsContract.Data.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
            ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME
        )

        private val PHONES_COLUMNS = listOf(
            ContactsContract.Data.RAW_CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        private val CONTACT_1_ID = 1L
        private val CONTACT_2_ID = 2L
        private val CONTACT_3_ID = 3L
        private val CONTACT_4_ID = 4L
        private val CONTACT_5_ID = 5L

        private val CONTACT_1_FULL = arrayOf(
            CONTACT_1_ID,
            CONTACT_1_ID,
            null,
            "display name",
            "given name",
            "family name"
        )

        private val CONTACT_2_FULL = arrayOf(
            CONTACT_2_ID,
            CONTACT_2_ID,
            null,
            "display name",
            "given name",
            "family name"
        )

        private val CONTACT_3_EMPTY = arrayOf(
            CONTACT_3_ID,
            CONTACT_3_ID,
            null,
            null,
            null,
            null
        )

        private val CONTACT_4_EMPTY = arrayOf(
            CONTACT_4_ID,
            CONTACT_4_ID,
            null,
            null,
            null,
            null
        )
        private val CONTACT_5_THE_SAME_CONTACT_ID = arrayOf(
            CONTACT_5_ID,
            CONTACT_2_ID,
            null,
            "display name",
            "given name",
            "family name"
        )

        private val PHONE_1_CONTACT_1 = arrayOf(
            CONTACT_1_ID,
            "1113343431"
        )

        private val PHONE_2_CONTACT_1 = arrayOf(
            CONTACT_1_ID,
            "9999999999"
        )

        private val PHONE_1_CONTACT_2_EMPTY = arrayOf(
            CONTACT_2_ID,
            null
        )

        private val PHONE_2_CONTACT_2_OK = arrayOf(
            CONTACT_2_ID,
            "9999999999"
        )

        private val PHONE_1_CONTACT_3_OK = arrayOf(
            CONTACT_3_ID,
            "9999999999"
        )

        private val PHONE_1_CONTACT_4_EMPTY = arrayOf(
            CONTACT_4_ID,
            null
        )

        private val PHONE_1_CONTACT_5_OK = arrayOf(
            CONTACT_5_ID,
            "88888888888"
        )
    }
}