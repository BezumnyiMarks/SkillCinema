package com.example.skillcinema

import DataModels.CountryID
import DataModels.DBCountry
import DataModels.DBGenre
import DataModels.DBImage
import DataModels.FilmInfo
import DataModels.FilmSelectionItemInfo
import DataModels.GalleryTab
import DataModels.GenreID
import DataModels.SearchSettings
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FilmInfo::class, DBCountry::class, DBGenre::class, FilmSelectionItemInfo::class, SearchSettings::class, CountryID::class, GenreID::class, DBImage::class, GalleryTab::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract  fun filmInfoDao(): FilmInfoDao
}