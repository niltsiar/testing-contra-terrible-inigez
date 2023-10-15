package dev.niltsiar.terribleiniguez.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
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
        return element.jsonObject["data"] ?: JsonObject(emptyMap())
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
