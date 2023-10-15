package dev.niltsiar.terribleiniguez

suspend fun main() {
    val episodes = fetchEpisodes()
    processEpisodes(episodes)
}
