package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "video_projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val status: String,
    val durationSec: Float,
    val aspectRatio: String,
    val quality: String,
    val autoSuspense: Boolean,
    val cleanAudio: Boolean,
    val autoDucking: Boolean,
    val fetchSfx: Boolean,
    val momentsCount: Int,
    val effectsAppliedText: String,
    val createdAt: Long
)
