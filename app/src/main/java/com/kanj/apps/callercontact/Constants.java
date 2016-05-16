package com.kanj.apps.callercontact;

/**
 * Created by kanj on 11/5/16.
 */
public class Constants {
    public static final String SHARED_PREFERENCE_FILE = "com.kanj.apps.callercontact.SHARED_PREFERENCE";
    public static final String MASK_SETTINGS_PREFERENCE_NAME = "MASK_SETTINGS_PREFERENCE";

    public static final int MASK_DEFAULT_ENABLE_ALL = 0x7fff;
    public static final int MASK_ENABLE = 0x0001;
    public static final int MASK_EMAIL = 0x0002;
    public static final int MASK_NICKNAME = 0x0004;
    public static final int MASK_NOTE = 0x0008;
    public static final int MASK_ORG = 0x0010;
    public static final int MASK_TITLE = 0x0020;
    public static final int MASK_RELATION = 0x0040;
    public static final int MASK_ADDRESS = 0x0080;
    public static final int MASK_STREET = 0x0100;
    public static final int MASK_PO_BOX = 0x0200;
    public static final int MASK_HOOD = 0x0400;
    public static final int MASK_CITY = 0x0800;
    public static final int MASK_REGION = 0x1000;
    public static final int MASK_POSTCODE = 0x2000;
    public static final int MASK_COUNTRY = 0x4000;

    public static final int ADDRESS_FIELDS_ON = 0x7f00; // Always OR this
    public static final int ADDRESS_FIELDS_OFF = 0x80ff; // Always AND this

    public static final int NOTIFICATION_ID = 132580;
}
