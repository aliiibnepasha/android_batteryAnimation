package com.lowbyte.battery.animation.server


import ApiResponse
import Category
import FileItem
import java.util.Locale

fun ApiResponse.categoryNames(): List<String> = categories.map { it.name }

fun ApiResponse.categoryByName(name: String): Category? =
    categories.firstOrNull { it.name.equals(name, ignoreCase = true) }

fun Category.pngsInFolder(folderName: String): List<FileItem> =
    folders.firstOrNull { it.name.equals(folderName, ignoreCase = true) }
        ?.files?.filter { it.name.lowercase(Locale.US).endsWith(".png") }
        ?: emptyList()