package dev.niltsiar.terribleiniguez.domain

import dev.niltsiar.terribleiniguez.network.API_URL
import dev.niltsiar.terribleiniguez.network.fetchEpisodes
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldNotBe

const val TERRIBLE_API_URL = "$API_URL/terrible"

class FetchEpisodesTest : FreeSpec({

    "Given a url" - {
        "When fetching the episodes" - {
            "Then it should return a list of episodes" {
                val episodes = fetchEpisodes(TERRIBLE_API_URL)
                episodes shouldNotBe null
                episodes.shouldNotBeEmpty()
            }
        }
    }
})
