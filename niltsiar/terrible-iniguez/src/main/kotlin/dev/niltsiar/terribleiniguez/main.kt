package dev.niltsiar.terribleiniguez

import dev.niltsiar.terribleiniguez.domain.processEpisodes
import dev.niltsiar.terribleiniguez.network.fetchEpisodes

suspend fun main() {
    val episodes = fetchEpisodes()
    val results = processEpisodes(episodes) ?: return

    println("Next episode number: ${results.nextEpisodeNumber}")
    println("Total duration: ${results.totalDuration}")
    println("Number of shortest episode: ${results.numberOfShortestEpisode}")
    println("Selected titles combined duration under two hours: ${results.selectedTitlesCombinedDurationUnderTwoHours}")
}
