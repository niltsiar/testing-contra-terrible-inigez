package dev.niltsiar.terribleiniguez

import arrow.core.raise.either
import dev.niltsiar.terribleiniguez.domain.processEpisodes
import dev.niltsiar.terribleiniguez.network.makeNetworkRequest
import dev.niltsiar.terribleiniguez.network.parseEpisodes

const val API_URL = "https://tormenta-codigo-app-terrible.vercel.app/api/podcast"
const val TERRIBLE_API_URL = "$API_URL/terrible"

suspend fun main() {
    either {
        val httpResponseBody = makeNetworkRequest(TERRIBLE_API_URL).bind()
        val (parsingErrors, episodes) = httpResponseBody.parseEpisodes().bind()
        if (parsingErrors.isNotEmpty()) {
            println("⚠️⚠️ Terrible Iniguez is messing with us ⚠️⚠️")
            println("😒🤨 These are the errors we could not recover from 🤨😒")
            parsingErrors.forEach { error ->
                println("Error: $error")
            }
            println("🙌🙌 But we'll try to get some results from the episodes we could parse 💪💪")
        }
        val results = processEpisodes(episodes).bind()

        println("Next episode number: ${results.nextEpisodeNumber}")
        println("Total duration: ${results.totalDuration}")
        println("Number of shortest episode: ${results.numberOfShortestEpisode}")
        println("Selected titles combined duration under two hours: ${results.selectedTitlesCombinedDurationUnderTwoHours}")
    }
        .mapLeft { error ->
            println("😵 We got an error which we could not recover from 🤯")
            println("Error: $error")
        }
}
