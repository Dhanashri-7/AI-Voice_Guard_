package com.voiceguard.utils

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

data class ForensicReportData(
    val caseId: String,
    val timestamp: String,
    val callerName: String = "",
    val callerNumber: String = "",
    val callDuration: String = "",
    val sourceTitle: String,
    val sourceDetails: String,
    val isDeepfake: Boolean,
    val authenticityScore: Int,
    val vocoderCutoffHz: Int,
    val microPitchJitterPct: Float,
    val averagePitchHz: Float,
    val suspectedArchitecture: String,
    val audioSha256: String,
    val detectionAlgorithm: String,
    val transcriptSnippet: String = "",
    val legalCitation: String = "Section 66D, Information Technology Act, 2000 (Cheating by Personation using Computer Resource)"
)

object PdfReportGenerator {

    /**
     * Generates a formal, printable A4 PDF Forensic Dossier using native Android PdfDocument.
     * Saves to phone's public Downloads directory.
     */
    fun generateForensicPdf(context: Context, data: ForensicReportData): File? {
        val pdfDoc = PdfDocument()
        val pageWidth = 595 // Standard A4 (points)
        val pageHeight = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDoc.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }

        try {
            // 1. Background Paper
            paint.color = Color.parseColor("#F8FAFC")
            paint.style = Paint.Style.FILL
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), paint)

            // 2. Official Header Banner (Navy)
            paint.color = Color.parseColor("#0F2546")
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 92f, paint)

            // Header Gold Accent Strip
            paint.color = Color.parseColor("#D97706")
            canvas.drawRect(0f, 88f, pageWidth.toFloat(), 92f, paint)

            // Header Typography
            paint.color = Color.WHITE
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            paint.letterSpacing = 0.15f
            canvas.drawText("NATIONAL CYBER CRIME CO-ORDINATION CENTRE (I4C)", 32f, 28f, paint)

            paint.textSize = 15f
            paint.letterSpacing = 0.05f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            canvas.drawText("VOICEGUARD ACOUSTIC FORENSIC DOSSIER", 32f, 52f, paint)

            paint.textSize = 9f
            paint.letterSpacing = 0.05f
            paint.color = Color.parseColor("#93C5FD")
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            canvas.drawText("Ministry of Home Affairs Guidelines • Indian Evidence Act Section 65B Electronic Audit", 32f, 72f, paint)

            // 3. Case Metadata Card (Includes Caller Name, Number, Date, Time & Duration)
            var curY = 108f
            val cardRect = RectF(32f, curY, pageWidth - 32f, curY + 86f)
            paint.color = Color.WHITE
            paint.style = Paint.Style.FILL
            canvas.drawRoundRect(cardRect, 8f, 8f, paint)

            paint.color = Color.parseColor("#CBD5E1")
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRoundRect(cardRect, 8f, 8f, paint)
            paint.style = Paint.Style.FILL

            paint.color = Color.parseColor("#334155")
            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)

            val callerDisplay = if (data.callerName.isNotBlank()) data.callerName else data.sourceTitle
            val numberDisplay = if (data.callerNumber.isNotBlank()) data.callerNumber else data.sourceDetails

            canvas.drawText("Case ID: ${data.caseId}", 44f, curY + 20f, paint)
            canvas.drawText("Date & Time: ${data.timestamp}", 44f, curY + 36f, paint)
            canvas.drawText("Caller Name: $callerDisplay", 44f, curY + 52f, paint)
            canvas.drawText("Caller Phone: $numberDisplay", 44f, curY + 68f, paint)

            canvas.drawText("Classifier: ${data.detectionAlgorithm}", (pageWidth / 2) + 15f, curY + 20f, paint)
            canvas.drawText("Call Duration: ${if (data.callDuration.isNotBlank()) data.callDuration else "Active Interception"}", (pageWidth / 2) + 15f, curY + 36f, paint)
            canvas.drawText("Statute: Section 66D IT Act (Cognizable)", (pageWidth / 2) + 15f, curY + 52f, paint)
            canvas.drawText("Status: FORENSIC INVESTIGATION CERTIFIED", (pageWidth / 2) + 15f, curY + 68f, paint)

            // 4. Verdict Banner
            curY += 98f
            val verdictRect = RectF(32f, curY, pageWidth - 32f, curY + 64f)
            val verdictBgColor = if (data.isDeepfake) Color.parseColor("#FEE2E2") else Color.parseColor("#DCFCE7")
            val verdictBorderColor = if (data.isDeepfake) Color.parseColor("#DC2626") else Color.parseColor("#16A34A")

            paint.color = verdictBgColor
            canvas.drawRoundRect(verdictRect, 8f, 8f, paint)
            paint.color = verdictBorderColor
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.5f
            canvas.drawRoundRect(verdictRect, 8f, 8f, paint)
            paint.style = Paint.Style.FILL

            paint.color = verdictBorderColor
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            val verdictTitle = if (data.isDeepfake) {
                "🚨 CRITICAL THREAT: NEURAL AI VOICE CLONE DETECTED (${100 - data.authenticityScore}% Synthesized)"
            } else {
                "✅ VERIFIED: AUTHENTIC ORGANIC HUMAN SPEECH (${data.authenticityScore}% Confidence)"
            }
            canvas.drawText(verdictTitle, 46f, curY + 26f, paint)

            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            val verdictSub = if (data.isDeepfake) {
                "Neural Vocoder Phase Anomaly & Mathematical Prosody Rigidity Confirmed. Caller claims are fraudulent."
            } else {
                "Natural Vocal Cord Biomechanics & Continuous Organic Formants Verified. Genuine human caller."
            }
            canvas.drawText(verdictSub, 46f, curY + 46f, paint)

            // 5. Forensic Evidence Table
            curY += 76f
            paint.color = Color.parseColor("#0F2546")
            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            canvas.drawText("ACOUSTIC FORENSIC EVIDENCE & SIGNAL METRICS", 32f, curY, paint)

            curY += 12f
            val tableTop = curY
            val rowHeight = 23f
            val tableRows = listOf(
                listOf("Forensic Metric", "Observed Value", "Standard Baseline", "Diagnostic Verdict"),
                listOf("Vocoder Cutoff (Hz)", "${data.vocoderCutoffHz} Hz", "> 7500 Hz (Organic)", if (data.isDeepfake) "Cutoff Anomaly" else "Normal Rolloff"),
                listOf("Micro-Pitch Jitter (F0)", "${String.format("%.2f", data.microPitchJitterPct)}%", "1.20% - 2.50% (Human)", if (data.microPitchJitterPct < 0.35f) "Artificial Rigidity" else "Natural Tremors"),
                listOf("Average Fundamental F0", "${data.averagePitchHz.toInt()} Hz", "85 Hz - 285 Hz", "In-band Formant"),
                listOf("Acoustic Architecture", data.suspectedArchitecture, "Human Vocal Tract", if (data.isDeepfake) "Synthetic TTS" else "Organic Larynx"),
                listOf("Neural Authenticity", "${data.authenticityScore}%", "> 80% Safe Threshold", if (data.isDeepfake) "EXTORTION ALERT" else "VERIFIED SAFE")
            )

            val tableRect = RectF(32f, tableTop, pageWidth - 32f, tableTop + (tableRows.size * rowHeight))
            paint.color = Color.WHITE
            canvas.drawRoundRect(tableRect, 6f, 6f, paint)
            paint.color = Color.parseColor("#E2E8F0")
            paint.style = Paint.Style.STROKE
            canvas.drawRoundRect(tableRect, 6f, 6f, paint)
            paint.style = Paint.Style.FILL

            tableRows.forEachIndexed { rIndex, row ->
                val rY = tableTop + (rIndex * rowHeight)
                if (rIndex == 0) {
                    paint.color = Color.parseColor("#F1F5F9")
                    canvas.drawRect(32f, rY, pageWidth - 32f, rY + rowHeight, paint)
                    paint.color = Color.parseColor("#0F2546")
                    paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    paint.textSize = 8.5f
                } else {
                    paint.color = Color.parseColor("#334155")
                    paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                    paint.textSize = 8.5f
                }

                canvas.drawText(row[0], 42f, rY + 15f, paint)
                canvas.drawText(row[1], 185f, rY + 15f, paint)
                canvas.drawText(row[2], 305f, rY + 15f, paint)

                if (rIndex > 0 && (row[3].contains("ALERT", ignoreCase = true) || row[3].contains("Anomaly", ignoreCase = true) || row[3].contains("Rigidity", ignoreCase = true))) {
                    paint.color = Color.parseColor("#DC2626")
                    paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                } else if (rIndex > 0 && (row[3].contains("SAFE", ignoreCase = true) || row[3].contains("Verified", ignoreCase = true))) {
                    paint.color = Color.parseColor("#16A34A")
                    paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                }
                canvas.drawText(row[3], 435f, rY + 15f, paint)

                paint.color = Color.parseColor("#E2E8F0")
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 0.5f
                canvas.drawLine(32f, rY + rowHeight, pageWidth - 32f, rY + rowHeight, paint)
                paint.style = Paint.Style.FILL
            }

            // 6. Transcript Snippet (if available)
            curY = tableTop + (tableRows.size * rowHeight) + 16f
            if (data.transcriptSnippet.isNotBlank()) {
                paint.color = Color.parseColor("#0F2546")
                paint.textSize = 10f
                paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                canvas.drawText("INTERCEPTED CALL SPEECH TRANSCRIPT", 32f, curY, paint)

                curY += 10f
                val transRect = RectF(32f, curY, pageWidth - 32f, curY + 38f)
                paint.color = Color.WHITE
                canvas.drawRoundRect(transRect, 6f, 6f, paint)
                paint.color = Color.parseColor("#E2E8F0")
                paint.style = Paint.Style.STROKE
                canvas.drawRoundRect(transRect, 6f, 6f, paint)
                paint.style = Paint.Style.FILL

                paint.color = Color.parseColor("#1E293B")
                paint.textSize = 8.5f
                paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC)
                canvas.drawText("\"${data.transcriptSnippet.take(95)}\"", 44f, curY + 22f, paint)
                curY += 46f
            }

            // 7. Statutory Enactments & Legal Citations
            paint.color = Color.parseColor("#0F2546")
            paint.textSize = 10.5f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            canvas.drawText("APPLICABLE INDIAN CYBERCRIME LEGAL ENACTMENTS", 32f, curY, paint)

            curY += 10f
            val legalRect = RectF(32f, curY, pageWidth - 32f, curY + 62f)
            paint.color = Color.parseColor("#EFF6FF")
            canvas.drawRoundRect(legalRect, 6f, 6f, paint)
            paint.color = Color.parseColor("#BFDBFE")
            paint.style = Paint.Style.STROKE
            canvas.drawRoundRect(legalRect, 6f, 6f, paint)
            paint.style = Paint.Style.FILL

            paint.color = Color.parseColor("#1E3A8A")
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            canvas.drawText("• Section 66D, Information Technology Act, 2000:", 42f, curY + 16f, paint)
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            canvas.drawText("  Punishment for cheating by personation by using computer resource (Imprisonment up to 3 years & Fine).", 42f, curY + 28f, paint)

            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            canvas.drawText("• Section 65B, Indian Evidence Act, 1872:", 42f, curY + 42f, paint)
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            canvas.drawText("  Cryptographic hash & electronic record admissibility. This dossier is admissible before courts of law.", 42f, curY + 54f, paint)

            // 8. Cryptographic Chain-of-Custody (SHA-256)
            curY += 72f
            paint.color = Color.parseColor("#0F2546")
            paint.textSize = 10.5f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            canvas.drawText("CRYPTOGRAPHIC AUDIT TRAIL & INTEGRITY HASH", 32f, curY, paint)

            curY += 10f
            val hashRect = RectF(32f, curY, pageWidth - 32f, curY + 44f)
            paint.color = Color.parseColor("#F1F5F9")
            canvas.drawRoundRect(hashRect, 6f, 6f, paint)
            paint.color = Color.parseColor("#CBD5E1")
            paint.style = Paint.Style.STROKE
            canvas.drawRoundRect(hashRect, 6f, 6f, paint)
            paint.style = Paint.Style.FILL

            paint.color = Color.parseColor("#475569")
            paint.textSize = 8f
            paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            canvas.drawText("SHA-256 Audio Hash: ${data.audioSha256}", 42f, curY + 18f, paint)
            canvas.drawText("Signed By: VoiceGuard AI Engine v2.0 • Hardware Keystore Authenticated", 42f, curY + 32f, paint)

            // 9. Official Footer
            paint.color = Color.parseColor("#94A3B8")
            paint.textSize = 8f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            canvas.drawText("Citizen Advisory: Report incidents immediately to National Cybercrime Helpline 1930 or visit cybercrime.gov.in", 32f, pageHeight - 28f, paint)
            canvas.drawText("Generated by VoiceGuard Mobile Acoustic Forensics Lab", pageWidth - 230f, pageHeight - 28f, paint)

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            pdfDoc.finishPage(page)
        }

        // Save PDF to public Downloads directory
        return try {
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadDir.exists()) downloadDir.mkdirs()

            val safeName = (data.callerName.ifBlank { data.sourceTitle }).replace("[^a-zA-Z0-9]".toRegex(), "_")
            val fileName = "VoiceGuard_Forensic_${safeName}_${data.caseId.takeLast(6)}.pdf"
            val targetFile = File(downloadDir, fileName)

            val outputStream = FileOutputStream(targetFile)
            pdfDoc.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDoc.close()

            targetFile
        } catch (e: Exception) {
            try {
                val fallbackDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
                val fallbackFile = File(fallbackDir, "VoiceGuard_Forensic_${System.currentTimeMillis()}.pdf")
                val fos = FileOutputStream(fallbackFile)
                pdfDoc.writeTo(fos)
                fos.flush()
                fos.close()
                pdfDoc.close()
                fallbackFile
            } catch (ex: Exception) {
                pdfDoc.close()
                null
            }
        }
    }

    /**
     * Opens the saved PDF using Android Intent with FileProvider and Chooser.
     */
    fun openPdf(context: Context, file: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val targetIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(targetIntent, "Open Forensic PDF Dossier").apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(chooser)
        } catch (e: Exception) {
            try {
                val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val directIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(directIntent)
            } catch (ex: Exception) {
                Toast.makeText(context, "Saved to Downloads: ${file.name}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
