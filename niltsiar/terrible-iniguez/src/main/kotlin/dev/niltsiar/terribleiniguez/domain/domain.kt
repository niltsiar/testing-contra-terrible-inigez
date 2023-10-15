package dev.niltsiar.terribleiniguez.domain

import kotlin.time.Duration

data class Episode(
    val number: Int,
    val duration: Duration,
    val title: String,
)

fun processEpisodes(episodes: List<Episode>?) {
    val episodes = episodes ?: return

    episodes.sortedBy { ep -> ep.number }.let {
        if (it.isNotEmpty()) {
            val nextEpisodeNumber = it.last().number.toInt() + 1
            val totalDuration = it.sumOf { ep -> ep.duration.inWholeSeconds }
            val shortestEpisode = it.minByOrNull { ep -> ep.duration.inWholeSeconds }
            val selectedTitles = it.shuffled()
                .asSequence()
                .runningFold(0L to null as Episode?) { acc, ep ->
                    (acc.first + ep.duration.inWholeSeconds) to ep
                }
                .filter { it.first < 2 * 60 * 60 }
                .mapNotNull { it.second }
                .toList()

            println("Next episode number: $nextEpisodeNumber")
            println("Total duration of all episodes: $totalDuration")
            println("Number of the shortest episode: ${shortestEpisode?.number}")
            println("Titles below 2 hours: ${selectedTitles.map { ep -> ep.title }}")
            println("Duration of all selected titles: ${selectedTitles.sumOf { ep -> ep.duration.inWholeSeconds }}")
        }
    }
}
