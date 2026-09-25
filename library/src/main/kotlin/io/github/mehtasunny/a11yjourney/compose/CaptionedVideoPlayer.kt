package io.github.mehtasunny.a11yjourney.compose

import android.net.Uri
import androidx.annotation.OptIn as AndroidXOptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

/**
 * A video the player is allowed to play. The type makes captions impossible to forget:
 * a video either comes with a caption track or is explicitly declared to have no speech
 * or meaningful sound.
 */
public sealed interface CaptionedVideo {
    public val videoUri: Uri

    /** A video with a captions file (WebVTT by default). */
    public data class WithCaptions(
        override val videoUri: Uri,
        val captionsUri: Uri,
        val language: String = "en",
        val mimeType: String = "text/vtt",
    ) : CaptionedVideo

    /** A video with no speech or meaningful sound, so captions are not needed. */
    public data class NoSpeech(override val videoUri: Uri) : CaptionedVideo
}

/** Builds the media item, with the caption track marked as the default selection. */
internal fun CaptionedVideo.toMediaItem(): MediaItem = when (this) {
    is CaptionedVideo.WithCaptions -> MediaItem.Builder()
        .setUri(videoUri)
        .setSubtitleConfigurations(
            listOf(
                MediaItem.SubtitleConfiguration.Builder(captionsUri)
                    .setMimeType(mimeType)
                    .setLanguage(language)
                    .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                    .build(),
            ),
        )
        .build()
    is CaptionedVideo.NoSpeech -> MediaItem.fromUri(videoUri)
}

/**
 * A video player for health, transit, and public-service content such as "how to take
 * this medication" or "how to apply".
 *
 * Guarantees: captions are required by the [CaptionedVideo] type (WCAG 1.2.2), turned on
 * by default, and styled by the user's system caption settings; nothing plays until the
 * user presses play (1.4.2); the controls are this library's own labeled 48dp buttons
 * that stay on screen instead of fading out, so screen-reader and switch users can always
 * reach them (2.2.1); an optional [transcript] can be expanded below the video for users
 * who prefer text or use braille displays.
 */
@OptIn(ExperimentalLayoutApi::class)
@AndroidXOptIn(UnstableApi::class)
@Composable
public fun CaptionedVideoPlayer(
    video: CaptionedVideo,
    title: String,
    modifier: Modifier = Modifier,
    transcript: String? = null,
) {
    requireMeaningfulLabel(title, "CaptionedVideoPlayer")
    val strings = LocalA11yStrings.current
    val context = LocalContext.current
    val hasCaptions = video is CaptionedVideo.WithCaptions
    val player = remember(video) {
        ExoPlayer.Builder(context)
            .setSeekBackIncrementMs(SEEK_BACK_MS)
            .build()
            .apply {
                trackSelectionParameters = trackSelectionParameters.buildUpon()
                    .setPreferredTextLanguage((video as? CaptionedVideo.WithCaptions)?.language)
                    .setSelectUndeterminedTextLanguage(true)
                    .build()
                setMediaItem(video.toMediaItem())
                prepare()
                playWhenReady = false
            }
    }
    var playing by remember(player) { mutableStateOf(false) }
    var captionsOn by rememberSaveable { mutableStateOf(true) }
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                playing = isPlaying
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }
    LaunchedEffect(player, captionsOn) {
        player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, !captionsOn)
            .build()
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() },
        )
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    useController = false
                    contentDescription = title
                    subtitleView?.setUserDefaultStyle()
                    subtitleView?.setUserDefaultTextSize()
                }
            },
            modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ActionButton(
                text = if (playing) strings.pauseVideo else strings.playVideo,
                onClick = { if (playing) player.pause() else player.play() },
            )
            ActionButton(
                text = strings.back10Seconds,
                onClick = { player.seekBack() },
                emphasis = ButtonEmphasis.Secondary,
            )
            if (hasCaptions) {
                ActionButton(
                    text = if (captionsOn) strings.captionsOff else strings.captionsOn,
                    onClick = { captionsOn = !captionsOn },
                    emphasis = ButtonEmphasis.Secondary,
                )
            }
        }
        if (transcript != null) {
            var open by rememberSaveable { mutableStateOf(false) }
            ActionButton(
                text = if (open) strings.hideTranscript else strings.showTranscript,
                onClick = { open = !open },
                emphasis = ButtonEmphasis.Secondary,
            )
            if (open) Text(text = transcript, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

private const val SEEK_BACK_MS = 10_000L
