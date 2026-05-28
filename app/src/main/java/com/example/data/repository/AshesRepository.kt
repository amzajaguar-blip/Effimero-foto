package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.data.api.AshSacrificeDetails
import com.example.data.api.GeminiContent
import com.example.data.api.GeminiGenerationConfig
import com.example.data.api.GeminiPart
import com.example.data.api.GeminiRequest
import com.example.data.api.RetrofitClient
import com.example.data.local.AshesDao
import com.example.data.model.BurnEntity
import com.example.data.model.MonumentBlockEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import org.json.JSONObject
import java.util.UUID

class AshesRepository(private val dao: AshesDao) {

    val userFlow: Flow<UserEntity?> = dao.getUserFlow()
    val allBurnsFlow: Flow<List<BurnEntity>> = dao.getAllBurns()
    val monumentBlocksFlow: Flow<List<MonumentBlockEntity>> = dao.getMonumentBlocks()

    // Ensure we have a local user initialized
    suspend fun ensureUserInitialized() {
        val user = dao.getUserSync()
        if (user == null) {
            dao.insertUser(UserEntity())
        }
    }

    suspend fun saveUser(user: UserEntity) {
        dao.insertUser(user)
    }

    suspend fun clearMonument() {
        dao.clearAllBlocks()
        val user = dao.getUserSync() ?: UserEntity()
        dao.insertUser(user.copy(monumentTier = 0)) // Set/reset to Rubble
    }

    suspend fun rebuildMonument() {
        val user = dao.getUserSync() ?: return
        if (user.ashBalance >= 200) {
            dao.insertUser(user.copy(ashBalance = user.ashBalance - 200, monumentTier = 1))
            // Re-add a base block
            dao.insertBlock(
                MonumentBlockEntity(
                    id = UUID.randomUUID().toString(),
                    blockType = "foundation",
                    material = "stone",
                    positionIndex = 0
                )
            )
        }
    }

    suspend fun purchaseBlock(blockType: String, cost: Int): Boolean {
        val user = dao.getUserSync() ?: return false
        if (user.ashBalance < cost) return false

        // Fetch current active blocks to get position index
        val currentBlocks = dao.getMonumentBlocks().firstOrNull() ?: emptyList()
        val index = currentBlocks.size

        // Streak check: custom gold textures if currentStreak >= 7
        val isGold = user.currentStreak >= 7
        val material = if (isGold) "gold" else "stone"

        dao.insertBlock(
            MonumentBlockEntity(
                id = UUID.randomUUID().toString(),
                blockType = blockType,
                material = material,
                positionIndex = index
            )
        )

        // Deduct Ash
        dao.insertUser(user.copy(ashBalance = user.ashBalance - cost))
        return true
    }

    suspend fun recordSimulatedPurchase(ashBonus: Int) {
        val user = dao.getUserSync() ?: return
        dao.insertUser(user.copy(ashBalance = user.ashBalance + ashBonus))
    }

    suspend fun handleStreakAndDecay() {
        // Every day this is called, we verify if user missed their daily sacrifice
        val user = dao.getUserSync() ?: return
        val lastBurn = user.lastBurnAt ?: return
        val elapsedMs = System.currentTimeMillis() - lastBurn
        val elapsedHours = elapsedMs / (1000 * 60 * 60)

        if (elapsedHours > 36) { // Missed the 24-36h window
            // Reset streak
            val updatedUser = user.copy(
                currentStreak = 0,
                monumentTier = if (user.monumentTier > 0) user.monumentTier - 1 else 0
            )
            dao.insertUser(updatedUser)

            // Crumble a block if any
            val blocks = dao.getMonumentBlocks().firstOrNull() ?: emptyList()
            if (blocks.isNotEmpty()) {
                dao.deleteBlockById(blocks.last().id)
            }
        }
    }

    suspend fun burnMemory(
        mediaType: String,
        title: String,
        details: String,
        fileSizeKb: Int,
        ageDays: Int,
        isTrueBurn: Boolean
    ): BurnResult {
        val user = dao.getUserSync() ?: UserEntity()

        // 1. Calculate Streak multiplier
        val multiplier = when {
            user.currentStreak == 0 -> 1.0
            user.currentStreak < 3 -> 1.5
            user.currentStreak < 7 -> 2.0
            user.currentStreak < 14 -> 2.5
            else -> 3.0
        }

        // 2. Local Fallback scoring calculation matching formula
        val baseAsh = 10
        val ageBonus = if (ageDays >= 1095) 50 else if (ageDays >= 365) 25 else 0
        val sizeBonus = if (fileSizeKb >= 10240) 15 else (fileSizeKb / 1024).coerceIn(0, 20)
        
        // Detect keyword emotions
        val hasEmotionalKeyword = details.uppercase().contains("LOVE") || 
                details.uppercase().contains("MOM") || 
                details.uppercase().contains("DAD") || 
                details.uppercase().contains("BABY") || 
                details.uppercase().contains("SORRY")
        val emotionBonus = if (hasEmotionalKeyword) 30 else 10
        val facesDetected = details.uppercase().contains("FACE") || details.uppercase().contains("PORTRAIT")

        val expectedEarnedRaw = (baseAsh + ageBonus + sizeBonus + (if (facesDetected) 30 else 0) + (if (hasEmotionalKeyword) 20 else 0)) * multiplier
        val computedAsh = expectedEarnedRaw.toInt()

        // 3. Let's invocation Gemini API to create a deeply emotional poetic epitaph and ghost memory outline
        val apiKey = BuildConfig.GEMINI_API_KEY
        var sacrificeDetails: AshSacrificeDetails? = null

        if (apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val prompt = """
                    You are the Grim Reaper and Priest of the digital sacrifice ritual app ASHES.
                    The user is permanently destroying a $mediaType memory titled "$title" with description/contents: "$details".
                    File Age: $ageDays days, File Size: $fileSizeKb KB.

                    Respond with a JSON block containing EXACTLY these fields (no other text, no markdown other than pure JSON):
                    {
                      "poeticEpitaph": "A deeply poetic, ritualistic, ominous, funeral-like text reflecting on this loss (2 sentences max). Speak of ash and fire.",
                      "ghostShadow": "A pixelated, blurred, voyeuristic description of what this memory looked/sounded like as a shadow.",
                      "emotionalScore": val (0 to 100 on how emotional this memory feels),
                      "detectedFaces": true/false (based on the contents),
                      "calculatedAsh": $computedAsh
                    }
                """.trimIndent()

                val request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(parts = listOf(GeminiPart(text = prompt)))
                    ),
                    generationConfig = GeminiGenerationConfig(
                        responseMimeType = "application/json",
                        temperature = 0.85f
                    ),
                    systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = "You are ASHES, the digital pyre. Ominous, ancient, luxury visual arts.")))
                )

                val response = RetrofitClient.apiService.generateContent(apiKey, request)
                val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!responseText.isNullOrEmpty()) {
                    // Extract JSON block even if model wrapped it in ```json ... ```
                    val jsonStr = extractJsonStringByForce(responseText)
                    val adapter = RetrofitClient.moshiParser.adapter(AshSacrificeDetails::class.java)
                    sacrificeDetails = adapter.fromJson(jsonStr)
                }
            } catch (e: Exception) {
                Log.e("AshesRepository", "Gemini API failed, using native logic.", e)
            }
        }

        // 4. Create final response from either Gemini or simulated local fallback
        val finalDetails = sacrificeDetails ?: AshSacrificeDetails(
            poeticEpitaph = "We feed the pyre with \"$title\". Its memory burns into fine particles, floating into the carbon dark sky.",
            ghostShadow = "A pixelated silhouette of $mediaType memory, forever obscured by digital carbon.",
            emotionalScore = if (hasEmotionalKeyword) 85 else 45,
            detectedFaces = facesDetected,
            calculatedAsh = computedAsh
        )

        // 5. Update user state (balance, streak, count)
        val newStreak = user.currentStreak + 1
        var addedBonus = 0
        if (newStreak == 7) {
            addedBonus = 100 // Day 7 Gold Bonus
        } else if (newStreak == 30) {
            addedBonus = 500
        }

        val totalEarned = finalDetails.calculatedAsh + addedBonus
        val updatedUser = user.copy(
            ashBalance = user.ashBalance + totalEarned,
            currentStreak = newStreak,
            maxStreak = maxOf(user.maxStreak, newStreak),
            totalBurns = user.totalBurns + 1,
            lastBurnAt = System.currentTimeMillis()
        )
        dao.insertUser(updatedUser)

        // 6. Save the new BurnEntity
        val newBurn = BurnEntity(
            mediaType = mediaType,
            title = title,
            description = details,
            ashEarned = totalEarned,
            streakMultiplier = multiplier,
            emotionalScore = finalDetails.emotionalScore,
            ageDays = ageDays,
            fileSizeKb = fileSizeKb,
            ghostDescription = finalDetails.ghostShadow,
            isTrueBurn = isTrueBurn
        )
        dao.insertBurn(newBurn)

        return BurnResult(
            burn = newBurn,
            epitaph = finalDetails.poeticEpitaph,
            ghostDescription = finalDetails.ghostShadow,
            totalEarned = totalEarned,
            streakBonus = addedBonus > 0
        )
    }

    private fun extractJsonStringByForce(response: String): String {
        val trimmed = response.trim()
        if (trimmed.startsWith("```json")) {
            val endIdx = trimmed.lastIndexOf("```")
            if (endIdx > 7) {
                return trimmed.substring(7, endIdx).trim()
            }
        } else if (trimmed.startsWith("```")) {
            val endIdx = trimmed.lastIndexOf("```")
            if (endIdx > 3) {
                return trimmed.substring(3, endIdx).trim()
            }
        }
        return trimmed
    }
}

data class BurnResult(
    val burn: BurnEntity,
    val epitaph: String,
    val ghostDescription: String,
    val totalEarned: Int,
    val streakBonus: Boolean
)
