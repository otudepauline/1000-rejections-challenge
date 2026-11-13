package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "asks")
data class AskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String,
    val dateEpochMillis: Long,
    val notes: String = "",
    val outcome: String, // AskOutcome name
    val takeaway: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
