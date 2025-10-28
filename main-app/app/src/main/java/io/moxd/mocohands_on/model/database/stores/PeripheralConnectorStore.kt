package io.moxd.mocohands_on.model.database.stores

import io.moxd.mocohands_on.model.database.daos.PeripheralConnectorDao
import io.moxd.mocohands_on.model.database.entities.PeripheralConnector

class PeripheralConnectorStore(private val peripheralConnectorDao: PeripheralConnectorDao) {
    suspend fun insertPeripheralConnector(connector: PeripheralConnector) = peripheralConnectorDao.insertPeripheralConnector(connector)
}