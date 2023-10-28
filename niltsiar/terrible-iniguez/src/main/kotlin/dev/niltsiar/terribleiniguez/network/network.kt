package dev.niltsiar.terribleiniguez.network

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.flatMap
import arrow.core.flatten
import arrow.core.left
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import arrow.core.recover
import arrow.core.right
import dev.niltsiar.terribleiniguez.domain.Episode
import dev.niltsiar.terribleiniguez.domain.Errors
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlin.time.Duration.Companion.seconds
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

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

suspend fun fetchEpisodes(url: String = API_URL): Either<Errors, NonEmptyList<Episode>> {
    return Either.catch {
        val httpClient = HttpClient(CIO)
        val response = httpClient.get(url)
        val textResponse = response.bodyAsText()
        println(textResponse)
        val jsonElement = json.decodeFromString<JsonElement>(textResponse)
        jsonElement.jsonObject["data"] as JsonArray?
    }
        .mapLeft { throwable -> Errors.NetworkError(throwable) }
        .map { jsonEpisodes ->
            jsonEpisodes.parseEpisodes()
                .flatMap { episodes ->
                    if (episodes.isEmpty()) {
                        Errors.NoPodcastsInResponseError.left()
                    } else {
                        NonEmptyList(episodes.first(), episodes.drop(1)).right()
                    }
                }
        }
        .flatten()
}

fun JsonArray?.parseEpisodes(): Either<Errors, List<Episode>> = either {
    ensureNotNull(this@parseEpisodes) { Errors.NoPodcastsInResponseError }

    mapNotNull { jsonEpisode ->
        jsonEpisode.parseEpisode()
            .getOrNull()
    }
}

fun JsonElement.parseEpisode(): Either<Errors, Episode> {
    return Either.catch {
        val networkEpisode = json.decodeFromJsonElement(NetworkEpisode.serializer(), this)
        Episode(from = networkEpisode)
    }
        .recover { throwable ->
            if (throwable is SerializationException) {
                tryParseEpisode().bind()
            } else {
                raise(Errors.JsonParsingError.UnknownError(this@parseEpisode, throwable))
            }
        }
        .onLeft { throwable ->
            println("Error decoding the episode: $throwable")
            println("Received json: $this")
        }
}

fun JsonElement.tryParseEpisode(): Either<Errors, Episode> = either {
    val title = jsonObject["title"]?.jsonPrimitive?.content ?: raise(Errors.JsonParsingError.MissingTitle(this@tryParseEpisode))
    val number = jsonObject["number"]?.jsonPrimitive?.intOrNull
        ?: title.removePrefix("WRP ").substringBefore(".").toIntOrNull()
        ?: raise(Errors.JsonParsingError.MissingNumber(this@tryParseEpisode))

    TODO()
}

private fun Episode(from: NetworkEpisode): Episode {
    return Episode(
        number = from.number.toInt(),
        duration = from.duration.toLong().seconds,
        title = from.title,
    )
}
