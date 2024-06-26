package io.github.cdsap.parsetimeline

import com.google.gson.Gson
import io.github.cdsap.parsetimeline.model.Response
import java.io.File

class GenerateModels(
    private val timelines: List<File>,
    private val generateTraceEvents: Boolean
) {

    fun generate() {
        val timelineParser = TimelineParser()
        val gson = Gson()
        timelines.forEach {
            val response = gson.fromJson(it.readText(), Response::class.java)
            val build = timelineParser.getBuild(response!!)
            File("${it.name}-parsed.json").writeText(Gson().toJson(build))
            println("Timeline parsed in ${it.name}-parsed.json")
            if (generateTraceEvents) {
                val traceEventsParser = TraceEventsParser()
                traceEventsParser.generateTraceFile(
                    response.data.timelineGraph,
                    "${it.name}-event-traces.json"
                )
            }
        }
    }
}
