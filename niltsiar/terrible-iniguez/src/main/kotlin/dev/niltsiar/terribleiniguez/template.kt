package dev.niltsiar.terribleiniguez

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.jsonObject

const val API_URL = "https://tormenta-codigo-app-terrible.vercel.app/api/podcast"

@Serializable
data class Episode(
    val number: String,
    var duration: String,
    val title: String,
)

// Create a JsonTransformingSerializer that gets the http response in the form {"data": [...]}
// and returns the list of episodes
object EpisodeDeserializer : JsonTransformingSerializer<List<Episode>>(ListSerializer(Episode.serializer())) {

    override fun transformDeserialize(element: JsonElement): JsonElement {
        return element.jsonObject["data"] ?: error("No data found")
    }
}

suspend fun fetchEpisodes(): List<Episode>? {
    return try {
        val httpClient = HttpClient(CIO)
        val response = httpClient.get(API_URL)
        println(response.bodyAsText())
        Json {
            ignoreUnknownKeys = true
        }.decodeFromString<List<Episode>>(EpisodeDeserializer, response.bodyAsText())
    } catch (e: Exception) {
        println("Error fetching the episodes: $e")
        null
    }
}

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

suspend fun main() {
    val episodes = fetchEpisodes()
    processEpisodes(episodes)
}
