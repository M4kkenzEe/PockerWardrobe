package com.ownstd.project.database

import androidx.room.Room
import androidx.room.RoomDatabase
import platform.Foundation.NSHomeDirectory

fun getDatabaseBuilderIos(): RoomDatabase.Builder<FeedDatabase> {
    val dbFilePath = NSHomeDirectory() + "/my_room.db"
    return Room.databaseBuilder<FeedDatabase>(
        name = dbFilePath,
        factory =  { FeedDatabase::class.instantiateImpl() }  // IDE may show error but there is none.
    )
}