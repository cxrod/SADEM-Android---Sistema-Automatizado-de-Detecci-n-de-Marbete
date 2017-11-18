package com.hackaton.sadm.pref;

import android.content.Context;
import android.content.SharedPreferences;

import com.hackaton.sadm.pref.model.User;

/**
 * Created by cesar_000 on 17/11/2017.
 */

public class PreferenceHelper {

    public static final long NULL_INDEX = -1L;
    public static final String PREF_FILE_NAME = "SADEM_PREFERENCE";

    private static final String PREF_KEY_USER_LOGGED_IN = "PREF_KEY_USER_LOGGED_IN";

    private final SharedPreferences mPrefs;

    public PreferenceHelper(Context context) {
        mPrefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
    }

    public User getCurrentUser() {
        String user = mPrefs.getString(PREF_KEY_USER_LOGGED_IN, null);
        return User.fromJson(user);
    }

    public void setCurrentUserName(User user) {
        mPrefs.edit().putString(PREF_KEY_USER_LOGGED_IN, user.toJson()).apply();
    }

    public boolean isUserLogged(){
        return mPrefs.getString(PREF_KEY_USER_LOGGED_IN, null) != null;
        //return true;
    }

}