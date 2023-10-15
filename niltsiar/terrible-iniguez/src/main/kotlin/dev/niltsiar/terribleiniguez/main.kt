package dev.niltsiar.terribleiniguez

import dev.niltsiar.terribleiniguez.domain.processEpisodes
import dev.niltsiar.terribleiniguez.network.fetchEpisodes

suspend fun main() {
    val episodes = fetchEpisodes()
    processEpisodes(episodes)
}
