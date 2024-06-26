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
    private val mode by option().choice("generate-metrics", "generate-models").required()
    private val firstTimeline by option().file()
    private val secondTimeline by option().file()
    private val timeline by option().file().multiple()
    private val generateTraceEvents by option().flag(default = false)

    override fun run() {
        when (mode) {
            "generate-metrics" -> {
                if (firstTimeline == null || secondTimeline == null) {
                    throw IllegalArgumentException("Missing required parameters for generate-metrics: example: --mode generate-metrics --first-timeline <first-timeline.json> --second-timeline <second-timeline.json>")
                }
                GenerateMetrics(firstTimeline!!, secondTimeline!!, generateTraceEvents).generate()
            }
            "generate-models" -> {
                if (timeline.isEmpty()) {
                    throw IllegalArgumentException("Missing required parameters for generate-models: example: --mode generate-models --timeline <timeline.json>")
                }
                GenerateModels(timeline, generateTraceEvents).generate()
            }
            else -> {
                throw IllegalArgumentException("Invalid value for --mode: $mode")
            }
        }
    }
}
