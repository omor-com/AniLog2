package com.anilog.omor.dev.domain.ranking

import com.anilog.omor.dev.data.database.entity.AnimeEntity
import com.anilog.omor.dev.domain.model.AnimeStatus
import java.util.Locale

data class RafaelConfig(
    val isEnabled: Boolean = true,
    val useFavoriteSignal: Boolean = true,
    val useCompletionSignal: Boolean = true,
    val useReviewDepthSignal: Boolean = true,
    val useProgressSignal: Boolean = true,
    val explicitPreferences: Set<String> = emptySet() // Set of "animeIdA>animeIdB"
)

enum class TieBreakSignal {
    EXPLICIT_PREFERENCE,
    FAVORITE_STATUS,
    REVIEW_DEPTH,
    WATCH_COMPLETION,
    EPISODE_PROGRESS,
    STABLE_FALLBACK
}

data class RafaelRankedItem(
    val anime: AnimeEntity,
    val rankPosition: Int,
    val tieBreakSignal: TieBreakSignal = TieBreakSignal.STABLE_FALLBACK,
    val explanationEn: String = "",
    val explanationBn: String = ""
)

object RafaelRankingEngine {

    /**
     * Primary entry point: sorts anime list using Rafael's deterministic preference algorithm.
     * 1. Sort by personalRating descending (unrated placed at end).
     * 2. Group equal ratings.
     * 3. Apply tie-breaking score to items with identical ratings.
     * 4. Return deterministic ranked items with explainable reasons.
     */
    fun rankAnime(
        animeList: List<AnimeEntity>,
        config: RafaelConfig = RafaelConfig(),
        languageCode: String = "en"
    ): List<RafaelRankedItem> {
        if (animeList.isEmpty()) return emptyList()

        if (!config.isEnabled) {
            // When Rafael is disabled, use standard rating sort with createdAt tie-break
            val simpleSorted = animeList.sortedWith(
                compareByDescending<AnimeEntity> { it.personalRating ?: -1f }
                    .thenByDescending { it.createdAt }
            )
            return simpleSorted.mapIndexed { index, anime ->
                RafaelRankedItem(
                    anime = anime,
                    rankPosition = index + 1,
                    tieBreakSignal = TieBreakSignal.STABLE_FALLBACK,
                    explanationEn = "Standard rating sorting",
                    explanationBn = "সাধারণ রেটিংভিত্তিক ক্রম"
                )
            }
        }

        // 1. Group by exact rating (null treated as special bucket)
        val ratedGroups = animeList
            .groupBy { it.personalRating }
            .toList()
            // Sort rating groups descending (null at the very end)
            .sortedWith { a, b ->
                val rA = a.first
                val rB = b.first
                when {
                    rA == null && rB == null -> 0
                    rA == null -> 1
                    rB == null -> -1
                    else -> rB.compareTo(rA) // Higher rating first
                }
            }

        val result = mutableListOf<RafaelRankedItem>()
        var currentRank = 1

        for ((rating, group) in ratedGroups) {
            if (group.size == 1) {
                val single = group.first()
                result.add(
                    RafaelRankedItem(
                        anime = single,
                        rankPosition = currentRank++,
                        tieBreakSignal = TieBreakSignal.STABLE_FALLBACK,
                        explanationEn = if (rating != null) "Ranked by personal rating: $rating" else "Unrated anime",
                        explanationBn = if (rating != null) "ব্যক্তিগত রেটিং অনুযায়ী ক্রম: $rating" else "রেটিংবিহীন অ্যানিমে"
                    )
                )
            } else {
                // Tie exists! Resolve deterministically among this group
                val resolvedTie = resolveTies(group, config)
                for (item in resolvedTie) {
                    result.add(item.copy(rankPosition = currentRank++))
                }
            }
        }

        return result
    }

    private fun resolveTies(
        tiedGroup: List<AnimeEntity>,
        config: RafaelConfig
    ): List<RafaelRankedItem> {
        // Calculate tie-break scores and determine principal reason
        data class ScoredAnime(
            val anime: AnimeEntity,
            val score: Double,
            val principalSignal: TieBreakSignal,
            val explanationEn: String,
            val explanationBn: String
        )

        val scored = tiedGroup.map { anime ->
            var score = 0.0
            var bestSignal = TieBreakSignal.STABLE_FALLBACK
            var explEn = "No additional preference evidence is available; their existing order was preserved."
            var explBn = "অতিরিক্ত তথ্য না থাকায় পূর্বের স্বাভাবিক ক্রমটি বজায় রাখা হয়েছে।"

            // Signal 1: Explicit preference pairs (Weight: 1000.0)
            val explicitWins = config.explicitPreferences.count { it.startsWith("${anime.id}>") }
            val explicitLosses = config.explicitPreferences.count { it.endsWith(">${anime.id}") }
            if (explicitWins > 0 || explicitLosses > 0) {
                val netWins = (explicitWins - explicitLosses)
                score += netWins * 1000.0
                if (netWins > 0) {
                    bestSignal = TieBreakSignal.EXPLICIT_PREFERENCE
                    explEn = "You explicitly ranked this Anime higher."
                    explBn = "আপনি নিজে এটিকে এগিয়ে রাখতে পছন্দ করেছেন।"
                }
            }

            // Signal 2: Favorite status (Weight: 100.0)
            if (config.useFavoriteSignal && anime.isFavorite) {
                score += 100.0
                if (bestSignal == TieBreakSignal.STABLE_FALLBACK) {
                    bestSignal = TieBreakSignal.FAVORITE_STATUS
                    explEn = "Both Anime have the same rating. You marked this one as a favorite."
                    explBn = "উভয় অ্যানিমের রেটিং সমান। আপনি এটিকে পছন্দের তালিকায় রেখেছেন।"
                }
            }

            // Signal 3: Personal review depth and positive sentiment (Weight: 50.0)
            if (config.useReviewDepthSignal) {
                val reviewLen = anime.review.trim().length
                val hasDeepReview = reviewLen >= 40
                if (hasDeepReview) {
                    val sentimentBonus = calculateLocalSentimentBonus(anime.review)
                    score += 50.0 + sentimentBonus
                    if (bestSignal == TieBreakSignal.STABLE_FALLBACK) {
                        bestSignal = TieBreakSignal.REVIEW_DEPTH
                        explEn = "Both have the same rating. This entry features an in-depth personal review."
                        explBn = "উভয়ের রেটিং সমান। এই এন্ট্রিতে আপনি বিস্তারিত ব্যক্তিগত মতামত লিখেছেন।"
                    }
                }
            }

            // Signal 4: Completion status (Weight: 20.0)
            if (config.useCompletionSignal && anime.status == AnimeStatus.COMPLETED) {
                score += 20.0
                if (bestSignal == TieBreakSignal.STABLE_FALLBACK) {
                    bestSignal = TieBreakSignal.WATCH_COMPLETION
                    explEn = "Both have the same rating. You have fully completed watching this title."
                    explBn = "উভয়ের রেটিং সমান। আপনি এই অ্যানিমেটি সম্পূর্ণ দেখেছেন।"
                }
            }

            // Signal 5: Episode progress commitment (Weight: 10.0 * ratio)
            if (config.useProgressSignal && anime.totalEpisodes > 0) {
                val ratio = (anime.watchedEpisodes.toDouble() / anime.totalEpisodes.toDouble()).coerceIn(0.0, 1.0)
                score += ratio * 10.0
                if (bestSignal == TieBreakSignal.STABLE_FALLBACK && ratio > 0.5) {
                    bestSignal = TieBreakSignal.EPISODE_PROGRESS
                    explEn = "Both have the same rating. Higher episode commitment."
                    explBn = "উভয়ের রেটিং সমান। বেশি পর্ব সম্পন্ন করার রেকর্ড রয়েছে।"
                }
            }

            ScoredAnime(anime, score, bestSignal, explEn, explBn)
        }

        // Sort by score descending, then fallback to createdAt descending, then id
        val sortedScored = scored.sortedWith(
            compareByDescending<ScoredAnime> { it.score }
                .thenByDescending { it.anime.createdAt }
                .thenByDescending { it.anime.id }
        )

        return sortedScored.map {
            RafaelRankedItem(
                anime = it.anime,
                rankPosition = 0, // Will be assigned by caller
                tieBreakSignal = it.principalSignal,
                explanationEn = it.explanationEn,
                explanationBn = it.explanationBn
            )
        }
    }

    /**
     * Transparent, rule-based local sentiment heuristic without external network or LLM calls
     */
    private fun calculateLocalSentimentBonus(text: String): Double {
        val lower = text.lowercase(Locale.ROOT)
        val positiveKeywords = listOf(
            "masterpiece", "peak", "brilliant", "amazing", "love", "favorite",
            "great", "incredible", "beautiful", "exceptional", "outstanding",
            "সেরা", "অসাধারণ", "চমৎকার", "অনবদ্য", "ভালো", "প্রিয়"
        )
        val matchCount = positiveKeywords.count { lower.contains(it) }
        return (matchCount.coerceAtMost(5) * 2.0)
    }
}
