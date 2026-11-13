package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.local.AskDao
import com.example.data.local.AskEntity
import com.example.data.model.AskOutcome
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AskRepository(private val askDao: AskDao) {

    val allAsks: Flow<List<AskEntity>> = askDao.getAllAsks()

    fun getAskById(id: Long): Flow<AskEntity?> = askDao.getAskById(id)

    suspend fun getAskByIdOnce(id: Long): AskEntity? = askDao.getAskByIdOnce(id)

    suspend fun insert(ask: AskEntity): Long = askDao.insertAsk(ask)

    suspend fun update(ask: AskEntity) = askDao.updateAsk(ask)

    suspend fun delete(ask: AskEntity) = askDao.deleteAsk(ask)

    suspend fun deleteById(id: Long) = askDao.deleteAskById(id)

    suspend fun insertAll(asks: List<AskEntity>) = askDao.insertAll(asks)

    suspend fun clearAll() = askDao.deleteAll()

    suspend fun exportToCsv(asks: List<AskEntity>): String = withContext(Dispatchers.Default) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sb = StringBuilder()
        sb.append("ID,Title,Category,Date,Outcome,Notes,Takeaway\n")
        for (ask in asks) {
            val dateStr = dateFormat.format(Date(ask.dateEpochMillis))
            val escapedTitle = escapeCsv(ask.title)
            val escapedCategory = escapeCsv(ask.category)
            val escapedNotes = escapeCsv(ask.notes)
            val escapedTakeaway = escapeCsv(ask.takeaway)
            sb.append("${ask.id},$escapedTitle,$escapedCategory,$dateStr,${ask.outcome},$escapedNotes,$escapedTakeaway\n")
        }
        sb.toString()
    }

    suspend fun exportToJson(asks: List<AskEntity>): String = withContext(Dispatchers.Default) {
        val root = JSONObject()
        root.put("app", "1000 Rejections")
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val array = JSONArray()
        for (ask in asks) {
            val item = JSONObject()
            item.put("id", ask.id)
            item.put("title", ask.title)
            item.put("category", ask.category)
            item.put("dateEpochMillis", ask.dateEpochMillis)
            item.put("notes", ask.notes)
            item.put("outcome", ask.outcome)
            item.put("takeaway", ask.takeaway)
            item.put("createdAt", ask.createdAt)
            array.put(item)
        }
        root.put("asks", array)
        root.toString(2)
    }

    suspend fun importFromJson(jsonString: String): Int = withContext(Dispatchers.Default) {
        val root = JSONObject(jsonString)
        val array = root.getJSONArray("asks")
        val imported = mutableListOf<AskEntity>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val ask = AskEntity(
                id = 0, // Reset ID to let Room auto-generate or append
                title = obj.optString("title", "Untitled Ask"),
                category = obj.optString("category", "Other"),
                dateEpochMillis = obj.optLong("dateEpochMillis", System.currentTimeMillis()),
                notes = obj.optString("notes", ""),
                outcome = obj.optString("outcome", AskOutcome.PENDING.name),
                takeaway = obj.optString("takeaway", ""),
                createdAt = obj.optLong("createdAt", System.currentTimeMillis())
            )
            imported.add(ask)
        }
        if (imported.isNotEmpty()) {
            askDao.insertAll(imported)
        }
        imported.size
    }

    suspend fun shareBackup(context: Context, asks: List<AskEntity>, format: String = "csv") {
        withContext(Dispatchers.IO) {
            val dateStr = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
            val filename = if (format == "json") "1000_rejections_backup_$dateStr.json" else "1000_rejections_backup_$dateStr.csv"
            val content = if (format == "json") exportToJson(asks) else exportToCsv(asks)
            val mimeType = if (format == "json") "application/json" else "text/csv"

            val backupDir = File(context.cacheDir, "backups").apply { mkdirs() }
            val backupFile = File(backupDir, filename).apply {
                writeText(content)
            }

            val uri: Uri = try {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    backupFile
                )
            } catch (e: Exception) {
                // Fallback to direct share with plain text if provider is not configured
                null
            } ?: run {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "1000 Rejections Backup ($dateStr)")
                    putExtra(Intent.EXTRA_TEXT, content)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share 1000 Rejections Backup via WhatsApp or Email").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
                return@withContext
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "1000 Rejections Backup ($dateStr)")
                putExtra(Intent.EXTRA_TEXT, "Here is my backup of 1000 Rejections asks ($dateStr).")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Backup via WhatsApp, Drive, or Files").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        }
    }

    private fun escapeCsv(value: String): String {
        var str = value.replace("\"", "\"\"")
        if (str.contains(",") || str.contains("\n") || str.contains("\r") || str.contains("\"")) {
            str = "\"$str\""
        }
        return str
    }
}
