package dev.niltsiar.terribleiniguez.domain

import dev.niltsiar.terribleiniguez.network.API_URL
import dev.niltsiar.terribleiniguez.network.fetchEpisodes
import dev.niltsiar.terribleiniguez.network.parseEpisode
import dev.niltsiar.terribleiniguez.network.parseEpisodes
import io.kotest.assertions.arrow.core.shouldBeLeft
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
      "duration": "5209",
      "id": "BNDv7ReKDZP2Gnrd9k\/9541YGmJ9z7K3gWLpe"
    },
    {
      "number": "248",
      "title": "WRP 248. Tips de seguridad OWASP para developers con \u00c9rica Aguado",
      "excerpt": "#SeguridadInformatica #Programaci\u00f3nSegura #ResponsabilidadLegal #ControlDeAcceso #Monitorizaci\u00f3nYRegistro",
      "published_at": 1685491555,
      "duration": 5209,
      "id": "BNDv7ReKDZP2Gnrd9k\/9541YGmJ9z7K3gWLpe"
    }
  ]
}"""

                val jsonElement = json.decodeFromString<JsonElement>(goodEpisode)
                val data = jsonElement.jsonObject["data"] as JsonArray
                val result = data.parseEpisodes()

                val expectedEpisode = Episode(
                    number = 248,
                    duration = 5209.seconds,
                    title = "WRP 248. Tips de seguridad OWASP para developers con Érica Aguado",
                )

                result.shouldBeRight(listOf(expectedEpisode, expectedEpisode))
            }
        }
    }

    "Given a bad encoded episode" - {
        "Given an episode with missing title" - {
            "When it is parsed" - {
                "Then we received a MissingTitle error" {
                    val badEpisode = """{
  "excerpt": "Preguntamos a un experto developer en node.js sobre las ventajas e inconvenientes de este lenguaje.",
  "published_at": 1694563528,
  "duration": "4138",
  "id": "BNDv7ReKDZP2Gnrd9k\/Q8mBdwoPqmpPjxzZv6"
}"""
                    val jsonElement = json.decodeFromString<JsonElement>(badEpisode)
                    val result = jsonElement.parseEpisode()

                    result.shouldBeLeft(Errors.JsonParsingError.MissingTitle(jsonElement))
                }
            }
        }
        "Given an episode with missing number" - {
            "When it can be extracted from the title" - {
                "Then we received the recovered episode" {
                    val badEpisode = """{
  "title": "WRP 244. D\u00f3nde poner el foco para actualizarse",
  "excerpt": "Tu mejor opci\u00f3n para seguir actualizado.",
  "published_at": 1683099502,
  "duration": "2574",
  "id": "BNDv7ReKDZP2Gnrd9k\/qZvpVO8PXdRPanNGkl"
}"""
                    val jsonElement = json.decodeFromString<JsonElement>(badEpisode)
                    val result = jsonElement.parseEpisode()

                    val expectedEpisode = Episode(
                        number = 244,
                        duration = 2574.seconds,
                        title = "WRP 244. Dónde poner el foco para actualizarse",
                    )
                    result.shouldBeRight(expectedEpisode)
                }
            }
            "When it can't be extracted from the title" - {
                "Then we received a MissingNumber error" {
                    val badEpisode = """{
  "title": "WRP WRONG_NUMBER. Desplegar en producci\u00f3n: Estrategias Feature Flags y Expand-Contract",
  "excerpt": "Tipos de despliegue y definici\u00f3n de los m\u00e1s efectivos.",
  "published_at": 1677629119,
  "duration": "3158",
  "id": "3381afe484e4b10af30f1ff0258a7705"
}"""
                    val jsonElement = json.decodeFromString<JsonElement>(badEpisode)
                    val result = jsonElement.parseEpisode()

                    result.shouldBeLeft(Errors.JsonParsingError.MissingNumber(jsonElement))
                }
            }
        }
        "Given an episode with missing duration" - {
            "When it can be recovered from supercoco" - {
                "Then we received the recovered episode" {
                    val badEpisode = """{
  "number": "222",
  "title": "WRP 222. Todo lo que necesitas saber para ser Blockchain Developer con Fernando L\u00f3pez",
  "excerpt": "Las principales ventajas e inconvenientes para tener un nuevo futuro profesional entre cryptos y NFTs.",
  "published_at": 1669765908,
  "id": "0fff26d111b557d0a49e4e02ec1d39f4",
  "supercoco": "6328"
}"""
                    val jsonElement = json.decodeFromString<JsonElement>(badEpisode)
                    val result = jsonElement.parseEpisode()

                    val expectedEpisode = Episode(
                        number = 222,
                        duration = 6328.seconds,
                        title = "WRP 222. Todo lo que necesitas saber para ser Blockchain Developer con Fernando López",
                    )
                    result.shouldBeRight(expectedEpisode)
                }
            }
        }
        "When it cannot be recovered from supercoco" - {
            "Then we received the recovered episode" {
                val badEpisode = """{
  "number": "222",
  "title": "WRP 222. Todo lo que necesitas saber para ser Blockchain Developer con Fernando L\u00f3pez",
  "excerpt": "Las principales ventajas e inconvenientes para tener un nuevo futuro profesional entre cryptos y NFTs.",
  "published_at": 1669765908,
  "id": "0fff26d111b557d0a49e4e02ec1d39f4"
}"""
                val jsonElement = json.decodeFromString<JsonElement>(badEpisode)
                val result = jsonElement.parseEpisode()

                result.shouldBeLeft(Errors.JsonParsingError.MissingDuration(jsonElement))
            }
        }
    }
})
