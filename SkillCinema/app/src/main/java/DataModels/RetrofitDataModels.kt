package DataModels

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Items (

    @Json(name = "total")
    val total: Long,

    @Json(name = "items")
    val items: List<Item>
)

@JsonClass(generateAdapter = true)
data class Item (

    var watched: Boolean? = null,

    var selectionAttachment: String? = null,

    @Json(name = "kinopoiskId")
    val kinopoiskID: Long? = null,

    @Json(name = "filmId")
    val filmID: Long? = null,

    @Json(name = "imdbId")
    val imdbID: String? = null,

    @Json(name = "nameRu")
    val nameRu: String? = null,

    @Json(name = "nameEn")
    val nameEn: String? = null,

    @Json(name = "nameOriginal")
    val nameOriginal: String? = null,

    @Json(name = "ratingKinopoisk")
    var ratingKinopoisk: Double? = null,

    @Json(name = "ratingImdb")
    val ratingImdb: Double? = null,

    @Json(name = "year")
    val year: Long? = null,

    @Json(name = "type")
    val type: String? = null,

    @Json(name = "posterUrl")
    val posterURL: String? = null,

    @Json(name = "posterUrlPreview")
    val posterURLPreview: String? = null,

    @Json(name = "imageUrl")
    val imageURL: String? = null,

    @Json(name = "previewUrl")
    val previewURL: String? = null,

    @Json(name = "countries")
    val countries: List<Country>? = null,

    @Json(name = "genres")
    val genres: List<Genre> ? = null,

    @Json(name = "duration")
    val duration: Long? = null,

    @Json(name = "premiereRu")
    val premiereRu: String? = null,

    @Json(name = "relationType")
    val relationType: String? = null
)

@JsonClass(generateAdapter = true)
data class Country (

    @Json(name = "id")
    val id: Long? = null,

    @Json(name = "country")
    val country: String
)

@JsonClass(generateAdapter = true)
data class Genre (

    @Json(name = "id")
    val id: Long? = null,

    @Json(name = "genre")
    val genre: String
)

/////////////////////////////////////////////////////////////////////////////////////////////////////////
@JsonClass(generateAdapter = true)
data class Films (

    @Json(name = "pagesCount")
    val pagesCount: Long,

    @Json(name = "films")
    val films: List<Film>
)

@JsonClass(generateAdapter = true)
data class Film (

    var watched: Boolean? = null,

    var selectionAttachment: String? = null,

    @Json(name = "filmId")
    val filmID: Long,

    @Json(name = "nameRu")
    val nameRu: String? = null,

    @Json(name = "nameEn")
    val nameEn: String? = null,

    @Json(name = "year")
    val year: String? = null,

    @Json(name = "filmLength")
    val filmLength: String? = null,

    @Json(name = "countries")
    val countries: List<Country>? = null,

    @Json(name = "genres")
    val genres: List<Genre>? = null,

    @Json(name = "rating")
    val rating: String? = null,

    @Json(name = "ratingVoteCount")
    val ratingVoteCount: Long? = null,

    @Json(name = "posterUrl")
    val posterURL: String? = null,

    @Json(name = "posterUrlPreview")
    val posterURLPreview: String? = null,

    @Json(name = "ratingChange")
    val ratingChange: Any? = null,

    @Json(name = "general")
    val general: Boolean? = null,

    @Json(name = "description")
    val description: String? = null,

    @Json(name = "professionKey")
    val professionKey: String? = null
)

/////////////////////////////////////////////////////////////////////////////////////////////////////////
@JsonClass(generateAdapter = true)
data class CountryGenreID (

    @Json(name = "countries")
    val countries: List<Country>,

    @Json(name = "genres")
    val genres: List<Genre>
)

/////////////////////////////////////////////////////////////////////////////////////////////////////////
@JsonClass(generateAdapter = true)
data class Series (
    @Json(name = "total")
    val total: Long? = null,

    @Json(name = "items")
    val items: List<Season>
)

@JsonClass(generateAdapter = true)
data class Season (
    @Json(name = "number")
    val number: Long? = null,

    @Json(name = "episodes")
    val episodes: List<Episode>
)

@JsonClass(generateAdapter = true)
data class Episode (
    @Json(name = "seasonNumber")
    val seasonNumber: Long? = null,

    @Json(name = "episodeNumber")
    val episodeNumber: Long? = null,

    @Json(name = "nameRu")
    val nameRu: String? = null,

    @Json(name = "nameEn")
    val nameEn: String? = null,

    @Json(name = "synopsis")
    val synopsis: String? = null,

    @Json(name = "releaseDate")
    val releaseDate: String? = null
)

/////////////////////////////////////////////////////////////////////////////////////////////////////////
@JsonClass(generateAdapter = true)
data class DetailedInfo (

    @Json(name = "kinopoiskId")
    val kinopoiskID: Long? = null,

    @Json(name = "imdbId")
    val imdbID: String? = null,

    @Json(name = "nameRu")
    val nameRu: String? = null,

    @Json(name = "nameEn")
    val nameEn: String? = null,

    @Json(name = "nameOriginal")
    val nameOriginal: String? = null,

    @Json(name = "posterUrl")
    val posterURL: String? = null,

    @Json(name = "posterUrlPreview")
    val posterURLPreview: String? = null,

    @Json(name = "coverUrl")
    val coverURL: String? = null,

    @Json(name = "logoUrl")
    val logoURL: String? = null,

    @Json(name = "reviewsCount")
    val reviewsCount: Long? = null,

    @Json(name = "ratingGoodReview")
    val ratingGoodReview: Double? = null,

    @Json(name = "ratingGoodReviewVoteCount")
    val ratingGoodReviewVoteCount: Long? = null,

    @Json(name = "ratingKinopoisk")
    val ratingKinopoisk: Double? = null,

    @Json(name = "ratingKinopoiskVoteCount")
    val ratingKinopoiskVoteCount: Long? = null,

    @Json(name = "ratingImdb")
    val ratingImdb: Double? = null,

    @Json(name = "ratingImdbVoteCount")
    val ratingImdbVoteCount: Long? = null,

    @Json(name = "ratingFilmCritics")
    val ratingFilmCritics: Double? = null,

    @Json(name = "ratingFilmCriticsVoteCount")
    val ratingFilmCriticsVoteCount: Long? = null,

    @Json(name = "ratingAwait")
    val ratingAwait: Double? = null,

    @Json(name = "ratingAwaitCount")
    val ratingAwaitCount: Long? = null,

    @Json(name = "ratingRfcritics")
    val ratingRFCritics: Double? = null,

    @Json(name = "ratingRfcriticsVoteCount")
    val ratingRFCriticsVoteCount: Long? = null,

    @Json(name = "webUrl")
    val webURL: String? = null,

    @Json(name = "year")
    val year: Long? = null,

    @Json(name = "filmLength")
    val filmLength: Long? = null,

    @Json(name = "slogan")
    val slogan: String? = null,

    @Json(name = "description")
    val description: String? = null,

    @Json(name = "shortDescription")
    val shortDescription: String? = null,

    @Json(name = "editorAnnotation")
    val editorAnnotation: String? = null,

    @Json(name = "isTicketsAvailable")
    val isTicketsAvailable: Boolean? = null,

    @Json(name = "productionStatus")
    val productionStatus: String? = null,

    @Json(name = "type")
    val type: String? = null,

    @Json(name = "ratingMpaa")
    val ratingMPAA: String? = null,

    @Json(name = "ratingAgeLimits")
    val ratingAgeLimits: String? = null,

    @Json(name = "hasImax")
    val hasImax: Boolean? = null,

    @Json(name = "has3D")
    val has3D: Boolean? = null,

    @Json(name = "lastSync")
    val lastSync: String? = null,

    @Json(name = "countries")
    val countries: List<Country>? = null,

    @Json(name = "genres")
    val genres: List<Genre>? = null,

    @Json(name = "startYear")
    val startYear: Long? = null,

    @Json(name = "endYear")
    val endYear: Long? = null,

    @Json(name = "serial")
    val serial: Boolean? = null,

    @Json(name = "shortFilm")
    val shortFilm: Boolean? = null,

    @Json(name = "completed")
    val completed: Boolean? = null
)

/////////////////////////////////////////////////////////////////////////////////////////////////////////
@JsonClass(generateAdapter = true)
data class Staff (

    @Json(name = "staffId")
    val staffID: Long? = null,

    @Json(name = "nameRu")
    val nameRu: String? = null,

    @Json(name = "nameEn")
    val nameEn: String? = null,

    @Json(name = "description")
    val description: String? = null,

    @Json(name = "posterUrl")
    val posterURL: String? = null,

    @Json(name = "professionText")
    val professionText: String? = null,

    @Json(name = "professionKey")
    val professionKey: String? = null
)

/////////////////////////////////////////////////////////////////////////////////////////////////////////
@JsonClass(generateAdapter = true)
data class StaffInfo (

    @Json(name = "personId")
    val personID: Long? = null,

    @Json(name = "webUrl")
    val webURL: String? = null,

    @Json(name = "nameRu")
    val nameRu: String? = null,

    @Json(name = "nameEn")
    val nameEn: String? = null,

    @Json(name = "sex")
    val sex: String? = null,

    @Json(name = "posterUrl")
    val posterURL: String? = null,

    @Json(name = "growth")
    val growth: Long? = null,

    @Json(name = "birthday")
    val birthday: String? = null,

    @Json(name = "death")
    val death: String? = null,

    @Json(name = "age")
    val age: Long? = null,

    @Json(name = "birthplace")
    val birthPlace: String? = null,

    @Json(name = "deathplace")
    val deathPlace: String? = null,

    @Json(name = "spouses")
    val spouses: List<Spouse>? = null,

    @Json(name = "hasAwards")
    val hasAwards: Long? = null,

    @Json(name = "profession")
    val profession: String? = null,

    @Json(name = "facts")
    val facts: List<String>? = null,

    @Json(name = "films")
    val films: List<Film>? = null
)

@JsonClass(generateAdapter = true)
data class Spouse (

    @Json(name = "personId")
    val personID: Long? = null,

    @Json(name = "name")
    val name: String? = null,

    @Json(name = "divorced")
    val divorced: Boolean? = null,

    @Json(name = "divorcedReason")
    val divorcedReason: String? = null,

    @Json(name = "sex")
    val sex: String? = null,

    @Json(name = "children")
    val children: Long? = null,

    @Json(name = "webUrl")
    val webURL: String? = null,

    @Json(name = "relation")
    val relation: String? = null
)


