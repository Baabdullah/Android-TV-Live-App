package com.openlive.tv

import com.openlive.tv.models.*
import com.openlive.tv.transfer.Playlists
import com.openlive.tv.utilities.Urls
import org.junit.Assert.*
import org.junit.Test

class ConfigurationTest {
    private fun channel(url: String = "https://example.org/live.m3u8") = Channel(id = "test", name = "Test", streamUrl = url)
    @Test fun hlsClassificationIgnoresQueryAndCase() { assertEquals(StreamType.HLS, Urls.classify("https://example.org/LIVE.M3U8?quality=hd")) }
    @Test fun directClassification() { assertEquals(StreamType.DIRECT, Urls.classify("https://example.org/live")) }
    @Test fun youtubeSupportedFormats() {
        val id = "abcdefghijk"
        listOf("https://youtu.be/$id", "https://www.youtube.com/watch?v=$id&feature=share", "https://youtube.com/live/$id", "https://m.youtube.com/shorts/$id", "https://youtube.com/embed/$id").forEach {
            assertEquals(id, Urls.youtubeId(it)); assertEquals(StreamType.YOUTUBE, Urls.classify(it))
        }
    }
    @Test fun youtubeSpoofHostsAndInvalidIdsRejected() {
        listOf("https://youtube.com.evil.org/watch?v=abcdefghijk", "https://evil.org/abcdefghijk", "https://youtube.com/watch?v=short", "javascript:abcdefghijk").forEach { assertNull(Urls.youtubeId(it)) }
    }
    @Test fun invalidAndInsecureUrlsRejected() {
        listOf("", "broken", "http://example.org/a", "file:///a", "https://", "https://user:pass@example.org/live", "https://example.org/live?token=secret").forEach { assertFalse(it, Urls.secureUrl(it)) }
    }
    @Test fun missingNameAndInvalidLogoRejected() { assertEquals(2, Urls.validate(channel().copy(name = "", logoUrl = "http://example.org/a.png")).size) }
    @Test fun validChannelAccepted() { assertTrue(Urls.validate(channel()).isEmpty()) }
    @Test fun wrongYouTubeTypeRejected() { assertTrue(Urls.validate(channel("https://youtu.be/abcdefghijk")).isNotEmpty()) }
    @Test fun jsonRoundTripPreservesFields() {
        val channels = listOf(channel().copy(isFavorite = true, lastWatched = 123, category = "News \"World\""))
        assertEquals(channels, Playlists.decode(Playlists.encode(channels)).channels)
    }
    @Test fun emptyConfigurationRoundTrips() { assertTrue(Playlists.decode("{\"channels\":[]}").channels.isEmpty()) }
    @Test fun duplicateIdsSkippedWithWarning() { val result = Playlists.decode(Playlists.encode(listOf(channel(), channel()))); assertEquals(1, result.channels.size); assertEquals(1, result.warnings.size) }
    @Test fun malformedEntriesSkippedWithoutLosingValidChannels() {
        val result = Playlists.decode(Playlists.encode(listOf(channel(), channel().copy(id = "bad", streamUrl = "broken"))))
        assertEquals(1, result.channels.size); assertEquals(1, result.warnings.size)
    }
    @Test(expected = Exception::class) fun malformedJsonFails() { Playlists.decode("{oops}") }
    @Test(expected = Exception::class) fun arbitraryTextFails() { Playlists.decode("not a playlist") }
    @Test fun m3uNamesCategoriesAndLogos() {
        val input = """#EXTM3U
#EXTINF:-1 tvg-logo="https://example.org/logo.png" group-title="News",Channel, with comma
https://example.org/live.m3u8
"""
        val c = Playlists.decode(input).channels.single()
        assertEquals("Channel, with comma", c.name); assertEquals("News", c.category); assertEquals("https://example.org/logo.png", c.logoUrl)
    }
    @Test fun malformedM3uEntriesProduceWarnings() {
        val r = Playlists.decode("#EXTM3U\n#EXTINF:-1,Missing\n#EXTINF:-1,Unsafe\nhttp://example.org/live\n#EXTINF:-1,Good\nhttps://example.org/live.m3u8\n#EXTINF:-1,Trailing")
        assertEquals(1, r.channels.size); assertEquals(3, r.warnings.size)
    }
    @Test fun quotedCommasDoNotTruncateM3uMetadata() {
        val r = Playlists.decode("#EXTM3U\n#EXTINF:-1 group-title=\"News, World\",World TV\nhttps://example.org/live.m3u8")
        assertEquals("World TV", r.channels.single().name)
        assertEquals("News, World", r.channels.single().category)
    }
    @Test fun unknownSecretQueryParametersRejected() {
        assertFalse(Urls.secureUrl("https://example.org/live?signature=secret"))
        assertFalse(Urls.secureUrl("https://example.org/live?session=secret"))
    }
    @Test fun emptyM3uHasNoChannels() { assertTrue(Playlists.decode("#EXTM3U\n").channels.isEmpty()) }
}
