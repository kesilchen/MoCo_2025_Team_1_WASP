package io.moxd.mocohands_on.model.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "devices",
    foreignKeys = [
        ForeignKey(
            entity = PeripheralConnector::class,
            parentColumns = ["id"],
            childColumns = ["peripheralConnectorId"]
        )
    ],
    indices = [Index("peripheralConnectorId")]
)
class Device(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val uwbAddress: String,
    val uwbSessionId: Int,
    val peripheralConnectorId: Long
)