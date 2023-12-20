package com.joeloewi.data.datastore

import android.content.Context
import androidx.datastore.dataStore

val Context.valuesDataStore by dataStore(
    fileName = "values.pb",
    serializer = ValuesSerializer
)