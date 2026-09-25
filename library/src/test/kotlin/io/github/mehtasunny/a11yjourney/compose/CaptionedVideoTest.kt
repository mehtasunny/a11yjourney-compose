package io.github.mehtasunny.a11yjourney.compose

import android.net.Uri
import androidx.media3.common.C
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CaptionedVideoTest {

    @Test
    fun captionTrackIsAttachedAndSelectedByDefault() {
        val item = CaptionedVideo.WithCaptions(
            videoUri = Uri.parse("https://example.org/apply.mp4"),
            captionsUri = Uri.parse("https://example.org/apply.vtt"),
        ).toMediaItem()
        val subs = item.localConfiguration!!.subtitleConfigurations
        assertEquals(1, subs.size)
        assertEquals("text/vtt", subs[0].mimeType)
        assertEquals("en", subs[0].language)
        assertTrue(subs[0].selectionFlags and C.SELECTION_FLAG_DEFAULT != 0)
    }

    @Test
    fun noSpeechVideoHasNoCaptionTrack() {
        val item = CaptionedVideo.NoSpeech(Uri.parse("https://example.org/loop.mp4")).toMediaItem()
        assertTrue(item.localConfiguration!!.subtitleConfigurations.isEmpty())
    }
}
