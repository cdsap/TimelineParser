package io.github.cdsap.parsetimeline.model

data class Block(
    val id: String,
    val laneId: Int,
    val startTime: Long,
    val finishTime: Long,
    val label: String
)
