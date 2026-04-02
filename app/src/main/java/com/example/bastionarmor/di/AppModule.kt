package com.example.bastionarmor.di
import android.content.Context
import androidx.room.Room
import com.example.bastionarmor.data.db.GameDatabase
import com.example.bastionarmor.data.db.GameStateDao
import com.example.bastionarmor.data.repository.GameRepositoryImpl
import com.example.bastionarmor.domain.repository.GameRepository
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun provideGson(): Gson = Gson()

    @Provides
    fun provideGameDatabase(@ApplicationContext context: Context): GameDatabase =
        Room.databaseBuilder(context, GameDatabase::class.java, "game_db")
            .build()

    @Provides
    fun provideGameStateDao(db: GameDatabase): GameStateDao = db.gameStateDao()

    @Provides
    fun provideGameRepository(dao: GameStateDao, gson: Gson): GameRepository =
        GameRepositoryImpl(dao, gson)
}