import android.net.Uri

data class MediaItem(
    val uri: Uri,
    val type: MediaType
)

enum class MediaType {
    IMAGE,
    VIDEO
}
