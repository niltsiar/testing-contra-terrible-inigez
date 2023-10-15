package dev.niltsiar.terribleiniguez.domain

import dev.niltsiar.terribleiniguez.network.fetchEpisodes
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldNotBe

class FetchEpisodesTest : FreeSpec({

    "Given a url" - {
        "When fetching the episodes" - {
            "Then it should return a list of episodes" {
                val episodes = fetchEpisodes()
                episodes shouldNotBe null
                episodes.shouldNotBeEmpty()
            }
        }
    }
})
