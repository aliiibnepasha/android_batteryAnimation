package com.lowbyte.battery.animation.server

import ApiResponse
import Category
import FileItem
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EmojiViewModel(private val repo: EmojiRepository) : ViewModel() {

    private val _allData = MutableStateFlow<Resource<ApiResponse>>(Resource.Loading)
    val allData: StateFlow<Resource<ApiResponse>> = _allData.asStateFlow()

    private val _categories = MutableStateFlow<Resource<List<String>>>(Resource.Loading)
    val categories: StateFlow<Resource<List<String>>> = _categories.asStateFlow()

    private val _singleCategory = MutableStateFlow<Resource<Category>>(Resource.Loading)
    val singleCategory: StateFlow<Resource<Category>> = _singleCategory.asStateFlow()

    private val _pngs = MutableStateFlow<Resource<List<FileItem>>>(Resource.Loading)
    val pngs: StateFlow<Resource<List<FileItem>>> = _pngs.asStateFlow()

    fun loadAll(url: String, forceRefresh: Boolean = false) = viewModelScope.launch {
        repo.getAllData(url, forceRefresh).collect { _allData.value = it }
    }

    fun loadCategoryNames(url: String, forceRefresh: Boolean = false) = viewModelScope.launch {
        repo.getAllCategoryNames(url, forceRefresh).collect { _categories.value = it }
    }

    fun loadCategory(url: String, name: String, forceRefresh: Boolean = false) = viewModelScope.launch {
        repo.getCategoryByName(url, name, forceRefresh).collect { _singleCategory.value = it }
    }

    fun loadFolderPngs(url: String, categoryName: String, folderName: String, forceRefresh: Boolean = false) = viewModelScope.launch {
        repo.getFolderPngs(url, categoryName, folderName, forceRefresh).collect { _pngs.value = it }
    }
}