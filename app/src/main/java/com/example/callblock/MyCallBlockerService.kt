package com.example.callblock

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallScreeningService

class MyCallBlockerService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        val isIncoming = callDetails.callDirection == Call.Details.DIRECTION_INCOMING

        if (isIncoming) {
            val phoneNumberUri = callDetails.handle
            val phoneNumber = phoneNumberUri?.schemeSpecificPart

            // Check if the number is in contacts
            val isSavedContact = isContact(applicationContext, phoneNumber)

            // If it is NOT a saved contact, block it
            if (!isSavedContact) {
                val response = CallResponse.Builder()
                    .setDisallowCall(true)
                    .setRejectCall(true)
                    .setSilenceCall(false)
                    .setSkipCallLog(true)
                    .setSkipNotification(true)
                    .build()

                respondToCall(callDetails, response)
                return
            }
        }

        // If it's a contact (or an outgoing call), let it through normally
        respondToCall(callDetails, CallResponse.Builder().build())
    }

    /**
     * Queries the Android Contacts database to see if the number exists.
     */
    /**
     * Queries the Android Contacts database to see if the number exists.
     */
    private fun isContact(context: Context, phoneNumber: String?): Boolean {
        if (phoneNumber.isNullOrEmpty()) return false

        return try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber)
            )

            val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
            var contactFound = false

            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    contactFound = true
                }
            }
            contactFound

        } catch (e: Exception) {
            // Catches any database or permission errors so the app doesn't crash
            e.printStackTrace()
            true // We return true on an error so we don't accidentally block a real caller
        }
    }
}