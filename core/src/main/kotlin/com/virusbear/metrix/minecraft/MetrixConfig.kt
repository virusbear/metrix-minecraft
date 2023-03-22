package com.virusbear.metrix.minecraft

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.properties.Properties
import kotlinx.serialization.properties.decodeFromStringMap
import kotlinx.serialization.properties.encodeToStringMap
import java.io.File
import java.util.*

@Serializable
data class MetrixConfig internal constructor(
    val port: Int = 9100,
    val includeJvm: Boolean = true,
    val scrapeInterval: Long = 5,
    val percentiles: List<Double> = emptyList()
) {
    @OptIn(ExperimentalSerializationApi::class)
    private fun save(file: File) {
        file.writeText(Properties(EmptySerializersModule()).encodeToStringMap(this).toList().joinToString(separator = "\n") { (k, v) -> "$k=$v" })
    }

    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        fun load(file: File): MetrixConfig =
            if(!file.exists()) {
                MetrixConfig().apply {
                    save(file)
                }
            } else {
                Properties(EmptySerializersModule())
                    .decodeFromStringMap(
                        file.readLines()
                            .associate {
                                it.split("=", limit = 2)
                                    .let {
                                        it[0] to it[1]
                                    }
                            }
                    )
            }
    }
}