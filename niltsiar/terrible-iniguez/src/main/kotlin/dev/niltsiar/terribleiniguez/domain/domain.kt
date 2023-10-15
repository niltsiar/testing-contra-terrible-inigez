package dev.niltsiar.terribleiniguez.domain

import kotlin.time.Duration

data class Episode(
    val number: Int,
    val duration: Duration,
    val title: String,
)

data class EpisodesResult(
    val nextEpisodeNumber: Int,
    val totalDuration: Long,
    val numberOfShortestEpisode: Int,
    val selectedTitlesCombinedDurationUnderTwoHours: List<String>,
)

fun processEpisodes(episodes: List<Episode>?): EpisodesResult? {
    val episodes = episodes?.sortedBy { ep -> ep.number } ?: return null

    if (episodes.isEmpty()) {
        return null
    }

    val nextEpisodeNumber = episodes.last().number.toInt() + 1
    val totalDuration = episodes.sumOf { ep -> ep.duration.inWholeSeconds }
    val shortestEpisode = episodes.minBy { ep -> ep.duration.inWholeSeconds }
    val selectedTitles = episodes.shuffled()
        .asSequence()
        .runningFold(0L to null as Episode?) { acc, ep ->
            (acc.first + ep.duration.inWholeSeconds) to ep
        }
        .filter { it.first < 2 * 60 * 60 }
        .mapNotNull { it.second }
        .toList()

    return EpisodesResult(
        nextEpisodeNumber = nextEpisodeNumber,
        totalDuration = totalDuration,
        numberOfShortestEpisode = shortestEpisode.number,
        selectedTitlesCombinedDurationUnderTwoHours = selectedTitles.map { ep -> ep.title },
    )
}
