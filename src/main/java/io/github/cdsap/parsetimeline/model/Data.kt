package io.github.cdsap.parsetimeline.model

data class Data(
    val hasOnlyTasks: Boolean,
    val projectCount: Int,
    val taskCount: Int,
    val failedTaskCount: Int,
    val transformCount: Int,
    val failedTransformCount: Int,
    val buildDuration: Int,
    val avoidanceSavingsDuration: Int,
    val avoidanceSavingsTaskCount: Int,
    val executionNodes: List<ExecutionNode>,
    val canFilterByCacheability: Boolean,
    val canFilterOutputByTask: Boolean,
    val plannedNodes: List<PlannedNode>,
    val dependencies: List<Dependency>,
    val projectPaths: List<String>,
    val nodeNames: List<String>,
    val workTypes: List<String>,
    val cacheabilityCategories: List<String>,
    val nonCacheableLabels: List<String>,
    val nonCacheableReasonMessages: List<String>,
    val inputArtifactNames: List<String>,
    val configurationNames: List<String>,
    val attributes: List<Attribute>,
    val isCriticalPathAvailable: Boolean,
    val timelineGraph: TimelineGraph
)
