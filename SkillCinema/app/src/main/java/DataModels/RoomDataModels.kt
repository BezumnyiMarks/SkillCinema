package DataModels

import androidx.room.*

data class MaxFilmInfo (
    @Embedded
    val filmInfo: FilmInfo,

    @Relation(entity = DBCountry::class, parentColumn = "filmID", entityColumn = "filmID")
    val country: List<DBCountry>,

    @Relation(entity = DBGenre::class, parentColumn = "filmID", entityColumn = "filmID")
    val genre: List<DBGenre>
)

@Entity(tableName = "FilmInfo")
data class FilmInfo(
    @PrimaryKey
    @ColumnInfo(name = "filmID")
    val filmID: Long,

    @ColumnInfo(name = "updateDate")
    val updateTimeInMillis: Long,

    @ColumnInfo(name = "posterURL")
    val posterURL: String?,

    @ColumnInfo(name = "logoURL")
    val logoURL: String?,

    @ColumnInfo(name = "ratingKinopoisk")
    val ratingKinopoisk: Double?,

    @ColumnInfo(name = "ratingImdb")
    val ratingImdb: Double?,

    @ColumnInfo(name = "ratingFilmCritics")
    val ratingFilmCritics: Double?,

    @ColumnInfo(name = "nameOriginal")
    val nameOriginal: String?,

    @ColumnInfo(name = "nameRu")
    val nameRu: String?,

    @ColumnInfo(name = "nameEn")
    val nameEn: String?,

    @ColumnInfo(name = "year")
    val year: Long?,

    @ColumnInfo(name = "filmLength")
    val filmLength: Long?,

    @ColumnInfo(name = "ratingAgeLimits")
    val ratingAgeLimits: String?,

    @ColumnInfo(name = "shortDescription")
    val shortDescription: String?,

    @ColumnInfo(name = "description")
    val description: String?,

    @ColumnInfo(name = "serial")
    val serial: Boolean?
)

@Entity(tableName = "DBCountry")
data class DBCountry (
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "filmID")
    val filmID: Long,

    @ColumnInfo(name = "country")
    val country: String
)

@Entity(tableName = "DBGenre")
data class DBGenre (
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "filmID")
    val filmID: Long,

    @ColumnInfo(name = "genre")
    val genre: String
)

@Entity(tableName = "SearchSettings")
data class SearchSettings(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "countryID")
    var countryID: Int,

    @ColumnInfo(name = "genreID")
    var genreID: Int,

    @ColumnInfo(name = "order")
    var order: String,

    @ColumnInfo(name = "type")
    var type: String,

    @ColumnInfo(name = "ratingFrom")
    var ratingFrom: Int,

    @ColumnInfo(name = "ratingTo")
    var ratingTo: Int,

    @ColumnInfo(name = "yearFrom")
    var yearFrom: Int,

    @ColumnInfo(name = "yearTo")
    var yearTo: Int,

    @ColumnInfo(name = "watched")
    var watched: Boolean
)

@Entity(tableName = "CountryID")
data class CountryID (
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "country")
    val country: String
)

@Entity(tableName = "GenreID")
data class GenreID (
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "genre")
    val genre: String
)

data class MaxFilmSelectionItemInfo (
    @Embedded
    val filmInfo: FilmSelectionItemInfo,

    @Relation(entity = DBGenre::class, parentColumn = "id", entityColumn = "filmID")
    val genre: List<DBGenre>
)

@Entity(tableName = "FilmSelectionItemInfo")
data class FilmSelectionItemInfo(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "filmID")
    val filmID: Int,

    @ColumnInfo(name = "selectionAttachment")
    val selectionAttachment: String?,

    @ColumnInfo(name = "posterURL")
    val imageURL: String?,

    @ColumnInfo(name = "ratingKinopoisk")
    val ratingKinopoisk: String?,

    @ColumnInfo(name = "nameRu")
    val nameRu: String?,

    @ColumnInfo(name = "nameEn")
    val nameEn: String?,

    @ColumnInfo(name = "nameOriginal")
    val nameOriginal: String?
)

@Entity(tableName = "Image")
data class DBImage(
    @PrimaryKey
    @ColumnInfo(name = "imageUrl")
    val imageUrl: String,

    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "filmAttachment")
    val filmAttachment: Int,
)

@Entity(tableName = "GalleryTab")
data class GalleryTab(
    @PrimaryKey
    @ColumnInfo(name = "editTime")
    val editTime: Long,

    @ColumnInfo(name = "filmAttachment")
    val filmAttachment: Int,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "type")
    val type: String
)
