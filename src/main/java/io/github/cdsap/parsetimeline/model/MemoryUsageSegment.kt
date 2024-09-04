package io.github.cdsap.parsetimeline.model

data class MemoryUsageSegment(
    val segment: Segment,
    val memoryDiff: Double = 0.0,
    val memoryAvg: Double = 0.0,
    val memoryMax: Double = 0.0
)

data class Segment(
    var startTime: Long,
    val finishTime: Long,
    val lanes: Set<Int>,
    var blocks: Set<String>,
    var blocksId: String
)
