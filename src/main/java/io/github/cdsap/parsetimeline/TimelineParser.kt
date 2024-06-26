package io.github.cdsap.parsetimeline

import io.github.cdsap.geapi.client.model.AvoidanceSavingsSummary
import io.github.cdsap.geapi.client.model.Build
import io.github.cdsap.geapi.client.model.Task
import io.github.cdsap.parsetimeline.model.Response

class TimelineParser {
    fun getBuild(response: Response): Build {
        val outcomes =
            arrayOf("fromCache", "avoided_up_to_date", "success", "failed", "no-source", "skipped", "unknown", "failed")

        val tasks = mutableListOf<Task>()
        response.data.executionNodes.forEach { executionNode ->
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
        return Build(
            builtTool = "gradle",
            taskExecution = tasks.toTypedArray(),
            tags = emptyArray(),
            requestedTask = emptyArray(),
            id = "",
            buildDuration = 0L,
            avoidanceSavingsSummary = AvoidanceSavingsSummary("0", "0", "0"),
            buildStartTime = 0L,
            projectName = "",
            goalExecution = emptyArray(),
            values = emptyArray()
        )
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
}
