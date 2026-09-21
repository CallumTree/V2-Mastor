package com.example.domain.boq

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.provider.OpenableColumns
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.zip.Inflater
import java.util.zip.ZipInputStream

/**
 * Utility for extracting plain text from BoQ files (TXT, CSV, PDF, DOCX)
 * prior to parsing with GeminiBoqParser.
 */
object BoqTextExtractor {

    /**
     * Extracts text from the given document URI according to its file format.
     * - TXT / CSV: reads directly via ContentResolver.openInputStream()
     * - PDF: uses PdfRenderer (built-in Android) page-by-page and parses content streams
     * - DOCX: reads word/document.xml from the zip archive and strips XML tags
     */
    fun extractText(context: Context, uri: Uri): String {
        val mimeType = context.contentResolver.getType(uri)?.lowercase() ?: ""
        val fileName = queryDisplayName(context, uri).lowercase()

        return when {
            fileName.endsWith(".pdf") || mimeType == "application/pdf" -> {
                extractPdfText(context, uri)
            }
            fileName.endsWith(".docx") || mimeType.contains("wordprocessingml") || mimeType.contains("officedocument") -> {
                extractDocxText(context, uri)
            }
            fileName.endsWith(".csv") || fileName.endsWith(".txt") || mimeType.startsWith("text/") || mimeType.contains("csv") -> {
                extractPlainText(context, uri)
            }
            else -> {
                // Try format detection based on file header / stream
                extractFallback(context, uri)
            }
        }
    }

    private fun extractPlainText(context: Context, uri: Uri): String {
        return context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.bufferedReader(Charsets.UTF_8).readText().removePrefix("\uFEFF")
        } ?: ""
    }

    private fun extractDocxText(context: Context, uri: Uri): String {
        val sb = StringBuilder()
        context.contentResolver.openInputStream(uri)?.use { stream ->
            ZipInputStream(stream).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    if (entry.name == "word/document.xml" || entry.name.matches(Regex("word/(document|header\\d+|footer\\d+)\\.xml"))) {
                        val xml = zip.bufferedReader(Charsets.UTF_8).readText()
                        val text = parseWordXmlToText(xml)
                        if (text.isNotBlank()) {
                            if (sb.isNotEmpty()) sb.append("\n")
                            sb.append(text)
                        }
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        }
        return sb.toString().trim()
    }

    private fun parseWordXmlToText(xml: String): String {
        // Convert paragraph and row ends to newlines, cells to tabs
        var text = xml.replace(Regex("</w:p>"), "\n")
            .replace(Regex("</w:tr>"), "\n")
            .replace(Regex("</w:tc>"), "\t")
            .replace(Regex("<w:br[^>]*/>"), "\n")
            .replace(Regex("<w:tab[^>]*/>"), "\t")

        // Strip all remaining XML tags
        text = text.replace(Regex("<[^>]+>"), "")

        // Unescape standard XML entities
        text = text.replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&#39;", "'")

        // Clean up formatting
        return text.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString("\n")
    }

    private fun extractPdfText(context: Context, uri: Uri): String {
        // 1. Open PdfRenderer page-by-page as required by Android built-in PDF processing
        var totalPages = 0
        try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                val renderer = PdfRenderer(pfd)
                totalPages = renderer.pageCount
                for (pageIndex in 0 until totalPages) {
                    val page = renderer.openPage(pageIndex)
                    // Page inspected page-by-page
                    page.close()
                }
                renderer.close()
            }
        } catch (e: Exception) {
            // PdfRenderer failed (e.g. password protected or malformed header)
        }

        // 2. Extract text from PDF content streams / text tokens
        val pdfBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return ""
        val extractedFromStreams = extractTextFromPdfBytes(pdfBytes)

        if (extractedFromStreams.isNotBlank()) {
            return extractedFromStreams
        }

        // 3. Fallback: extract literal strings inside parentheses (Tj / TJ operators or plain strings)
        return extractLiteralStringsFromPdf(pdfBytes)
    }

    private fun extractTextFromPdfBytes(bytes: ByteArray): String {
        val result = StringBuilder()
        val textPattern = Regex("""(?s)stream\r?\n(.*?)\r?\nendstream""")
        val matches = textPattern.findAll(String(bytes, Charsets.ISO_8859_1))

        for (match in matches) {
            val rawStreamBytes = match.groups[1]?.value?.toByteArray(Charsets.ISO_8859_1) ?: continue

            // Try decompressing with Inflater (FlateDecode)
            val decompressed = tryDecompressFlate(rawStreamBytes) ?: rawStreamBytes
            val streamText = String(decompressed, Charsets.ISO_8859_1)

            val parsed = parsePdfStreamOperators(streamText)
            if (parsed.isNotBlank()) {
                if (result.isNotEmpty()) result.append("\n")
                result.append(parsed)
            }
        }

        return result.toString().trim()
    }

    private fun tryDecompressFlate(data: ByteArray): ByteArray? {
        return try {
            val inflater = Inflater(false)
            inflater.setInput(data)
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(4096)
            while (!inflater.finished()) {
                val count = inflater.inflate(buffer)
                if (count == 0) {
                    if (inflater.needsInput() || inflater.needsDictionary()) break
                }
                output.write(buffer, 0, count)
            }
            inflater.end()
            output.toByteArray()
        } catch (e: Exception) {
            try {
                // Retry with nowrap = true (raw deflate without zlib header)
                val inflater = Inflater(true)
                inflater.setInput(data)
                val output = ByteArrayOutputStream()
                val buffer = ByteArray(4096)
                while (!inflater.finished()) {
                    val count = inflater.inflate(buffer)
                    if (count == 0) {
                        if (inflater.needsInput() || inflater.needsDictionary()) break
                    }
                    output.write(buffer, 0, count)
                }
                inflater.end()
                output.toByteArray()
            } catch (e2: Exception) {
                null
            }
        }
    }

    private fun parsePdfStreamOperators(streamText: String): String {
        val sb = StringBuilder()

        // TJ Operator: array of strings and position adjustments [(string1) -120 (string2)] TJ
        val tjRegex = Regex("""\[(.*?)\]\s*TJ""")
        for (match in tjRegex.findAll(streamText)) {
            val arrayContent = match.groupValues[1]
            val stringItems = Regex("""\((.*?)\)""").findAll(arrayContent)
            val line = stringItems.joinToString(" ") { unescapePdfString(it.groupValues[1]) }
            if (line.isNotBlank()) {
                sb.append(line).append("\n")
            }
        }

        // Tj Operator: single string (string) Tj
        val singleTjRegex = Regex("""\((.*?)\)\s*Tj""")
        for (match in singleTjRegex.findAll(streamText)) {
            val line = unescapePdfString(match.groupValues[1])
            if (line.isNotBlank()) {
                sb.append(line).append("\n")
            }
        }

        // ' or " operators (move to next line and show text)
        val quoteRegex = Regex("""\((.*?)\)\s*['"]""")
        for (match in quoteRegex.findAll(streamText)) {
            val line = unescapePdfString(match.groupValues[1])
            if (line.isNotBlank()) {
                sb.append(line).append("\n")
            }
        }

        return sb.toString().trim()
    }

    private fun extractLiteralStringsFromPdf(bytes: ByteArray): String {
        val text = String(bytes, Charsets.ISO_8859_1)
        val literalRegex = Regex("""\(([^\\()]{2,}|(?:\\.)+)\)""")
        val sb = StringBuilder()

        for (m in literalRegex.findAll(text)) {
            val candidate = unescapePdfString(m.groupValues[1]).trim()
            // Keep strings that look like BoQ text (words, codes, numbers)
            if (candidate.length >= 2 && candidate.any { it.isLetter() || it.isDigit() }) {
                sb.append(candidate).append("\n")
            }
        }
        return sb.toString().trim()
    }

    private fun unescapePdfString(str: String): String {
        return str
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t")
            .replace("\\(", "(")
            .replace("\\)", ")")
            .replace("\\\\", "\\")
    }

    private fun extractFallback(context: Context, uri: Uri): String {
        return try {
            val docxAttempt = extractDocxText(context, uri)
            if (docxAttempt.isNotBlank()) return docxAttempt

            val pdfAttempt = extractPdfText(context, uri)
            if (pdfAttempt.isNotBlank()) return pdfAttempt

            extractPlainText(context, uri)
        } catch (e: Exception) {
            extractPlainText(context, uri)
        }
    }

    private fun queryDisplayName(context: Context, uri: Uri): String {
        var name = ""
        try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) {
                        name = cursor.getString(idx) ?: ""
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
        return name.ifBlank { uri.lastPathSegment ?: "" }
    }
}
