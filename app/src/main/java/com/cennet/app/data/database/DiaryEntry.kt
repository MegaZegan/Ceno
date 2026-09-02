package com.cennet.app.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diary_entries")
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val text: String,
    val photoUri: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
