/**
 * A class to hold all constants needed in this app.
 *
 * @author Zac (Qi ZHANG)
 * Created on 09/24/2014.
 */
package com.dfwexcellerator.iplugin.helpers;

public class Constants {
    // Time limit for "press back again to quit" feature
    public static final long TIME_LIMIT_TO_EXIT = 2000;

    // Web addresses
    // We do not have server pages for these 2 functions, so we just use Google now.
    public static final String WEB_PAGE_ADDRESS_FORGET_PASSWORD = "https://www.google.com";
    public static final String WEB_PAGE_ADDRESS_REGISTER = "http://www.google.com";

    // PHP files addresses in our server
    public static final String PHP_AUTHORIZE_USER = "http://utdcan.org/zac_ntx_app/php/authorize_user.php";
    public static final String PHP_PLUGIN_GET_LIST = "http://utdcan.org/zac_ntx_app/php/plugin_get_list.php";
    public static final String PHP_PLUGIN_ADD = "http://utdcan.org/zac_ntx_app/php/plugin_add.php";
    public static final String PHP_PLUGIN_DELETE = "http://utdcan.org/zac_ntx_app/php/plugin_delete.php";
    public static final String PHP_COMMENT_EDIT = "http://utdcan.org/zac_ntx_app/php/comment_edit.php";
    public static final String PHP_COMMENT_GET_FOR_A_DEVICE = "http://utdcan.org/zac_ntx_app/php/comment_get_for_a_device.php";
    public static final String PHP_COMMENT_GET_FOR_A_USER = "http://utdcan.org/zac_ntx_app/php/comment_get_for_a_user.php";

    // Amazon search address
    public static final String AMAZON = "http://www.amazon.com";
    public static final String AMAZON_PREFIX = "http://www.amazon.com/s/ref=nb_sb_noss_2?url=search-alias%3Daps&field-keywords=";
    public static final String AMAZON_SUFFIX = "";

    // This part is about preferences
    public static final String PREFS_FILE_NAME = "MyPrefsFile";
    public static final String PREFS_KEY_HAS_SAVED_USER = "hasSavedUser";
    public static final String PREFS_KEY_SAVED_USER_AUTH = "savedUserAuth";
    public static final String PREFS_KEY_SAVED_USER_EMAIL = "savedUserEmail";
    public static final String PREFS_SENSOR_DATA_FILE_NAME = "MyDataFile";
    public static final String SUFFIX_POWER_CONSUME = "PowerConsume";
    public static final String SUFFIX_LAST_TIMESTAMP = "LastTimeStamp";
    public static final String SUFFIX_SAVED_DATA_SET = "SavedDataTree";

    // Authorization info for SensorLogin server
    public static final String AUTH_USER = "user";
    public static final String AUTH_ORG = "NTx_LIU";
    // Secret is covered in this public-edition source code.
    // If you need this to login to SensorLogic, please contact us.
    public static final String AUTH_SECRET = "";
    public static final String AUTH_STATUS_REPORT_API_ADDRESS = "http://partner.api.sensorlogic.com/v5.0/statusReport/list.json?";
    public static final String PAGE_SIZE = "500";

    // This part is about using intent to share data between activities.
    public static final String EXTRA_WEB_PAGE_URL = "webPageURL";
}
