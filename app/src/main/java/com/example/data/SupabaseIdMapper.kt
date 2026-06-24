package com.example.data

import java.util.concurrent.ConcurrentHashMap

object SupabaseIdMapper {
    private val uuidToInt = ConcurrentHashMap<String, Int>()
    private val intToUuid = ConcurrentHashMap<Int, String>()

    fun getIntId(uuid: String): Int {
        return uuidToInt.getOrPut(uuid) {
            // Generate a positive unique 31-bit integer
            var id = uuid.hashCode() and 0x7FFFFFFF
            if (id == 0) id = 1
            // Handle extremely rare collisions
            while (intToUuid.containsKey(id) && intToUuid[id] != uuid) {
                id = (id + 1) and 0x7FFFFFFF
                if (id == 0) id = 1
            }
            intToUuid[id] = uuid
            id
        }
    }

    fun getUuid(intId: Int): String? {
        return intToUuid[intId]
    }

    fun registerMapping(intId: Int, uuid: String) {
        uuidToInt[uuid] = intId
        intToUuid[intId] = uuid
    }
}
