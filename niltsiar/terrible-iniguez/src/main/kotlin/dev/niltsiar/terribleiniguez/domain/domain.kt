package dev.niltsiar.terribleiniguez.domain

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
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

fun processEpisodes(episodes: List<Episode>): Either<Errors, EpisodesResult> = either {
    ensure(episodes.isNotEmpty()) { Errors.NoPodcastsInResponseError }

    val sortedEpisodes = episodes.sortedBy { ep -> ep.number }

    val nextEpisodeNumber = sortedEpisodes.last().number.toInt() + 1
    val totalDuration = sortedEpisodes.sumOf { ep -> ep.duration.inWholeSeconds }
    val shortestEpisode = sortedEpisodes.minBy { ep -> ep.duration.inWholeSeconds }
    val selectedTitles = sortedEpisodes.shuffled()
        .asSequence()
        .runningFold(0L to null as Episode?) { acc, ep ->
            (acc.first + ep.duration.inWholeSeconds) to ep
        }
        .takeWhile { it.first < 2 * 60 * 60 }
        .mapNotNull { it.second }
        .toList()

    EpisodesResult(
        nextEpisodeNumber = nextEpisodeNumber,
        totalDuration = totalDuration,
        numberOfShortestEpisode = shortestEpisode.number,
        selectedTitlesCombinedDurationUnderTwoHours = selectedTitles.map { ep -> ep.title },
    )
}
