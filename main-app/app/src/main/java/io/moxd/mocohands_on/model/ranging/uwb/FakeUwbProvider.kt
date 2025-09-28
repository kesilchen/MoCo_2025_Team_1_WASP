package io.moxd.mocohands_on.model.ranging.uwb

import androidx.core.uwb.UwbAddress
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.merge
import kotlin.math.PI
import kotlin.random.Random

class FakeUwbProvider : UwbProvider {
    private val uwbControllers = MutableStateFlow<List<FakeUwbController>>(listOf())

    @OptIn(ExperimentalCoroutinesApi::class)
    override val readings = uwbControllers.flatMapLatest { controllers ->
        merge(*controllers.map { it.readings }.toTypedArray())
    }

    override suspend fun prepareSession(nRemotes: Int): List<UwbAddress> {
        repeat(nRemotes) { index ->
            val controller = FakeUwbController(index * PI * Random.nextDouble())
            uwbControllers.value += controller
        }
        return listOf()
    }

    override fun startRanging(
        uwbDevices: List<UwbDeviceConfiguration>,
    ) {
        uwbControllers.value.forEachIndexed { i, controller ->
            controller.startRanging()
        }
    }

    override fun stopRanging() {
        uwbControllers.value.forEach { controller ->
            controller.stopRanging()
        }
        uwbControllers.value = listOf()
    }
}