package io.moxd.mocohands_on.model.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import io.moxd.mocohands_on.model.database.entities.Device
import io.moxd.mocohands_on.model.database.entities.DeviceWithPeripheralConnector
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    @Insert
    suspend fun insertDevice(device: Device): Long

    @Transaction
    @Query("SELECT * FROM devices WHERE id = :id")
    fun getDeviceWithPeripheralConnector(id: Long): Flow<DeviceWithPeripheralConnector?>

    @Transaction
    @Query("SELECT * FROM devices")
    fun listDevicesWithPeripheralConnector(): Flow<List<DeviceWithPeripheralConnector>>

    @Query("SELECT COUNT(*) FROM devices")
    fun getDeviceCount(): Flow<Int>
}