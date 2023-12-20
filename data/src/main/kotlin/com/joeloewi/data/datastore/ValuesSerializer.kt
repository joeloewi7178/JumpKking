package com.joeloewi.data.datastore

import androidx.datastore.core.Serializer
import com.joeloewi.data.Values
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

object ValuesSerializer : Serializer<Values> {

    override val defaultValue: Values = Values.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): Values = withContext(Dispatchers.IO) {
        Values.parseFrom(input)
    }

    override suspend fun writeTo(t: Values, output: OutputStream) = withContext(Dispatchers.IO) {
        t.writeTo(output)
    }
}