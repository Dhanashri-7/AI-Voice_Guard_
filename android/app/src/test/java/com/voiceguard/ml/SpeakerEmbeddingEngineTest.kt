package com.voiceguard.ml

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.sqrt

class SpeakerEmbeddingEngineTest {

    @Test
    fun testEmbeddingExtractionDimensionAndNorm() {
        val dummyPcm = ShortArray(1600) { i ->
            (kotlin.math.sin(i * 0.1) * 10000.0).toInt().toShort()
        }
        val embedding = SpeakerEmbeddingEngine.extractEmbedding(dummyPcm)

        assertEquals("Embedding dimension must be 64", 64, embedding.size)

        var normSq = 0.0
        for (v in embedding) {
            normSq += v * v
        }
        val norm = sqrt(normSq)
        assertEquals("L2 norm must be approximately 1.0", 1.0, norm, 1e-4)
    }

    @Test
    fun testCosineSimilarityIdenticalAndOpposite() {
        val v1 = FloatArray(64) { 0.125f }
        val v2 = FloatArray(64) { 0.125f }

        val simIdentical = SpeakerEmbeddingEngine.computeCosineSimilarity(v1, v2)
        assertEquals("Cosine similarity of identical vectors must be 1.0", 1.0f, simIdentical, 1e-5f)

        val vOpposite = FloatArray(64) { -0.125f }
        val simOpposite = SpeakerEmbeddingEngine.computeCosineSimilarity(v1, vOpposite)
        assertEquals("Cosine similarity of opposite vectors must be -1.0", -1.0f, simOpposite, 1e-5f)
    }

    @Test
    fun testSpeakerVerificationResult() {
        val enrolled = FloatArray(64) { i -> (i.toFloat() + 1.0f) / 100.0f }
        val liveIdentical = enrolled.clone()

        val resultMatch = SpeakerEmbeddingEngine.verifySpeaker(liveIdentical, enrolled, "Mom")
        assertTrue("Identical embeddings should match", resultMatch.isMatched)
        assertEquals("Mom", resultMatch.matchedContactName)
        assertTrue("Cosine similarity should be >= 0.99", resultMatch.cosineSimilarity >= 0.99f)

        // Orthogonal / different vector
        val liveDivergent = FloatArray(64) { i -> if (i % 2 == 0) 1.0f else -1.0f }
        val resultMismatch = SpeakerEmbeddingEngine.verifySpeaker(liveDivergent, enrolled, "Mom")
        assertFalse("Divergent voice should not match", resultMismatch.isMatched)
        assertNull(resultMismatch.matchedContactName)
    }

    @Test
    fun testEmbeddingSerializationRoundTrip() {
        val original = FloatArray(64) { i -> (i * 1.5f) - 48.0f }
        val json = SpeakerEmbeddingEngine.serializeEmbedding(original)
        assertNotNull(json)
        assertTrue(json.startsWith("[") && json.endsWith("]"))

        val deserialized = SpeakerEmbeddingEngine.deserializeEmbedding(json)
        assertNotNull(deserialized)
        assertEquals(64, deserialized!!.size)
        for (i in original.indices) {
            assertEquals("Values at index $i must match", original[i], deserialized[i], 1e-5f)
        }
    }
}
