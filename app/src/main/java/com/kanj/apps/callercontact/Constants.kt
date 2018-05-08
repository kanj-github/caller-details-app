package com.kanj.apps.callercontact

class Constants {
    companion object {
        val SHARED_PREFERENCE_FILE = "com.kanj.apps.callercontact.SHARED_PREFERENCE"
        val MASK_SETTINGS_PREFERENCE_NAME = "MASK_SETTINGS_PREFERENCE"

        val MASK_DEFAULT_ENABLE_ALL = 0x7fff
        val MASK_ENABLE = 0x0001
        val MASK_EMAIL = 0x0002
        val MASK_NICKNAME = 0x0004
        val MASK_NOTE = 0x0008
        val MASK_ORG = 0x0010
        val MASK_TITLE = 0x0020
        val MASK_RELATION = 0x0040
        val MASK_ADDRESS = 0x0080
        val MASK_STREET = 0x0100
        val MASK_PO_BOX = 0x0200
        val MASK_HOOD = 0x0400
        val MASK_CITY = 0x0800
        val MASK_REGION = 0x1000
        val MASK_POSTCODE = 0x2000
        val MASK_COUNTRY = 0x4000

        val ADDRESS_FIELDS_ON = 0x7f00 // Always OR this
        val ADDRESS_FIELDS_OFF = 0x80ff // Always AND this

        val NOTIFICATION_ID = 132580
        val NOTIFICATION_CHANNEL = "Caller Deatils"
    }
}