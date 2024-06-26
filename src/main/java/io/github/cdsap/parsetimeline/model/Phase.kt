package io.github.cdsap.parsetimeline.model

data class Phase(
    val blocks: List<Block>,
    val startTime: Long,
    val finishTime: Long,
    val type: String
)
