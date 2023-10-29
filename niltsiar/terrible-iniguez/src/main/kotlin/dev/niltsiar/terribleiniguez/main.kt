package dev.niltsiar.terribleiniguez

import arrow.core.raise.either
import dev.niltsiar.terribleiniguez.domain.processEpisodes
import dev.niltsiar.terribleiniguez.network.makeNetworkRequest
import dev.niltsiar.terribleiniguez.network.parseEpisodes

suspend fun main() {
    either {
        val httpResponseBody = makeNetworkRequest().bind()
        val episodes = httpResponseBody.parseEpisodes().bind()
        val results = processEpisodes(episodes).bind()

        println("Next episode number: ${results.nextEpisodeNumber}")
        println("Total duration: ${results.totalDuration}")
        println("Number of shortest episode: ${results.numberOfShortestEpisode}")
        println("Selected titles combined duration under two hours: ${results.selectedTitlesCombinedDurationUnderTwoHours}")
    }
        .mapLeft { error ->
            println("We got an error from which we could not recover :(")
            println("Error: $error")
        }
}
