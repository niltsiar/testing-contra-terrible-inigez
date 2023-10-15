package dev.niltsiar.terribleiniguez.domain

import dev.niltsiar.terribleiniguez.network.Episode

fun processEpisodes(episodes: List<Episode>?) {
    episodes?.let {
        if (it.isNotEmpty()) {
            it.forEach { ep -> ep.duration = ep.duration.toInt().toString() }
            it.sortedBy { ep -> ep.number.toInt() }

            val nextEpisodeNumber = it.last().number.toInt() + 1
            val totalDuration = it.sumOf { ep -> ep.duration.toInt() }
            val shortestEpisode = it.minByOrNull { ep -> ep.duration.toInt() }
            val selectedTitles = it.shuffled()
                .asSequence()
                .runningFold(0 to null) { acc: Pair<Int, Episode?>, ep ->
                    (acc.first + ep.duration.toInt()) to ep
                }
                .filter { it.first < 2 * 60 * 60 }
                .mapNotNull { it.second }
                .toList()

            println("Next episode number: $nextEpisodeNumber")
            println("Total duration of all episodes: $totalDuration")
            println("Number of the shortest episode: ${shortestEpisode?.number}")
            println("Titles below 2 hours: ${selectedTitles.map { ep -> ep.title }}")
            println("Duration of all selected titles: ${selectedTitles.sumOf { ep -> ep.duration.toInt() }}")
        }
    }
}
