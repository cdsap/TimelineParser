package io.github.cdsap.parsetimeline.model

data class PlannedNode(
    val id: String,
    val name: Name,
    val outcome: Int,
    val notExecuted: Boolean,
    val predecessors: List<Int>,
    val successors: List<Int>
)
