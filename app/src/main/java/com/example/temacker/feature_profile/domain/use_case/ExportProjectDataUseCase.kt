package com.example.temacker.feature_profile.domain.use_case

import com.example.temacker.feature_profile.domain.model.ExportFormat
import com.example.temacker.feature_profile.domain.model.ProjectExportSnapshot
import com.example.temacker.feature_profile.domain.repository.ExportFileWriter

class ExportProjectDataUseCase(
    private val exportFileWriter: ExportFileWriter
) {
    suspend operator fun invoke(projectName: String, snapshot: ProjectExportSnapshot, format: ExportFormat) =
        exportFileWriter.write(projectName, snapshot, format)
}
