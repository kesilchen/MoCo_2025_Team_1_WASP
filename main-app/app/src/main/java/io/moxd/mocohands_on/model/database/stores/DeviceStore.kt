package io.moxd.mocohands_on.model.database.stores

import io.moxd.mocohands_on.model.database.daos.DeviceDao
import io.moxd.mocohands_on.model.database.entities.Device
import io.moxd.mocohands_on.model.database.entities.DeviceWithPeripheralConnector
import kotlinx.coroutines.flow.Flow

class DeviceStore(private val deviceDao: DeviceDao) {
    suspend fun insertDevice(device: Device) = deviceDao.insertDevice(device)
    fun getDeviceWithPeripheralConnector(id: Long) =
        deviceDao.getDeviceWithPeripheralConnector(id)
    fun listDevicesWithPeripheralConnector() = deviceDao.listDevicesWithPeripheralConnector()
    fun getDeviceCount() = deviceDao.getDeviceCount()
}