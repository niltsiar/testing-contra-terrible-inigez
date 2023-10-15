package dev.niltsiar.terribleiniguez.network

import dev.niltsiar.terribleiniguez.domain.Episode
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlin.time.Duration.Companion.seconds
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.jsonObject

const val API_URL = "https://tormenta-codigo-app-terrible.vercel.app/api/podcast"

private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

@Serializable
data class NetworkEpisode(
    val number: String,
    val duration: String,
    val title: String,
)

// Create a JsonTransformingSerializer that gets the http response in the form {"data": [...]}
// and returns the list of episodes
object NetworkEpisodeDeserializer : JsonTransformingSerializer<List<NetworkEpisode>>(ListSerializer(NetworkEpisode.serializer())) {

    override fun transformDeserialize(element: JsonElement): JsonElement {
        return element.jsonObject["data"] ?: JsonObject(emptyMap())
    }
}

suspend fun fetchEpisodes(url: String = API_URL): List<Episode>? {
    return try {
        val httpClient = HttpClient(CIO)
        val response = httpClient.get(url)
        val textResponse = response.bodyAsText()
        val jsonElement = json.decodeFromString<JsonElement>(textResponse)
        val jsonEpisodes = jsonElement.jsonObject["data"] as JsonArray?
        jsonEpisodes?.mapNotNull { jsonEpisode ->
            try {
                val networkEpisode = json.decodeFromJsonElement(NetworkEpisode.serializer(), jsonEpisode)
                Episode(from = networkEpisode)
            } catch (e: Exception) {
                println("Error decoding the episode: $e")
                println("Received json: $jsonEpisode")
                null
            }
        }
    } catch (e: Exception) {
        println("Error fetching the episodes: $e")
        null
    }
}

fun Episode(from: NetworkEpisode): Episode {
    return Episode(
        number = from.number.toInt(),
        duration = from.duration.toInt().seconds,
        title = from.title,
    )
}
