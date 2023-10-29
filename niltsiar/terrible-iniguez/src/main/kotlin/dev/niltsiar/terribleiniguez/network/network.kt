package dev.niltsiar.terribleiniguez.network

import arrow.core.Either
import arrow.core.flatten
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import arrow.core.recover
import dev.niltsiar.terribleiniguez.API_URL
import dev.niltsiar.terribleiniguez.domain.Episode
import dev.niltsiar.terribleiniguez.domain.Errors
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlin.time.Duration
import kotlin.time.Duration.Companion.microseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

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

suspend fun makeNetworkRequest(url: String = API_URL): Either<Errors, String> {
    return Either.catch {
        val httpClient = HttpClient(CIO)
        val response = httpClient.get(url)
        response.bodyAsText()
    }
        .mapLeft { throwable -> Errors.NetworkError(throwable) }
}

fun String.parseEpisodes(): Either<Errors, Pair<List<Errors.JsonParsingError>, List<Episode>>> {
    return Either.catch {
        val jsonElement = json.decodeFromString<JsonElement>(this)
        jsonElement.jsonObject["data"] as JsonArray?
    }
        .mapLeft { throwable -> Errors.NetworkError(throwable) }
        .map { jsonEpisodes ->
            jsonEpisodes.parseEpisodes()
        }
        .flatten()
}

fun JsonArray?.parseEpisodes(): Either<Errors, Pair<List<Errors.JsonParsingError>, List<Episode>>> = either {
    ensureNotNull(this@parseEpisodes) { Errors.NoPodcastsInResponseError }

    fold((emptyList<Errors.JsonParsingError>() to emptyList<Episode>())) { acc, jsonElement ->
        jsonElement.parseEpisode()
            .fold(
                ifLeft = { error -> acc.first + error to acc.second },
                ifRight = { episode -> acc.first to acc.second + episode },
            )
    }
}

fun JsonElement.parseEpisode(): Either<Errors.JsonParsingError, Episode> {
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
}

fun JsonElement.tryParseEpisode(): Either<Errors.JsonParsingError, Episode> = either {
    val title = jsonObject["title"]?.jsonPrimitive?.content
        ?: jsonObject["excerpt"]?.jsonPrimitive?.content
        ?: raise(Errors.JsonParsingError.MissingTitle(this@tryParseEpisode))
    val number = jsonObject["number"]?.jsonPrimitive?.intOrNull
        ?: title.removePrefix("WRP ").substringBefore(".").toIntOrNull()
        ?: raise(Errors.JsonParsingError.MissingNumber(this@tryParseEpisode))

    val duration = jsonObject["duration"]?.jsonPrimitive?.longOrNull
        ?: jsonObject["supercoco"]?.jsonPrimitive?.longOrNull
        ?: raise(Errors.JsonParsingError.MissingDuration(this@tryParseEpisode))

    Episode(
        number = number,
        duration = duration.convertToSeconds(),
        title = title,
    )
}

private fun Episode(from: NetworkEpisode): Episode {
    return Episode(
        number = from.number.toInt(),
        duration = from.duration.toLong().convertToSeconds(),
        title = from.title,
    )
}

private fun Long.convertToSeconds(): Duration {
    return if (this > 1_000_000) {
        microseconds.inWholeSeconds.seconds
    } else {
        seconds
    }
}
