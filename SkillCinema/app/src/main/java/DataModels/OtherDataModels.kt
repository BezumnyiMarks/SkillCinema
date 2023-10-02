package DataModels

data class BottomSheetItemDataModel(
    var includesCurrentFilm: Boolean,
    val collectionName: String,
    val filmsNumber: Int
)

data class DynamicSelectionRequestAttributes(
    val countryID: Long,
    val genreID: Long,
    val type: String
)

data class ProfileCollection(
    val collectionName: String,
    val filmsAmount: Int
)

data class OnboardingAdapterItems(
    val image: Int,
    val text: String
)
