package com.example.data.sample

import android.content.Context
import com.example.domain.model.Song
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sin

object SampleAudioProvider {

    fun generateSampleAudioFilesIfNeeded(context: Context): List<Song> {
        // Clean up any previously generated sample music files
        try {
            val sampleDir = File(context.cacheDir, "sample_music")
            if (sampleDir.exists()) {
                sampleDir.deleteRecursively()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList()
    }

    private fun generateWavFile(file: File, frequencies: DoubleArray, durationSeconds: Int) {
        val sampleRate = 22050 // 22.05 kHz for compact size and fast generation
        val numSamples = durationSeconds * sampleRate
        val pcmData = ByteArray(numSamples * 2) // 16-bit mono

        var pcmIdx = 0
        val twopi = 2.0 * Math.PI

        for (i in 0 until numSamples) {
            val time = i.toDouble() / sampleRate
            
            // Musical chord with gentle envelope & bass pulse
            var sampleVal = 0.0
            val beatSec = (time % 2.0)
            val envelope = if (beatSec < 0.1) beatSec / 0.1 else (2.0 - beatSec) / 1.9

            for ((idx, freq) in frequencies.withIndex()) {
                val amplitude = 0.25 / (idx + 1)
                // Add subtle vibrato / lfo
                val lfo = 1.0 + 0.02 * sin(twopi * 4.0 * time)
                sampleVal += amplitude * sin(twopi * freq * lfo * time)
            }

            // Apply envelope
            sampleVal *= (0.7 + 0.3 * envelope)
            
            // Clamp sampleVal
            val clamped = (sampleVal.coerceIn(-1.0, 1.0) * 32767).toInt().toShort()

            pcmData[pcmIdx++] = (clamped.toInt() and 0xFF).toByte()
            pcmData[pcmIdx++] = ((clamped.toInt() shr 8) and 0xFF).toByte()
        }

        FileOutputStream(file).use { out ->
            val wavHeader = createWavHeader(pcmData.size, sampleRate, 1, 16)
            out.write(wavHeader)
            out.write(pcmData)
        }
    }

    private fun createWavHeader(dataLen: Int, sampleRate: Int, channels: Int, bitsPerSample: Int): ByteArray {
        val totalDataLen = dataLen + 36
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val buffer = ByteBuffer.allocate(44)
        buffer.order(ByteOrder.LITTLE_ENDIAN)

        buffer.put("RIFF".toByteArray())
        buffer.putInt(totalDataLen)
        buffer.put("WAVE".toByteArray())
        buffer.put("fmt ".toByteArray())
        buffer.putInt(16) // Subchunk1Size for PCM
        buffer.putShort(1.toShort()) // AudioFormat 1 = PCM
        buffer.putShort(channels.toShort())
        buffer.putInt(sampleRate)
        buffer.putInt(byteRate)
        buffer.putShort((channels * bitsPerSample / 8).toShort()) // BlockAlign
        buffer.putShort(bitsPerSample.toShort())
        buffer.put("data".toByteArray())
        buffer.putInt(dataLen)

        return buffer.array()
    }

    private data class SampleTrackInfo(
        val id: Long,
        val title: String,
        val artist: String,
        val album: String,
        val genre: String,
        val frequencies: DoubleArray,
        val durationSeconds: Int,
        val filename: String,
        val lyrics: String
    )
}
