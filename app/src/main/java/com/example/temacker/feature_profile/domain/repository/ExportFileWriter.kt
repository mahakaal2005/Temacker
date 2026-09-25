package com.example.temacker.feature_profile.domain.repository

import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_profile.domain.model.ExportFormat
import com.example.temacker.feature_profile.domain.model.ProjectExportSnapshot

// Writes the export to a cache file and returns its absolute path — implemented in
// feature_profile/data with a Context dependency, keeping domain Android-free (no domain interface
// in this codebase imports android.*). The presentation layer turns the path into a shareable
// content:// URI via FileProvider at share time (it already has a Context via LocalContext).
interface ExportFileWriter {
    suspend fun write(projectName: String, snapshot: ProjectExportSnapshot, format: ExportFormat): Result<String, DataError>
}
