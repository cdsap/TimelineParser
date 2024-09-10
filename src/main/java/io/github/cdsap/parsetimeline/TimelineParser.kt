package io.github.cdsap.parsetimeline

import io.github.cdsap.geapi.client.model.AvoidanceSavingsSummary
import io.github.cdsap.geapi.client.model.BuildWithResourceUsage
import io.github.cdsap.geapi.client.model.Metric
import io.github.cdsap.geapi.client.model.PerformanceMetrics
import io.github.cdsap.geapi.client.model.Task
import io.github.cdsap.parsetimeline.model.Response
import io.github.cdsap.parsetimeline.model.TimelineMetricsGraph
import org.nield.kotlinstatistics.median
import org.nield.kotlinstatistics.percentile

class TimelineParser {
    fun getBuild(response: Response, name: String): BuildWithResourceUsage {
        val outcomes =
            arrayOf("fromCache", "avoided_up_to_date", "success", "failed", "no-source", "skipped", "unknown", "failed")

        val tasks = mutableListOf<Task>()

        response.data.executionNodes.filter { it.inputArtifact == null }.forEach { executionNode ->
            val nodeName = response.data.nodeNames[executionNode.name.name]
            val projectPath = response.data.projectPaths[executionNode.name.projectPath]
            val type = response.data.workTypes[executionNode.type]
            var outcomeFormatted = ""
            if (outcomes[executionNode.outcome] == "fromCache") {
                if (executionNode.workUnitCacheOutcome == "local_hit") {
                    outcomeFormatted = "avoided_from_local_cache"
                } else if (executionNode.workUnitCacheOutcome == "remote_hit") {
                    outcomeFormatted = "avoided_from_remote_cache"
                }
            } else if ((outcomes[executionNode.outcome] == "avoided_up_to_date" && executionNode.noActions) || outcomes[executionNode.outcome] == "success" && executionNode.noActions) {
                outcomeFormatted = "lifecycle"
            } else if (outcomes[executionNode.outcome] == "noSource") {
                outcomeFormatted = "no-source"
            } else if (outcomes[executionNode.outcome] == "success" && executionNode.workUnitCacheOutcome != null) {
                outcomeFormatted = "executed_cacheable"
            } else if (outcomes[executionNode.outcome] == "success" && executionNode.workUnitCacheOutcome == null) {
                outcomeFormatted = "executed_not_cacheable"
            } else if (outcomes[executionNode.outcome] == "failed") {
                outcomeFormatted = if (executionNode.cacheabilityCategory == 1) {
                    "executed_cacheable"
                } else {
                    "executed_not_cacheable"
                }
            } else {
                outcomeFormatted = outcomes[executionNode.outcome]
            }
            tasks.add(
                Task(
                    taskType = type,
                    taskPath = "$projectPath$nodeName",
                    duration = executionNode.duration,
                    avoidanceOutcome = outcomeFormatted,
                    fingerprintingDuration = executionNode.snapshottingInputsDuration,
                    cacheArtifactSize = if (executionNode.buildCacheArtifact != null) convertToBytes(executionNode.buildCacheArtifact.formattedSize) else null
                )
            )
        }
        if (response.data.timelineMetricsGraph == null || response.data.timelineMetricsGraph.data == null) {
            return BuildWithResourceUsage(

                builtTool = "gradle",
                taskExecution = tasks.toTypedArray(),
                tags = emptyArray(),
                requestedTask = emptyArray(),
                id = name,
                buildDuration = 0L,
                avoidanceSavingsSummary = AvoidanceSavingsSummary("0", "0", "0"),
                buildStartTime = 0L,
                projectName = "",
                goalExecution = emptyArray(),
                values = emptyArray(),
                total = nullResponse(),
                execution = nullResponse(),
                nonExecution = nullResponse(),
                totalMemory = -1
            )
        } else {
            return BuildWithResourceUsage(

                builtTool = "gradle",
                taskExecution = tasks.toTypedArray(),
                tags = emptyArray(),
                requestedTask = emptyArray(),
                id = name,
                buildDuration = 0L,
                avoidanceSavingsSummary = AvoidanceSavingsSummary("0", "0", "0"),
                buildStartTime = 0L,
                projectName = "",
                goalExecution = emptyArray(),
                values = emptyArray(),
                total = calculateResponse(response.data.timelineMetricsGraph),
                execution = calculateResponse(response.data.timelineMetricsGraph),
                nonExecution = calculateResponse(response.data.timelineMetricsGraph),
                totalMemory = response.data.timelineMetricsGraph.totalSystemMemory
            )
        }
    }

    private fun convertToBytes(value: String): Long {
        val regex = """(\d+(\.\d+)?)\s*([a-zA-Z]+)""".toRegex()
        val matchResult = regex.find(value.trim())

        if (matchResult != null) {
            val (number, _, unit) = matchResult.destructured
            val size = number.toDouble()
            return when (unit) {
                "B" -> size.toLong()
                "KiB" -> (size * 1024).toLong()
                "MiB" -> (size * 1024 * 1024).toLong()
                "GiB" -> (size * 1024 * 1024 * 1024).toLong()
                else -> throw IllegalArgumentException("Unknown unit: $unit")
            }
        } else {
            throw IllegalArgumentException("Invalid input: $value")
        }
    }

    private fun calculateResponse(timelineMetricsGraph: TimelineMetricsGraph): PerformanceMetrics {
        return PerformanceMetrics(
            buildProcessCpu = returnValues(timelineMetricsGraph.data.ownProcessCPU.values.dropLast(1)),
            allProcessesCpu = returnValues(timelineMetricsGraph.data.totalCPU.values.dropLast(1)),
            buildChildProcessesCpu = returnValues(timelineMetricsGraph.data.spawnProcessCPU.values.dropLast(1)),
            allProcessesMemory = returnValues(timelineMetricsGraph.data.totalMemory.values.dropLast(1)),
            buildProcessMemory = returnValues(timelineMetricsGraph.data.ownProcessMemory.values.dropLast(1)),
            buildChildProcessesMemory = returnValues(timelineMetricsGraph.data.spawnProcessMemory.values.dropLast(1)),
            diskReadThroughput = returnValues(timelineMetricsGraph.data.ioReadSpeed.values.dropLast(1)),
            diskWriteThroughput = returnValues(timelineMetricsGraph.data.ioWriteSpeed.values.dropLast(1)),
            networkUploadThroughput = returnValues(timelineMetricsGraph.data.networkUploadSpeed.values.dropLast(1)),
            networkDownloadThroughput = returnValues(timelineMetricsGraph.data.networkDownloadSpeed.values.dropLast(1))
        )
    }

    private fun returnValues(performanceMetricsRaw: List<Long>): Metric {
        return Metric(
            max = performanceMetricsRaw.filter { it != null }.max(),
            median = performanceMetricsRaw.filter { it != null }.median().toLong(),
            p25 = performanceMetricsRaw.filter { it != null }.percentile(25.0).toLong(),
            p75 = performanceMetricsRaw.filter { it != null }.percentile(75.0).toLong(),
            p95 = performanceMetricsRaw.filter { it != null }.percentile(95.0).toLong(),
            average = performanceMetricsRaw.filter { it != null }.average().toLong()
        )
    }

    private fun nullResponse(): PerformanceMetrics {
        return PerformanceMetrics(
            buildProcessCpu = nullMetric(),
            allProcessesCpu = nullMetric(),
            buildChildProcessesCpu = nullMetric(),
            allProcessesMemory = nullMetric(),
            buildProcessMemory = nullMetric(),
            buildChildProcessesMemory = nullMetric(),
            diskReadThroughput = nullMetric(),
            diskWriteThroughput = nullMetric(),
            networkUploadThroughput = nullMetric(),
            networkDownloadThroughput = nullMetric()
        )
    }
    private fun nullMetric(): Metric {
        return Metric(
            max = -1,
            median = -1,
            p25 = -1,
            p75 = -1,
            p95 = -1,
            average = -1
        )
    }
}
