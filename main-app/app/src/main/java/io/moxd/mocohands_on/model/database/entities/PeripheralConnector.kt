package io.moxd.mocohands_on.model.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.moxd.mocohands_on.model.peripherals.PeripheralConnectorType

@Entity(tableName = "peripheral_connectors")
class PeripheralConnector(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val type: PeripheralConnectorType,
    val apiUrl: String,
)