package io.moxd.mocohands_on.model.database.entities

import androidx.room.Embedded
import androidx.room.Relation

data class DeviceWithPeripheralConnector(
    @Embedded
    val device: Device,
    @Relation(
        parentColumn = "peripheralConnectorId",
        entityColumn = "id"
    )
    val peripheralConnector: PeripheralConnector
)