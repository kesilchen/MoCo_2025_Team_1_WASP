package io.moxd.mocohands_on.model.database.daos

import androidx.room.Dao
import androidx.room.Insert
import io.moxd.mocohands_on.model.database.entities.PeripheralConnector

@Dao
interface PeripheralConnectorDao {
    @Insert
    suspend fun insertPeripheralConnector(connector: PeripheralConnector): Long
}