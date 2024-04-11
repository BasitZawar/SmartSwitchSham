package com.example.ss_new.subscription

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "subscriptions")

class DataStoreManager(val context: Context) {

   private val subscription = booleanPreferencesKey("subscriptionFound")
    suspend fun saveSubscription(sub: Boolean) {
        context.dataStore.edit {
            it[subscription] = sub
        }
    }


    val hasSubscription: Flow<Boolean> = context.dataStore.data.map {
        it[subscription] ?: false
    }

    suspend fun saveSubscription(sub: Subscriptions) {

        val key = booleanPreferencesKey(sub.key)
        context.dataStore.edit {
            it[key] = sub.isSubscribe
        }
    }


    fun getSubscription(sub: Subscriptions): Flow<Boolean> = context.dataStore.data.map {
        val key = booleanPreferencesKey(sub.key)
        it[key] ?: sub.isSubscribe
    }

    /*  suspend fun getFromDataStore(): String? {
          val values = context.dataStore.data.first()
          val value = values[FOCUS_LENGTH]
          return value
      }*/
}