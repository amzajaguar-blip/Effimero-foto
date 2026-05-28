package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.local.AshesDatabase
import com.example.data.model.BurnEntity
import com.example.data.model.MonumentBlockEntity
import com.example.data.model.UserEntity
import com.example.data.repository.AshesRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.Locale

class AshesViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application,
        AshesDatabase::class.java,
        "ashes_ritual.db"
    ).build()

    private val repository = AshesRepository(db.dao)

    // User state
    val userFlow: StateFlow<UserEntity?> = repository.userFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Historical list of burns
    val burnsFlow: StateFlow<List<BurnEntity>> = repository.allBurnsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active monument blocks
    val monumentBlocksFlow: StateFlow<List<MonumentBlockEntity>> = repository.monumentBlocksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Live state of current burning operation
    private val _burnState = MutableStateFlow<BurnStage>(BurnStage.Idle)
    val burnState: StateFlow<BurnStage> = _burnState.asStateFlow()

    // Cemetery / Friends status simulation
    private val _cemeteryFriends = MutableStateFlow<List<FriendSinner>>(emptyList())
    val cemeteryFriends: StateFlow<List<FriendSinner>> = _cemeteryFriends.asStateFlow()

    // Status notifications for PVP actions
    private val _pvpAlert = MutableStateFlow<String?>(null)
    val pvpAlert: StateFlow<String?> = _pvpAlert.asStateFlow()

    // Auctions state
    private val _auctions = MutableStateFlow<List<AshAuction>>(emptyList())
    val auctionsFlow: StateFlow<List<AshAuction>> = _auctions.asStateFlow()

    private val _auctionNotifications = MutableStateFlow<List<AuctionNotification>>(emptyList())
    val auctionNotificationsFlow: StateFlow<List<AuctionNotification>> = _auctionNotifications.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureUserInitialized()
            repository.handleStreakAndDecay()
            generateMockSinners()
            setupAuctions()
            launchRandomPvpEvents()
            launchAuctionSimulation()
        }
    }

    private fun generateMockSinners() {
        _cemeteryFriends.value = listOf(
            FriendSinner(
                id = "sinner_1",
                username = "Memento_Morte",
                monumentTier = 4,
                blocksCount = 12,
                currentStreak = 18,
                totalBurns = 45,
                ghostMediaType = "photo",
                ghostDescription = "A pixelated outline of an old graduation cap.",
                guessedCorrectly = null,
                isEmbered = false
            ),
            FriendSinner(
                id = "sinner_2",
                username = "Pixel_Slayer",
                monumentTier = 1,
                blocksCount = 3,
                currentStreak = 3,
                totalBurns = 8,
                ghostMediaType = "voice",
                ghostDescription = "A high-frequency green wave outline representing a crying echo.",
                guessedCorrectly = null,
                isEmbered = false
            ),
            FriendSinner(
                id = "sinner_3",
                username = "Clutter_Free_Poet",
                monumentTier = 5,
                blocksCount = 28,
                currentStreak = 42,
                totalBurns = 120,
                ghostMediaType = "text",
                ghostDescription = "A blurred sheet of digital rows with highlighted words 'love/forever'.",
                guessedCorrectly = null,
                isEmbered = false
            ),
            FriendSinner(
                id = "sinner_4",
                username = "Digital_Vagabond",
                monumentTier = 2,
                blocksCount = 7,
                currentStreak = 0, // In decay!
                totalBurns = 14,
                ghostMediaType = "video",
                ghostDescription = "A chaotic pixel-cloud of flashing turquoise and gold.",
                guessedCorrectly = null,
                isEmbered = false
            )
        )
    }

    private fun launchRandomPvpEvents() {
        viewModelScope.launch {
            while (true) {
                delay(90000) // Every 1.5 minutes a companion does something!
                val user = userFlow.value ?: continue
                if (user.totalBurns > 0) {
                    val eventSelector = (0..2).random()
                    when (eventSelector) {
                        0 -> {
                            // Friend guesses your ghost incorrectly
                            _pvpAlert.value = "Clutter_Free_Poet tried to Guess your Ghost but guessed PHOTO instead of TEXT. You pocketed +10 Ash!"
                            repository.saveUser(user.copy(ashBalance = user.ashBalance + 10))
                        }
                        1 -> {
                            // Friend steals some ash
                            if (user.ashBalance >= 15) {
                                _pvpAlert.value = "Pixel_Slayer guessed your latest photo correctly! Steals 15 Ash."
                                repository.saveUser(user.copy(ashBalance = maxOf(0, user.ashBalance - 15)))
                            }
                        }
                        2 -> {
                            _pvpAlert.value = "Your Monument enters decay monitoring. Burn daily to keep it from crumbling!"
                        }
                    }
                }
            }
        }
    }

    fun dismissPvpAlert() {
        _pvpAlert.value = null
    }

    // Spend ash to place a block on user's Monument
    fun buildBlock(blockType: String, cost: Int) {
        viewModelScope.launch {
            val success = repository.purchaseBlock(blockType, cost)
            if (!success) {
                // Not enough Ash or failed
            }
        }
    }

    // Burn a file
    fun initiateBurn(
        mediaType: String,
        title: String,
        details: String,
        fileSizeKb: Int,
        ageDays: Int,
        isTrueBurn: Boolean
    ) {
        viewModelScope.launch {
            _burnState.value = BurnStage.Scoring
            delay(1500) // Simulated ML valuation delay

            try {
                _burnState.value = BurnStage.ScoringDone(
                    mediaType = mediaType,
                    title = title,
                    details = details,
                    fileSizeKb = fileSizeKb,
                    ageDays = ageDays,
                    isTrueBurn = isTrueBurn
                )
            } catch (e: Exception) {
                _burnState.value = BurnStage.Idle
            }
        }
    }

    fun confirmBurn(stage: BurnStage.ScoringDone) {
        viewModelScope.launch {
            _burnState.value = BurnStage.Burning(0f)
            
            // Custom 3-second animated burning timeline progress
            for (progress in 1..30) {
                delay(100)
                _burnState.value = BurnStage.Burning(progress / 30f)
            }

            // Run repository operation to earn Ash and update DB
            val result = repository.burnMemory(
                mediaType = stage.mediaType,
                title = stage.title,
                details = stage.details,
                fileSizeKb = stage.fileSizeKb,
                ageDays = stage.ageDays,
                isTrueBurn = stage.isTrueBurn
            )

            _burnState.value = BurnStage.Burnt(
                burn = result.burn,
                epitaph = result.epitaph,
                ghostDescription = result.ghostDescription,
                totalEarned = result.totalEarned,
                streakBonus = result.streakBonus
            )
        }
    }

    fun resetBurnState() {
        _burnState.value = BurnStage.Idle
    }

    fun clearMonument() {
        viewModelScope.launch {
            repository.clearMonument()
        }
    }

    fun rebuildMonument() {
        viewModelScope.launch {
            repository.rebuildMonument()
        }
    }

    // PVP: Guess guest sinner's ghost media type
    fun guessGhostMediaType(friendId: String, guessedType: String) {
        val friends = _cemeteryFriends.value.toMutableList()
        val index = friends.indexOfFirst { it.id == friendId }
        val user = userFlow.value ?: return

        if (index != -1 && friends[index].guessedCorrectly == null) {
            val friend = friends[index]
            val isCorrect = friend.ghostMediaType == guessedType
            val updatedFriend = if (isCorrect) {
                friend.copy(guessedCorrectly = true)
            } else {
                friend.copy(guessedCorrectly = false)
            }
            friends[index] = updatedFriend
            _cemeteryFriends.value = friends

            viewModelScope.launch {
                val newBalance = if (isCorrect) {
                    user.ashBalance + 25 // Win half of their next theoretical burn payout
                } else {
                    maxOf(0, user.ashBalance - 10) // Lose 10 Ash penalty
                }
                repository.saveUser(user.copy(ashBalance = newBalance))
            }
        }
    }

    // Leave an Ember (+1 Ash like)
    fun leaveEmber(friendId: String) {
        val friends = _cemeteryFriends.value.toMutableList()
        val index = friends.indexOfFirst { it.id == friendId }
        val user = userFlow.value ?: return

        if (index != -1 && !friends[index].isEmbered && user.ashBalance >= 1) {
            val friend = friends[index]
            friends[index] = friend.copy(isEmbered = true)
            _cemeteryFriends.value = friends

            viewModelScope.launch {
                // Cost 1 Ash to leave an Ember, shows connection
                repository.saveUser(user.copy(ashBalance = user.ashBalance - 1))
            }
        }
    }

    // IAP simulation packages
    fun triggerVirtualOffer(productId: String, priceEur: Double, ashBonus: Int) {
        viewModelScope.launch {
            repository.recordSimulatedPurchase(ashBonus)
            _pvpAlert.value = "Ritual Sacrifice completed. Transmuted €$priceEur into +$ashBonus Ash."
        }
    }

    // --- Ash Auctions System ---
    private fun setupAuctions() {
        val initialAuctions = listOf(
            AshAuction(
                id = "auc_1",
                originalBurner = "Memento_Morte",
                mediaType = "photo",
                ghostDescription = "A highly pixelated Polaroid of two silhouettes on a rocky cliff under an amber sun.",
                originalTitle = "Midnight Peak Secret",
                originalDetails = "Me and Sophie shared our first deep secret at midnight after escaping graduation on Mt. Tamalpais.",
                currentHighestBid = 60,
                highestBidder = "Pixel_Slayer",
                timeLeftLabel = "23h 42m"
            ),
            AshAuction(
                id = "auc_2",
                originalBurner = "Clutter_Free_Poet",
                mediaType = "voice",
                ghostDescription = "A highly pixelated green frequency wave outline representing a soft whispering echo.",
                originalTitle = "Grandmother's Last Voicemail",
                originalDetails = "Last 7 seconds of Grandmother's soft voice telling me to stay safe and write poems before she passed.",
                currentHighestBid = 110,
                highestBidder = "Memento_Morte",
                timeLeftLabel = "21h 05m"
            )
        )
        
        viewModelScope.launch {
            burnsFlow.collect { list ->
                val current = _auctions.value
                if (current.isEmpty() && list.isNotEmpty()) {
                    val latestUserBurn = list.first()
                    val userAuction = AshAuction(
                        id = "auc_user_1",
                        originalBurner = "You (Massimo)",
                        mediaType = latestUserBurn.mediaType,
                        ghostDescription = "An ominous pixelated matrix with terms: ${latestUserBurn.title.take(minOf(6, latestUserBurn.title.length))}...",
                        originalTitle = latestUserBurn.title,
                        originalDetails = latestUserBurn.description,
                        currentHighestBid = 80,
                        highestBidder = "Clutter_Free_Poet",
                        timeLeftLabel = "22h 30m"
                    )
                    _auctions.value = initialAuctions + userAuction
                    addAuctionNotification("Your burned memory '${latestUserBurn.title}' has been highlighted in the weekly Ash Auction! Bidding has started.")
                } else if (current.isEmpty()) {
                    _auctions.value = initialAuctions
                }
            }
        }
    }

    fun addAuctionNotification(msg: String) {
        val current = _auctionNotifications.value.toMutableList()
        current.add(0, AuctionNotification(message = msg))
        _auctionNotifications.value = current.take(50)
    }

    fun placeBid(auctionId: String, amount: Int) {
        val user = userFlow.value ?: return
        if (amount <= 0) return
        if (user.ashBalance < amount) {
            _pvpAlert.value = "You do not have enough Ash (requires $amount ASH)!"
            return
        }

        val list = _auctions.value.map { auc ->
            if (auc.id == auctionId) {
                if (amount <= auc.currentHighestBid) {
                    _pvpAlert.value = "Your bid must be higher than the current highest bid of ${auc.currentHighestBid} ASH!"
                    return
                }
                
                auc.copy(
                    currentHighestBid = amount,
                    highestBidder = "You",
                    bidCount = auc.bidCount + 1,
                    userBids = auc.userBids + amount
                )
            } else {
                auc
            }
        }

        _auctions.value = list
        addAuctionNotification("You placed a bid of $amount Ash on the Ghost Memory!")

        viewModelScope.launch {
            val updatedUser = userFlow.value ?: return@launch
            val auc = _auctions.value.find { it.id == auctionId }
            var refundAmount = 0
            if (auc != null && auc.userBids.size > 1) {
                refundAmount = auc.userBids.dropLast(1).lastOrNull() ?: 0
            }
            val netCost = amount - refundAmount
            repository.saveUser(updatedUser.copy(ashBalance = maxOf(0, updatedUser.ashBalance - netCost)))
        }
    }

    fun resolveAuction(auctionId: String) {
        val user = userFlow.value ?: return
        val list = _auctions.value.map { auc ->
            if (auc.id == auctionId) {
                if (auc.isResolved) return@map auc

                val winningBid = auc.currentHighestBid
                val winner = auc.highestBidder

                if (winner == "You") {
                    addAuctionNotification("🏆 Congratulations! You won the auction for ${auc.originalTitle} with a bid of $winningBid Ash!")
                    _pvpAlert.value = "You won! The pixelated Ghost Memory has been revealed: \"${auc.originalTitle}\". Bidded $winningBid Ash. 30% retained by app, 70% goes to burner."
                    auc.copy(isResolved = true, isWonByMe = true, timeLeftLabel = "ENDED")
                } else {
                    val originalBurnerReceived = (winningBid * 0.7).toInt()
                    val msg = "Settle: $winner won the auction for the Ghost of ${auc.originalBurner} at $winningBid Ash!"
                    addAuctionNotification(msg)

                    if (auc.originalBurner.contains("You", ignoreCase = true)) {
                        viewModelScope.launch {
                            val currentUser = userFlow.value ?: return@launch
                            repository.saveUser(currentUser.copy(ashBalance = currentUser.ashBalance + originalBurnerReceived))
                            _pvpAlert.value = "Your memory was purchased by $winner for $winningBid Ash! You received your 70% share (+$originalBurnerReceived Ash)!"
                        }
                    }

                    val lastUserBid = auc.userBids.lastOrNull()
                    if (lastUserBid != null) {
                        viewModelScope.launch {
                            val currentUser = userFlow.value ?: return@launch
                            repository.saveUser(currentUser.copy(ashBalance = currentUser.ashBalance + lastUserBid))
                            addAuctionNotification("Refunded $lastUserBid Ash for your outbid/lost auction.")
                        }
                    }

                    auc.copy(isResolved = true, isWonByMe = false, timeLeftLabel = "ENDED")
                }
            } else {
                auc
            }
        }
        _auctions.value = list
    }

    private fun launchAuctionSimulation() {
        viewModelScope.launch {
            while (true) {
                delay(30000) // Trigger every 30 seconds
                val current = _auctions.value.toMutableList()
                if (current.isEmpty()) continue

                val unresolvedIndices = current.indices.filter { !current[it].isResolved }
                if (unresolvedIndices.isEmpty()) continue

                val randomIndex = unresolvedIndices.random()
                val auction = current[randomIndex]

                val potentialBidders = listOf("Memento_Morte", "Pixel_Slayer", "Clutter_Free_Poet", "Digital_Vagabond")
                    .filter { it != auction.originalBurner }
                if (potentialBidders.isEmpty()) continue
                val companion = potentialBidders.random()

                val raiseAmount = (10..30).random()
                val nextBid = auction.currentHighestBid + raiseAmount

                if (auction.highestBidder == "You") {
                    if (kotlin.random.Random.nextDouble() > 0.6 || auction.currentHighestBid > 400) {
                        continue
                    }
                }

                if (auction.highestBidder == "You") {
                    val userLatestBid = auction.userBids.lastOrNull()
                    if (userLatestBid != null) {
                        val currentUser = userFlow.value
                        if (currentUser != null) {
                            repository.saveUser(currentUser.copy(ashBalance = currentUser.ashBalance + userLatestBid))
                        }
                        addAuctionNotification("⚠️ You were outbid by $companion on '${auction.mediaType.uppercase(Locale.ROOT)}'! Refunded $userLatestBid Ash.")
                    }
                }

                val updatedAuction = auction.copy(
                    currentHighestBid = nextBid,
                    highestBidder = companion,
                    bidCount = auction.bidCount + 1
                )
                current[randomIndex] = updatedAuction
                _auctions.value = current

                addAuctionNotification("🔔 $companion bid $nextBid Ash on '${auction.mediaType.uppercase(Locale.ROOT)}' Ghost!")
            }
        }
    }
}

// Stages of the Sacrifice Loop workflow
sealed interface BurnStage {
    object Idle : BurnStage
    object Scoring : BurnStage
    data class ScoringDone(
        val mediaType: String,
        val title: String,
        val details: String,
        val fileSizeKb: Int,
        val ageDays: Int,
        val isTrueBurn: Boolean
    ) : BurnStage
    data class Burning(val progress: Float) : BurnStage
    data class Burnt(
        val burn: BurnEntity,
        val epitaph: String,
        val ghostDescription: String,
        val totalEarned: Int,
        val streakBonus: Boolean
    ) : BurnStage
}

data class FriendSinner(
    val id: String,
    val username: String,
    val monumentTier: Int,
    val blocksCount: Int,
    val currentStreak: Int,
    val totalBurns: Int,
    val ghostMediaType: String,
    val ghostDescription: String,
    val guessedCorrectly: Boolean?, // true: correct, false: wrong, null: not guessed
    val isEmbered: Boolean
)

data class AshAuction(
    val id: String,
    val originalBurner: String,
    val mediaType: String,
    val ghostDescription: String,
    val originalTitle: String,
    val originalDetails: String,
    val currentHighestBid: Int,
    val highestBidder: String,
    val timeLeftLabel: String,
    val isResolved: Boolean = false,
    val isWonByMe: Boolean = false,
    val bidCount: Int = 0,
    val userBids: List<Int> = emptyList()
)

data class AuctionNotification(
    val id: String = UUID.randomUUID().toString(),
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
