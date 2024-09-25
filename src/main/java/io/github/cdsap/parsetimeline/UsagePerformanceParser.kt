package io.github.cdsap.parsetimeline

import io.github.cdsap.parsetimeline.model.Block
import io.github.cdsap.parsetimeline.model.Data
import io.github.cdsap.parsetimeline.model.MemoryUsageSegment
import io.github.cdsap.parsetimeline.model.Segment
import io.github.cdsap.parsetimeline.model.TimelineMetricsGraph
import java.io.BufferedWriter
import java.io.File

class UsagePerformanceParser {

    fun parseTimelineDataGraphMet(timelineData: Data) {
        val allBlocks = timelineData.timelineGraph.phases.flatMap {
            it.blocks.filter { tasksFiltered(it) }
        }

        val segments = splitBlocksIntoSegments(allBlocks)
        writeSegments(segments)

        val metrics = memoryMetricsBySegment(
            segments,
            timelineData.timelineMetricsGraph.data
        )
        writMemoryMetricsBySegment(metrics)
    }

    private fun writMemoryMetricsBySegment(avgMemoryBySegment: List<MemoryUsageSegment>) {
        println("writing average memory by segment in average_memory_by_segment.csv")
        val headers = "segment start, segment end, tasks ,max memory,avg memory, diff\n"
        File("memory_by_segment.csv").bufferedWriter().use { out: BufferedWriter ->
            out.write(headers)
            avgMemoryBySegment.forEach { metric ->
                val line = buildString {
                    append(
                        "${metric.segment.startTime}," +
                            "${metric.segment.finishTime},${metric.segment.blocks.joinToString(";")}, " +
                            "${metric.memoryMax},${metric.memoryAvg},${metric.memoryDiff}"
                    )
                    append("\n")
                }
                out.write(line)
            }
        }
    }

    private fun writeSegments(segments: List<Segment>) {
        println("writing segments.csv")
        File("segments.csv").bufferedWriter().use { out: BufferedWriter ->
            segments.forEach { segment ->
                val line = buildString {
                    append("${segment.startTime},${segment.finishTime},${segment.blocks.joinToString(";")}")
                    append("\n")
                }
                out.write(line)
            }
        }
    }

    private fun tasksFiltered(it: Block) = (
        it.label.lowercase()
            .endsWith("kotlin") && !it.label.contains("checkKotlinGradlePluginConfigurationErrors") && !it.label.contains(
            "ksp"
        ) &&
            !it.label.contains("kapt")
        )

    private fun splitBlocksIntoSegments(blocks: List<Block>): List<Segment> {
        val segments = mutableListOf<Segment>()

        val events = blocks.flatMap { listOf(it.startTime, it.finishTime) }.distinct().sorted()

        var activeBlocks = mutableListOf<Block>()
        var currentSegmentStartTime: Long? = null

        for (i in 0 until events.size - 1) {
            val eventStart = events[i]
            val eventEnd = events[i + 1]

            activeBlocks = blocks.filter { it.startTime <= eventStart && it.finishTime >= eventEnd }.toMutableList()

            if (activeBlocks.isNotEmpty()) {
                if (currentSegmentStartTime == null) {
                    currentSegmentStartTime = eventStart
                }
                segments.add(
                    Segment(
                        startTime = eventStart,
                        finishTime = eventEnd,
                        lanes = activeBlocks.map { it.laneId }.toSet(),
                        blocks = activeBlocks.map { it.label }.toSet(),
                        blocksId = activeBlocks.map { it.label }.toSet().joinToString(";")
                    )
                )
            }
        }

        return segments
    }

    private fun memoryMetricsBySegment(
        segments: List<Segment>,
        graphData: TimelineMetricsGraph.GraphData
    ): List<MemoryUsageSegment> {
        val memoryUsageSegments = mutableListOf<MemoryUsageSegment>()

        segments.forEach { segment ->

            val memoryPositions = findMemoryMetricPositions(segment.startTime, segment.finishTime, graphData)
            val memoryValues = getMemoryMetricValues(graphData.buildChildProcessesMemory, memoryPositions)
            if (!memoryValues.isNullOrEmpty()) {
                val maxMemory = bytesToGigabytes(memoryValues.max().toLong())
                val avgMemory = bytesToGigabytes(memoryValues.average().toLong())
                val diff = memoryValues.first() - memoryValues.last()
                val increaseMemory = bytesToGigabytes(diff)

                memoryUsageSegments.add(
                    MemoryUsageSegment(
                        segment = segment,
                        memoryDiff = increaseMemory,
                        memoryAvg = avgMemory,
                        memoryMax = maxMemory
                    )
                )
            }
        }
        return memoryUsageSegments
    }
}

fun findMemoryMetricPositions(
    startTime: Long,
    finishTime: Long,
    graphData: TimelineMetricsGraph.GraphData
): IntRange? {
    val startTimeIndex = graphData.timestamp.indexOfFirst { it >= startTime }
    val finishTimeIndex = graphData.timestamp.indexOfLast { it <= finishTime }
    return if (startTimeIndex > 0 && finishTimeIndex < graphData.timestamp.size - 1) {
        (startTimeIndex - 1)..(finishTimeIndex + 1)
    } else {
        null
    }
}

fun bytesToGigabytes(bytes: Long): Double {
    return bytes / (1024.0 * 1024.0 * 1024.0)
}

fun getMemoryMetricValues(
    memoryMetric: TimelineMetricsGraph.GraphData.MemoryMetric,
    positions: IntRange?
): List<Long>? {
    return positions?.mapNotNull { index -> memoryMetric.values.getOrNull(index) }
}
