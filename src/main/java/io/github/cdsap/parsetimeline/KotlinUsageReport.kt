package io.github.cdsap.parsetimeline

import com.google.gson.Gson
import io.github.cdsap.parsetimeline.model.Response
import java.io.File

class KotlinUsageReport(private val file: File) {

    fun generate() {
        val gson = Gson()
        val timeLineResponse = gson.fromJson(file.readText(), Response::class.java)
        if (timeLineResponse.data.timelineMetricsGraph == null) {
            throw IllegalArgumentException("Build scan response does not contain timelineMetricsGraph data. Please use a build scan with timelineMetricsGraph data.")
        }
        UsagePerformanceParser().parseTimelineDataGraphMet(timeLineResponse.data)
    }
}
