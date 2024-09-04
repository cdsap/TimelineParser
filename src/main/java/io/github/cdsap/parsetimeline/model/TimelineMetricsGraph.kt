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
        val totalCPU: CpuMetric,
        val ownProcessCPU: CpuMetric,
        val spawnProcessCPU: CpuMetric,
        val totalMemory: MemoryMetric,
        val ownProcessMemory: MemoryMetric,
        val spawnProcessMemory: MemoryMetric,
        val ioReadSpeed: IoMetric,
        val ioWriteSpeed: IoMetric,
        val networkUploadSpeed: IoMetric,
        val networkDownloadSpeed: IoMetric,
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
