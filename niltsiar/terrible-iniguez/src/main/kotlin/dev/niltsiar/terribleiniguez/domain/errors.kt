package dev.niltsiar.terribleiniguez.domain

import kotlinx.serialization.json.JsonElement

sealed interface Errors {
    data class NetworkError(val throwable: Throwable) : Errors
    data object NoPodcastsInResponseError : Errors

    sealed interface JsonParsingError : Errors {

        data class UnknownError(
            val json: JsonElement,
            val throwable: Throwable,
        ) : JsonParsingError

        data class MissingTitle(val json: JsonElement) : JsonParsingError
        data class MissingNumber(val json: JsonElement) : JsonParsingError

    }
}
