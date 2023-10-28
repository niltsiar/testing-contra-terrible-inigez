package dev.niltsiar.terribleiniguez.domain

import dev.niltsiar.terribleiniguez.network.API_URL
import dev.niltsiar.terribleiniguez.network.fetchEpisodes
import dev.niltsiar.terribleiniguez.network.parseEpisodes
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FreeSpec
import kotlin.time.Duration.Companion.seconds
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

const val TERRIBLE_API_URL = "$API_URL/terrible"

private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

class FetchEpisodesTest : FreeSpec({

    "Given a url" - {
        "When fetching the episodes" - {
            "Then it should return a list of episodes" {
                val episodes = fetchEpisodes(TERRIBLE_API_URL)
                episodes.shouldBeRight()
            }
        }
    }

    "Given a list of good episodes" - {
        "When they are parsed" - {
            "Then we got the expected results" {
                val goodEpisode = """{
  "data": [
    {
      "number": "248",
      "title": "WRP 248. Tips de seguridad OWASP para developers con \u00c9rica Aguado",
      "excerpt": "#SeguridadInformatica #Programaci\u00f3nSegura #ResponsabilidadLegal #ControlDeAcceso #Monitorizaci\u00f3nYRegistro",
      "published_at": 1685491555,
      "duration": 5209177106,
      "id": "BNDv7ReKDZP2Gnrd9k\/9541YGmJ9z7K3gWLpe"
    },
    {
      "number": "248",
      "title": "WRP 248. Tips de seguridad OWASP para developers con \u00c9rica Aguado",
      "excerpt": "#SeguridadInformatica #Programaci\u00f3nSegura #ResponsabilidadLegal #ControlDeAcceso #Monitorizaci\u00f3nYRegistro",
      "published_at": 1685491555,
      "duration": "5209177106",
      "id": "BNDv7ReKDZP2Gnrd9k\/9541YGmJ9z7K3gWLpe"
    }
  ]
}"""

                val jsonElement = json.decodeFromString<JsonElement>(goodEpisode)
                val data = jsonElement.jsonObject["data"] as JsonArray
                val result = data.parseEpisodes()

                val expectedEpisode = Episode(
                    number = 248,
                    duration = 5209177106.seconds,
                    title = "WRP 248. Tips de seguridad OWASP para developers con Érica Aguado",
                )

                result.shouldBeRight(listOf(expectedEpisode, expectedEpisode))
            }
        }
    }
})
