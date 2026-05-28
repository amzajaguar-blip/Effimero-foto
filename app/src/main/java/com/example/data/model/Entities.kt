package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String = "local_priest",
    val username: String = "Massimo",
    val ashBalance: Int = 120, // Start with some initial balance so they can play around immediately!
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val totalBurns: Int = 0,
    val monumentTier: Int = 1, // 0 = rubble, 1 = stone, 2 = bronze, 3 = silver, 4 = gold, 5 = phoenix
    val lastBurnAt: Long? = null,
    val isFireproof: Boolean = false,
    val eternalFlameActive: Boolean = false
)

@Entity(tableName = "burns")
data class BurnEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val mediaType: String, // "photo", "voice", "text", "video"
    val title: String,
    val description: String, // original description
    val ashEarned: Int,
    val streakMultiplier: Double,
    val emotionalScore: Int,
    val ageDays: Int,
    val fileSizeKb: Int,
    val burnedAt: Long = System.currentTimeMillis(),
    val ghostDescription: String, // mystical description returned or simulated
    val isResurrected: Boolean = false,
    val isTrueBurn: Boolean = true
)

@Entity(tableName = "monument_blocks")
data class MonumentBlockEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val blockType: String, // "foundation", "pillar", "arch", "altar", "phoenix_statue"
    val material: String = "stone", // "stone", "gold", "obsidian"
    val positionIndex: Int, // layout order index
    val placedAt: Long = System.currentTimeMillis()
)
