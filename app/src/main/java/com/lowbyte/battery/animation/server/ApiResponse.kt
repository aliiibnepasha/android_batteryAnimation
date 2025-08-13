import com.google.gson.annotations.SerializedName

data class ApiResponse(
    @SerializedName("generated_at") val generatedAt: String,
    val sort: String,
    val order: String,
    val category: String?,
    val total: Int,
    val limit: Int,
    val offset: Int,
    val categories: List<Category>
)

data class Category(
    val name: String,
    @SerializedName("updated_at") val updatedAt: String,
    val folders: List<Folder>
)

data class Folder(
    val name: String,
    @SerializedName("updated_at") val updatedAt: String,
    val files: List<FileItem>
)

data class FileItem(
    val name: String,
    val url: String,
    val size: Long,
    @SerializedName("updated_at") val updatedAt: String
)
