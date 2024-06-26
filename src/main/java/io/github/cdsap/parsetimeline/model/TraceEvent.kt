package io.github.cdsap.parsetimeline.model

data class TraceEvent(
    val name: String,
    val cat: String,
    val ph: String,
    val ts: Long,
    val dur: Long? = null,
    val pid: Int,
    val tid: Int,
    val args: Map<String, String>? = null
)
