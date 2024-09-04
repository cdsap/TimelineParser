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
            val build = timelineParser.getBuild(response!!, it.name)
            File("${it.name.removeExtension()}-parsed.json").writeText(Gson().toJson(build))
            println("Timeline parsed in ${it.name.removeExtension()}-parsed.json")
            if (generateTraceEvents) {
                val traceEventsParser = TraceEventsParser()
                traceEventsParser.generateTraceFile(
                    response.data.timelineGraph,
                    "${it.name.removeExtension()}-event-traces.json"
                )
            }
        }
    }
}
