package io.github.cdsap.parsetimeline.model

data class UpToDateMessages(
    val known: List<String>,
    val unknown: List<String>
)
