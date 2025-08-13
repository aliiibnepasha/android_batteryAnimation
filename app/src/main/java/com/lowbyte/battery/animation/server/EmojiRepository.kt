package com.lowbyte.battery.animation.server


import ApiResponse
import Category
import FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.Locale

class EmojiRepository(
    private val api: EmojiApiService
) {
    @Volatile private var lastResponse: ApiResponse? = null

    /** Internal helper to ensure data is loaded */
    private suspend fun ensureAllData(url: String, forceRefresh: Boolean): ApiResponse {
        if (forceRefresh || lastResponse == null) {
            val data = api.getAll(url)
            lastResponse = data
        }
        return lastResponse!!
    }

    /** Get full API data (may be cached) */
    fun getAllData(url: String, forceRefresh: Boolean = false): Flow<Resource<ApiResponse>> = flow {
        emit(Resource.Loading)
        try {
            val data = ensureAllData(url, forceRefresh)
            emit(Resource.Success(data))
        } catch (t: Throwable) {
            emit(Resource.Error(ErrorMapper.map(t), t))
        }
    }.flowOn(Dispatchers.IO)

    /** Get category names from cached/all data */
    fun getAllCategoryNames(url: String, forceRefresh: Boolean = false): Flow<Resource<List<String>>> = flow {
        emit(Resource.Loading)
        try {
            val data = ensureAllData(url, forceRefresh)
            emit(Resource.Success(data.categories.map { it.name }))
        } catch (t: Throwable) {
            emit(Resource.Error(ErrorMapper.map(t), t))
        }
    }.flowOn(Dispatchers.IO)

    /** Get a single category from cached/all data */
    fun getCategoryByName(url: String, name: String, forceRefresh: Boolean = false): Flow<Resource<Category>> = flow {
        emit(Resource.Loading)
        try {
            val data = ensureAllData(url, forceRefresh)
            val cat = data.categories.firstOrNull { it.name.equals(name, ignoreCase = true) }
            if (cat != null) {
                emit(Resource.Success(cat))
            } else {
                emit(Resource.Error("Category '$name' not found"))
            }
        } catch (t: Throwable) {
            emit(Resource.Error(ErrorMapper.map(t), t))
        }
    }.flowOn(Dispatchers.IO)

    /** Get PNG files from cached/all data */
    fun getFolderPngs(url: String, categoryName: String, folderName: String, forceRefresh: Boolean = false): Flow<Resource<List<FileItem>>> = flow {
        emit(Resource.Loading)
        try {
            val data = ensureAllData(url, forceRefresh)
            val cat = data.categories.firstOrNull { it.name.equals(categoryName, ignoreCase = true) }
                ?: return@flow emit(Resource.Error("Category '$categoryName' not found"))
            val folder = cat.folders.firstOrNull { it.name.equals(folderName, ignoreCase = true) }
                ?: return@flow emit(Resource.Error("Folder '$folderName' not found in '$categoryName'"))
            val pngs = folder.files.filter { it.name.lowercase(Locale.US).endsWith(".png") }
            emit(Resource.Success(pngs))
        } catch (t: Throwable) {
            emit(Resource.Error(ErrorMapper.map(t), t))
        }
    }.flowOn(Dispatchers.IO)
}
