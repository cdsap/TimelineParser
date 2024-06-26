package io.github.cdsap.parsetimeline.model

data class BuildCacheOutcome(
    val miss: String?,
    val hit: String?,
    val store: String?,
    val rejectedStore: Any?,
    val hasFailure: Boolean,
    val rejectedStoreReason: Any?
)
