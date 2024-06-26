package io.github.cdsap.parsetimeline

import com.google.gson.GsonBuilder
import io.github.cdsap.parsetimeline.model.TimelineGraph
import io.github.cdsap.parsetimeline.model.TraceEvent
import java.io.FileWriter
import java.io.IOException

class TraceEventsParser {

    private fun convertToTraceEvents(timelineData: TimelineGraph): List<TraceEvent> {
        val traceEvents = mutableListOf<TraceEvent>()
        timelineData.phases.forEachIndexed { phaseIndex, phase ->
            phase.blocks.forEach { block ->
                traceEvents.add(
                    TraceEvent(
                        name = block.label,
                        cat = phase.type,
                        ph = "X",
                        ts = block.startTime,
                        dur = block.finishTime - block.startTime,
                        pid = 0,
                        tid = block.laneId,
                        args = mapOf("phase" to phaseIndex.toString())
                    )
                )
            }
        }
        return traceEvents
    }

    fun generateTraceFile(timelineData: TimelineGraph, filePath: String) {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val traceEvents = convertToTraceEvents(timelineData)
        try {
            FileWriter(filePath).use { writer -> gson.toJson(traceEvents, writer) }
            println("Trace events generated in $filePath")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
