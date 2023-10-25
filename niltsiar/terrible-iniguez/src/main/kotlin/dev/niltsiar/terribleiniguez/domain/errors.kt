package dev.niltsiar.terribleiniguez.domain

sealed interface Errors {
    class NetworkError(val throwable: Throwable) : Errors
    data object NoPodcastsInResponseError : Errors
}
