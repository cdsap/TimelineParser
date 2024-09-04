package io.github.cdsap.parsetimeline

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file

fun main(args: Array<String>) {
    TimeLine().main(args)
}

class TimeLine : CliktCommand() {
    private val mode by option().choice("generate-metrics", "generate-models", "kotlin-usage-report").required()
    private val timeline by option().file().multiple(required = true)
    private val generateTraceEvents by option().flag(default = false)

    override fun run() {
        when (mode) {
            "generate-metrics" -> {
                GenerateMetrics(timeline, generateTraceEvents).generate()
            }

            "generate-models" -> {
                if (timeline.isEmpty()) {
                    throw IllegalArgumentException("Missing required parameters for generate-models: example: --mode generate-models --timeline <timeline.json>")
                }
                GenerateModels(timeline, generateTraceEvents).generate()
            }

            "kotlin-usage-report" -> {
                if (timeline.size != 1) {
                    throw IllegalArgumentException("kotlin-usage-report only supports 1 timeline parameter")
                }
                KotlinUsageReport(timeline[0]).generate()
            }

            else -> {
                throw IllegalArgumentException("Invalid value for --mode: $mode")
            }
        }
    }
}
