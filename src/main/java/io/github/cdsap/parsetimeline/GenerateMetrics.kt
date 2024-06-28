package io.github.cdsap.parsetimeline

import com.google.gson.Gson
import io.github.cdsap.comparescans.Metrics
import io.github.cdsap.comparescans.model.Metric
import io.github.cdsap.parsetimeline.model.Response
import java.io.BufferedWriter
import java.io.File

class GenerateMetrics(
    private val firstTimeline: File,
    private val secondTimeline: File,
    private val generateTraceEvents: Boolean
) {

    fun generate() {
        val timelineParser = TimelineParser()
        val gson = Gson()

        println("Parsing timeline $firstTimeline")
        val firstTimeLineResponse = gson.fromJson(firstTimeline.readText(), Response::class.java)
        val firstBuild = timelineParser.getBuild(firstTimeLineResponse!!)
        println("Parsing timeline $secondTimeline")
        val secondTimeLineResponse = gson.fromJson(secondTimeline.readText(), Response::class.java)
        val secondBuild = timelineParser.getBuild(secondTimeLineResponse!!)

        println("Calculating metrics")
        val metrics = Metrics(firstBuild, secondBuild).get()

        metricsCsv(metrics, "metrics-${firstTimeline.name.removeExtension()}-${secondTimeline.name.removeExtension()}")

        if (generateTraceEvents) {
            val traceEventsParser = TraceEventsParser()
            traceEventsParser.generateTraceFile(
                firstTimeLineResponse.data.timelineGraph,
                "${firstTimeline.name.removeExtension()}-event-traces.json"
            )
            traceEventsParser.generateTraceFile(
                secondTimeLineResponse.data.timelineGraph,
                "${secondTimeline.name.removeExtension()}-event-traces.json"
            )
        }
    }

    private fun metricsCsv(metrics: List<Metric>, filePath: String) {
        val csv = "$filePath.csv"
        val headers =
            "entity,name,subcategory,type,first build,second build\n"
        val startTimestamp = System.currentTimeMillis()
        File(csv).bufferedWriter().use { out: BufferedWriter ->
            out.write(headers)
            metrics.forEach { metric ->
                val line =
                    "${metric.entity.name},${metric.name},${metric.subcategory},${metric.type.name},${metric.firstBuild},${metric.secondBuild}\n"
                out.write(line)
            }
        }
        val endTime = System.currentTimeMillis()
        println("File $csv created in ${endTime - startTimestamp} ms")
    }
}
