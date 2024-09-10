package io.github.cdsap.parsetimeline

import com.google.gson.Gson
import io.github.cdsap.comparescans.MultipleScanMetrics
import io.github.cdsap.comparescans.model.MultipleBuildScanMetric
import io.github.cdsap.geapi.client.model.BuildWithResourceUsage
import io.github.cdsap.parsetimeline.model.Response
import java.io.BufferedWriter
import java.io.File

class GenerateMetrics(
    private val timelines: List<File>,
    private val generateTraceEvents: Boolean
) {

    fun generate() {
        val timelineParser = TimelineParser()
        val gson = Gson()
        val builds = mutableListOf<BuildWithResourceUsage>()
        val responses = mutableMapOf<String, Response>()
        timelines.forEach {
            println("Parsing timeline ${it.name}")
            val timeLineResponse = gson.fromJson(it.readText(), Response::class.java)
            responses[it.name] = timeLineResponse
            builds.add(timelineParser.getBuild(timeLineResponse!!, it.name))
        }
        println("Calculating metrics")
        val metrics = MultipleScanMetrics(builds).get()
        val name = timelines.map { it.name.removeExtension() }.joinToString("-")
        metricsCsv(metrics, "metrics-$name")

        if (generateTraceEvents) {
            responses.forEach {
                val traceEventsParser = TraceEventsParser()
                traceEventsParser.generateTraceFile(
                    it.value.data.timelineGraph,
                    "${it.key.removeExtension()}-event-traces.json"
                )
            }
        }
    }

    private fun metricsCsv(metrics: List<MultipleBuildScanMetric>, filePath: String) {
        val csv = "$filePath.csv"

        val headers =
            "entity,name,subcategory,type,${metrics.first().values.keys.joinToString(",")}\n"
        val startTimestamp = System.currentTimeMillis()
        File(csv).bufferedWriter().use { out: BufferedWriter ->
            out.write(headers)
            metrics.forEach { metric ->

                val line =
                    "${metric.metric.entity.name}," +
                        "${metric.metric.name},${metric.metric.subcategory},${metric.metric.type.name}," +
                        "${metric.values.values.joinToString(",")}\n"
                out.write(line)
            }
        }
        val endTime = System.currentTimeMillis()
        println("File $csv created in ${endTime - startTimestamp} ms")
    }
}
