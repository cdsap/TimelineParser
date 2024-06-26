package io.github.cdsap.parsetimeline.model

data class BuildCacheOperation(
    val type: String,
    val duration: Int,
    val formattedAverageSpeed: String?,
    val archiveEntryCount: Int,
    val exceptions: Any?
)
