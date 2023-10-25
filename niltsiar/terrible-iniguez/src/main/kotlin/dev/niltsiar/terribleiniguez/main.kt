package dev.niltsiar.terribleiniguez

import arrow.core.raise.either
import dev.niltsiar.terribleiniguez.domain.processEpisodes
import dev.niltsiar.terribleiniguez.network.fetchEpisodes

suspend fun main() {
    either {
        val episodes = fetchEpisodes().bind()
        val results = processEpisodes(episodes).bind()

        println("Next episode number: ${results.nextEpisodeNumber}")
        println("Total duration: ${results.totalDuration}")
        println("Number of shortest episode: ${results.numberOfShortestEpisode}")
        println("Selected titles combined duration under two hours: ${results.selectedTitlesCombinedDurationUnderTwoHours}")
    }
}
