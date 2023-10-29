package dev.niltsiar.terribleiniguez.domain

import arrow.core.flatMap
import dev.niltsiar.terribleiniguez.TERRIBLE_API_URL
import dev.niltsiar.terribleiniguez.network.makeNetworkRequest
import dev.niltsiar.terribleiniguez.network.parseEpisode
import dev.niltsiar.terribleiniguez.network.parseEpisodes
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldNotBe
import kotlin.time.Duration.Companion.seconds
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

class NetworkTests : FreeSpec({

    "Given a url" - {
        "When fetching the episodes" - {
            "Then it should return a list of episodes" {
                val networkResponse = makeNetworkRequest(TERRIBLE_API_URL)
                networkResponse.shouldBeRight()
                @Suppress("INFERRED_TYPE_VARIABLE_INTO_POSSIBLE_EMPTY_INTERSECTION")
                val parsingResult = networkResponse.flatMap { jsonResponse ->
                    jsonResponse.parseEpisodes()
                }
                parsingResult.onRight { (_, episodes) ->
                    episodes shouldNotBe emptyList<Episode>()
                }
            }
        }
    }

    "Given a list of good episodes" - {
        "When they are parsed" - {
            "Then we got the expected results" {
                val goodEpisode = """
{
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
}
""".trimIndent()

                val jsonElement = json.decodeFromString<JsonElement>(goodEpisode)
                val data = jsonElement.jsonObject["data"] as JsonArray
                val result = data.parseEpisodes()

                val expectedEpisode = Episode(
                    number = 248,
                    duration = 5209.seconds,
                    title = "WRP 248. Tips de seguridad OWASP para developers con Érica Aguado",
                )

                result.shouldBeRight(emptyList<Errors.JsonParsingError>() to listOf(expectedEpisode, expectedEpisode))
            }
        }
    }

    "Given a bad encoded episode" - {
        "Given an episode with missing title" - {
            "When it is parsed" - {
                "Then we receive a MissingTitle error" {
                    val badEpisode = """
{
  "number": "263",
  "published_at": 1694563528,
  "duration": "4138",
  "id": "BNDv7ReKDZP2Gnrd9k\/Q8mBdwoPqmpPjxzZv6"
}
""".trimIndent()
                    val jsonElement = json.decodeFromString<JsonElement>(badEpisode)
                    val result = jsonElement.parseEpisode()

                    result.shouldBeLeft(Errors.JsonParsingError.MissingTitle(jsonElement))
                }
            }
            "When it can be recovered using excerpt" - {
                "Then we receive the recovered episode" {
                    val badEpisode = """
{
  "number": 226,
  "excerpt": "5 proyectos que te van a encantar.",
  "published_at": 1672821686,
  "duration": "3660",
  "id": "6ccf82594cce11357ffaa3ae0469535a"
}
""".trimIndent()
                    val jsonElement = json.decodeFromString<JsonElement>(badEpisode)
                    val result = jsonElement.parseEpisode()

                    val expectedEpisode = Episode(
                        number = 226,
                        duration = 3660.seconds,
                        title = "5 proyectos que te van a encantar.",
                    )
                    result.shouldBeRight(expectedEpisode)
                }
            }
        }
        "Given an episode with missing number" - {
            "When it can be extracted from the title" - {
                "Then we receive the recovered episode" {
                    val badEpisode = """
{
  "title": "WRP 244. D\u00f3nde poner el foco para actualizarse",
  "excerpt": "Tu mejor opci\u00f3n para seguir actualizado.",
  "published_at": 1683099502,
  "duration": "2574",
  "id": "BNDv7ReKDZP2Gnrd9k\/qZvpVO8PXdRPanNGkl"
}
""".trimIndent()
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
                val unparsableNumberFromTitleEpisode = """
  {
    "title": "WRP WRONG_NUMBER. Desplegar en producci\u00f3n: Estrategias Feature Flags y Expand-Contract",
    "excerpt": "Tipos de despliegue y definici\u00f3n de los m\u00e1s efectivos.",
    "published_at": 1677629119,
    "duration": "3158",
    "id": "3381afe484e4b10af30f1ff0258a7705"
  }
""".trimIndent()
                val missingTitleAndNumberEpisode = """
{
  "excerpt": "Las principales ventajas e inconvenientes para tener un nuevo futuro profesional entre cryptos y NFTs.",
  "published_at": 1669765908,
  "duration": 6328,
  "id": "BNDv7ReKDZP2Gnrd9k/W6ADGpkPRg6Jrl3nXR"
}
""".trimIndent()
                listOf(unparsableNumberFromTitleEpisode, missingTitleAndNumberEpisode).forEach { badEpisode ->
                    "Then we receive a MissingNumber error" {

                        val jsonElement = json.decodeFromString<JsonElement>(badEpisode)
                        val result = jsonElement.parseEpisode()

                        result.shouldBeLeft(Errors.JsonParsingError.MissingNumber(jsonElement))
                    }
                }
            }
        }
        "Given an episode with duration in microseconds" - {
            "When it's parsed" - {
                "Then we receive proper episode" {
                    val goodEpisodeWidDurationInMicroseconds = """
{
  "number": "246",
  "title": "WRP 246. Programar con lo que no est\u00e1 de moda: PHP",
  "excerpt": "Desmontando mitos de PHP: lenguaje pasado de moda o todav\u00eda relevante.",
  "published_at": 1684280419,
  "id": "a9ee1f451fb2bd81cd773abb7005ebce",
  "duration": 2994101796
}
""".trimIndent()
                    val jsonElement = json.decodeFromString<JsonElement>(goodEpisodeWidDurationInMicroseconds)
                    val result = jsonElement.parseEpisode()

                    val expectedEpisode = Episode(
                        number = 246,
                        duration = 2994.seconds,
                        title = "WRP 246. Programar con lo que no está de moda: PHP",
                    )
                    result.shouldBeRight(expectedEpisode)
                }
            }
        }
        "Given an episode with missing duration" - {
            "When it can be recovered from supercoco" - {
                "Then we receive the recovered episode" {
                    val badEpisode = """
{
  "number": "222",
  "title": "WRP 222. Todo lo que necesitas saber para ser Blockchain Developer con Fernando L\u00f3pez",
  "excerpt": "Las principales ventajas e inconvenientes para tener un nuevo futuro profesional entre cryptos y NFTs.",
  "published_at": 1669765908,
  "id": "0fff26d111b557d0a49e4e02ec1d39f4",
  "supercoco": "6328"
}
""".trimIndent()
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
            "When it cannot be recovered from supercoco in microseconds" - {
                "Then we receive the recovered episode" {
                    val badEpisode = """
{
  "number": "246",
  "title": "WRP 246. Programar con lo que no est\u00e1 de moda: PHP",
  "excerpt": "Desmontando mitos de PHP: lenguaje pasado de moda o todav\u00eda relevante.",
  "published_at": 1684280419,
  "id": "a9ee1f451fb2bd81cd773abb7005ebce",
  "supercoco": 2994101796
}
""".trimIndent()
                    val jsonElement = json.decodeFromString<JsonElement>(badEpisode)
                    val result = jsonElement.parseEpisode()

                    val expectedEpisode = Episode(
                        number = 246,
                        duration = 2994.seconds,
                        title = "WRP 246. Programar con lo que no está de moda: PHP",
                    )
                    result.shouldBeRight(expectedEpisode)
                }
            }
            "When it cannot be recovered from supercoco" - {
                "Then we receive a MissingDuration error" {
                    val badEpisode = """
{
  "number": "222",
  "title": "WRP 222. Todo lo que necesitas saber para ser Blockchain Developer con Fernando L\u00f3pez",
  "excerpt": "Las principales ventajas e inconvenientes para tener un nuevo futuro profesional entre cryptos y NFTs.",
  "published_at": 1669765908,
  "id": "0fff26d111b557d0a49e4e02ec1d39f4"
}
""".trimIndent()
                    val jsonElement = json.decodeFromString<JsonElement>(badEpisode)
                    val result = jsonElement.parseEpisode()

                    result.shouldBeLeft(Errors.JsonParsingError.MissingDuration(jsonElement))
                }
            }
        }
        "Given an episode with missing number and duration" - {
            "When it can be fully recovered" - {
                "Then we receive the recovered episode" {
                    val badEpisode = """
{
  "title": "WRP 219. Mastermind Ornitorrincos: Mucha IA y bots de telegram",
  "excerpt": "Robert, Dani y Gabri se juntan por sexta vez.",
  "published_at": 1667950903,
  "id": "BNDv7ReKDZP2Gnrd9k\/Q8mBdwoPqGpKjxzZv6",
  "supercoco": 2621089114
}
""".trimIndent()

                    val jsonElement = json.decodeFromString<JsonElement>(badEpisode)
                    val result = jsonElement.parseEpisode()

                    val expectedEpisode = Episode(
                        number = 219,
                        duration = 2621.seconds,
                        title = "WRP 219. Mastermind Ornitorrincos: Mucha IA y bots de telegram",
                    )
                    result.shouldBeRight(expectedEpisode)
                }
            }
        }
    }
})
