package com.crattendance.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
import java.io.File
import java.io.FileWriter

/** Intent helpers: clipboard, WhatsApp, dialer, export/share. */
object IntentHelper {

    private const val WHATSAPP_PACKAGE = "com.whatsapp"
    private const val WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b"

    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService<ClipboardManager>()
        clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(context, "Copied: $text", Toast.LENGTH_SHORT).show()
    }

    /** Normalizes a Pakistani number for `wa.me`: `+92…` or `03…` → `92…`. */
    fun whatsappId(number: String): String {
        val digits = number.filter(Char::isDigit)
        return when {
            digits.startsWith("00") -> digits.drop(2)
            digits.startsWith("0") -> "92" + digits.drop(1)
            else -> digits
        }
    }

    /** Opens WhatsApp directly (falls back to WhatsApp Business, then the browser). */
    fun openWhatsApp(context: Context, number: String) {
        val id = whatsappId(number)
        if (id.isEmpty()) {
            Toast.makeText(context, "No valid number", Toast.LENGTH_SHORT).show()
            return
        }
        val uri = Uri.parse("https://wa.me/$id")
        val direct = Intent(Intent.ACTION_VIEW, uri).setPackage(WHATSAPP_PACKAGE)
        val business = Intent(Intent.ACTION_VIEW, uri).setPackage(WHATSAPP_BUSINESS_PACKAGE)
        val generic = Intent(Intent.ACTION_VIEW, uri)
        val intent = sequenceOf(direct, business, generic)
            .firstOrNull { it.resolveActivity(context.packageManager) != null }
        if (intent == null) {
            Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
        } else {
            context.startActivity(intent)
        }
    }

    /** Opens the dialer with the number pre-filled (ACTION_DIAL needs no permission). */
    fun openDialer(context: Context, number: String) {
        if (number.isBlank()) {
            Toast.makeText(context, "No phone number saved", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(number)}"))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No dialer available", Toast.LENGTH_SHORT).show()
        }
    }

    /** ACTION_CREATE_DOCUMENT intent that opens the standard "save as" picker. */
    fun exportCsvIntent(fileName: String): Intent =
        Intent(Intent.ACTION_CREATE_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("text/csv")
            .putExtra(Intent.EXTRA_TITLE, fileName)

    /** Writes the CSV into the app cache and returns a FileProvider URI for sharing. */
    fun writeCsvToCache(context: Context, fileName: String, content: String): Uri {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, fileName)
        FileWriter(file).use { it.write(content) }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    /** Opens the Android share sheet with the CSV attached (WhatsApp, Email, Drive…). */
    fun shareCsv(context: Context, fileName: String, content: String) {
        val uri = writeCsvToCache(context, fileName, content)
        val send = Intent(Intent.ACTION_SEND)
            .setType("text/csv")
            .putExtra(Intent.EXTRA_STREAM, uri)
            .putExtra(Intent.EXTRA_SUBJECT, fileName)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(send, "Share Attendance"))
    }

    /** Writes the CSV to the Downloads directory (used by tests / non-picker export). */
    fun writeCsvToDownloads(context: Context, fileName: String, content: String): File {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        dir.mkdirs()
        val file = File(dir, fileName)
        FileWriter(file).use { it.write(content) }
        return file
    }
}
