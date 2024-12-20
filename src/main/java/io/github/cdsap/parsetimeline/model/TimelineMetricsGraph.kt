package io.github.cdsap.parsetimeline.model

data class TimelineMetricsGraph(
    val data: GraphData,
    val startTime: Long,
    val finishTime: Long,
    val totalSystemMemory: Long,
    val processes: List<Process>,
    val typename: String
) {
    data class GraphData(
        val timestamp: List<Long>,
        val allProcessesCpu: CpuMetric,
        val buildProcessCpu: CpuMetric,
        val buildChildProcessesCpu: CpuMetric,
        val allProcessesMemory: MemoryMetric,
        val buildProcessMemory: MemoryMetric,
        val buildChildProcessesMemory: MemoryMetric,
        val diskReadThroughput: IoMetric,
        val diskWriteThroughput: IoMetric,
        val networkUploadThroughput: IoMetric,
        val networkDownloadThroughput: IoMetric,
        val topProcesses: List<List<Int>>
    ) {

        data class CpuMetric(
            val values: List<Long>,
            val message: String?
        )

        data class MemoryMetric(
            val values: List<Long>,
            val message: String?
        )

        data class IoMetric(
            val values: List<Long>,
            val message: String?
        )
    }

    data class Process(
        val name: String,
        val type: String
    )
}
