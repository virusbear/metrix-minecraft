package com.virusbear.metrix.minecraft

import com.virusbear.metrix.micrometer.MetrixBinder
import io.ktor.application.*
import io.ktor.metrics.micrometer.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.micrometer.core.instrument.Meter
import io.micrometer.core.instrument.binder.jvm.*
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.core.instrument.binder.system.UptimeMetrics
import io.micrometer.core.instrument.config.MeterFilter
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.time.Duration

class MetrixServer(private val binder: MetrixBinder, private var config: MetrixConfig) {
    companion object {
        val LOG: Logger = LogManager.getLogger()
    }

    private val meterRegistry = PrometheusMeterRegistry(object: PrometheusConfig {
        override fun get(key: String): String? =
            null

        override fun step(): Duration =
            Duration.ofSeconds(config.scrapeInterval)

        override fun descriptions(): Boolean =
            false
    }).apply {
        config().meterFilter(object: MeterFilter {
            override fun configure(id: Meter.Id, config: DistributionStatisticConfig): DistributionStatisticConfig? =
                if(id.type == Meter.Type.TIMER && this@MetrixServer.config.percentiles.isNotEmpty()) {
                    DistributionStatisticConfig
                        .builder()
                        .percentiles(*this@MetrixServer.config.percentiles.toDoubleArray())
                        .build()
                        .merge(config)
                } else {
                    config
                }
        })
    }

    private val server =
        embeddedServer(
            Netty,
            port = config.port,
            configure = {
                tcpKeepAlive = true
            }
        ) {
            install(MicrometerMetrics) {
                registry = meterRegistry
                meterBinders = if(config.includeJvm) {
                    listOf(
                        JvmMemoryMetrics(),
                        JvmGcMetrics(),
                        ProcessorMetrics(),
                        UptimeMetrics(),
                        JvmThreadMetrics(),
                        ClassLoaderMetrics(),
                        FileDescriptorMetrics(),
                        JvmCompilationMetrics(),
                        JvmHeapPressureMetrics(),
                        JvmInfoMetrics()
                    )
                } else {
                    emptyList()
                } + binder
            }

            routing {
                get("/metrics") {
                    call.respond(meterRegistry.scrape())
                }
            }
        }

    fun start() {
        LOG.info("Metrix server starting")
        server.start()
    }

    fun stop() {
        LOG.info("Metrix server stopping")
        server.stop(1000, 1000)
    }
}