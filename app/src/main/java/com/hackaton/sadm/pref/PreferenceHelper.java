package com.hackaton.sadm.pref;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by cesar_000 on 17/11/2017.
 */

public class PreferenceHelper {

    public static final long NULL_INDEX = -1L;
    public static final String PREF_FILE_NAME = "SADEM_PREFERENCE";

    private static final String PREF_KEY_USER_TOKEN = "PREF_KEY_USER_TOKEN";

    private final SharedPreferences mPrefs;

    public PreferenceHelper(Context context) {
        mPrefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
    }

    public String getAccessToken() {
        String token = mPrefs.getString(PREF_KEY_USER_TOKEN, null);
        return token;
    }

    public void setAccessToken(String token) {
        mPrefs.edit().putString(PREF_KEY_USER_TOKEN, token).apply();
    }

    public boolean isUserLogged(){
        return mPrefs.getString(PREF_KEY_USER_TOKEN, null) != null;
        //return true;
    }

}