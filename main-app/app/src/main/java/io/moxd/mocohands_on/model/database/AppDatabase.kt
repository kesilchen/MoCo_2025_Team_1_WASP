package io.moxd.mocohands_on.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import io.moxd.mocohands_on.model.database.daos.DeviceDao
import io.moxd.mocohands_on.model.database.daos.PeripheralConnectorDao
import io.moxd.mocohands_on.model.database.entities.Device
import io.moxd.mocohands_on.model.database.entities.PeripheralConnector

@Database(
    entities = [Device::class, PeripheralConnector::class],
    version = 3
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
    abstract fun peripheralConnectorDao(): PeripheralConnectorDao

    companion object {
        private const val DB_NAME = "appstore"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                ).fallbackToDestructiveMigration(true).build().also { INSTANCE = it }
            }
        }
    }
}