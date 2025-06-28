package com.ownstd.project.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getDatabaseBuilderAndroid(ctx: Context): RoomDatabase.Builder<FeedDatabase> {
    val appContext = ctx.applicationContext
    val dbFile = appContext.getDatabasePath("my_room.db")
    return Room.databaseBuilder<FeedDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}