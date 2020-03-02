package com.cazimir.relaxoo.repository

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.GsonBuilder

object ModelPreferencesManager {


    //Shared Preference field used to save and retrieve JSON string
    lateinit var preferences: SharedPreferences

    //Name of Shared Preference file
    private const val PREFERENCES_FILE_NAME = "PREFERENCES_FILE_NAME"

    /**
     * Call this first before retrieving or saving object.
     *
     * @param application Instance of application class
     */
    fun with(application: Application) {
        preferences = application.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Saves object into the Preferences.
     *
     * @param `object` Object of model class (of type [T]) to save
     * @param key Key with which Shared preferences to
     **/
    fun <T> put(`object`: T, key: String) {
        //Convert object to JSON String.
        val jsonString = GsonBuilder().create().toJson(`object`)
        // save that string in shared preferences
        preferences.edit().putString(key, jsonString).apply()
    }

    /**
     * Used to retrieve object from the Preferences.
     *
     * @param key Shared Preference key with which object was saved.
     **/
    inline fun <reified T> get(key: String): T? {
        // we read JSON String which was saved.
        val value = preferences.getString(key, null)
        // json string was found which means object can be read.
        // we convert this JSON strng to model object.
        return GsonBuilder().create().fromJson(value, T::class.java)
    }
}