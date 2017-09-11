// Copyright 2017 Sourcerer Inc. All Rights Reserved.
// Author: Liubov Yaronskaya (lyaronskaya@sourcerer.io)
// Author: Anatoly Kislov (anatoly@sourcerer.io)

package app.extractors

import app.model.CommitStats
import app.model.DiffFile

class GoExtractor : ExtractorInterface {
    companion object {
        val LANGUAGE_NAME = "go"
        val FILE_EXTS = listOf("go")
    }

    override fun extract(files: List<DiffFile>): List<CommitStats> {
        files.map { file -> file.language = LANGUAGE_NAME }
        return super.extract(files)
    }

    override fun extractImports(fileContent: List<String>): List<String> {
        val libraries = mutableSetOf<String>()

        val singleImportRegex = Regex("""import\s+"(\w+)"""")
        fileContent.forEach {
            val res = singleImportRegex.find(it)
            if (res != null) {
                val lineLib = res.groupValues.last()
                libraries.add(lineLib)
            }
        }
        val multipleImportRegex = Regex("""import[\s\t\n]+\((.+?)\)""",
                RegexOption.DOT_MATCHES_ALL)
        val contentJoined = fileContent.joinToString(separator = "")
        multipleImportRegex.findAll(contentJoined).forEach { matchResult ->
            libraries.addAll(matchResult.groupValues.last()
                .split(Regex("""(\t+|\n+|\s+)"""))
                .filter { it.isNotEmpty() }
                .map { it -> it.replace("\"", "") })
        }

        return libraries.toList()
    }
}
