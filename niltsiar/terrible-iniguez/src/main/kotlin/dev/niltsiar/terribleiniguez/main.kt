package dev.niltsiar.terribleiniguez

import arrow.core.raise.either
import dev.niltsiar.terribleiniguez.domain.processEpisodes
import dev.niltsiar.terribleiniguez.network.API_URL
import dev.niltsiar.terribleiniguez.network.makeNetworkRequest
import dev.niltsiar.terribleiniguez.network.parseEpisodes

suspend fun main() {
    either {
        val httpResponseBody = makeNetworkRequest("$API_URL/terrible").bind()
        val (parsingErrors, episodes) = httpResponseBody.parseEpisodes().bind()
        if (parsingErrors.isNotEmpty()) {
            println("Terrible Iniguez is messing with us")
            println("This are the errors we could not recover from")
            parsingErrors.forEach { error ->
                println("Error: $error")
            }
            println("But we'll try to get some results from the episodes we could parse")
        }
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
