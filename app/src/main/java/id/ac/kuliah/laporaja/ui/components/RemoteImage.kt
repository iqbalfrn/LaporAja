package id.ac.kuliah.laporaja.ui.components

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material.icons.outlined.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade

/**
 * Satu-satunya tempat pemuatan gambar dari Storage (Subbab 9.3 nomor 6).
 * URL publik dibentuk dari path relatif bucket lewat PublicUrlBuilder.
 */
@Composable
fun RemoteImage(
    url: String?,
    contentDescription: String?,
    aspectRatio: Float,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val placeholder = rememberVectorPainter(Icons.Outlined.Image)
    val failure = rememberVectorPainter(Icons.Outlined.BrokenImage)
    val platformContext = LocalPlatformContext.current
    AsyncImage(
        model = url?.let { ImageRequest.Builder(platformContext).data(it).crossfade(true).build() },
        contentDescription = contentDescription,
        placeholder = placeholder,
        error = failure,
        contentScale = contentScale,
        modifier = modifier.aspectRatio(aspectRatio),
    )
}
