package com.example.player

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.audiofx.Visualizer
import android.os.SystemClock
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.math.log10
import kotlin.math.sin
import kotlin.math.sqrt

class VisualizerManager(private val context: Context) {
    private var visualizer: Visualizer? = null
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var simulationJob: Job? = null
    
    private val _fftData = MutableStateFlow(FloatArray(32) { 0f })
    val fftData = _fftData.asStateFlow()
    
    private val _waveform = MutableStateFlow(ByteArray(128) { 0 })
    val waveform = _waveform.asStateFlow()
    
    private val _rms = MutableStateFlow(0f)
    val rms = _rms.asStateFlow()

    private val peakHold = FloatArray(32) { 0f }
    private val decayRate = 0.92f

    // Reusable buffers to eliminate GC allocations during capture
    private val waveformBuffer = ByteArray(128) { 0 }
    private val fftBuffer = FloatArray(32) { 0f }

    private var lastWaveformUpdateMs = 0L
    private var lastFftUpdateMs = 0L
    private var lastDataReceivedMs = 0L
    private val minFrameIntervalMs = 33L // Throttle UI updates to ~30 FPS

    init {
        startFallbackTracker()
    }

    fun init(sessionId: Int) {
        if (sessionId <= 0) return
        
        // Check permission
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.w("VisualizerManager", "RECORD_AUDIO permission not granted. Skiping hardware Visualizer init.")
            return
        }
        
        try {
            releaseHardware()
            visualizer = Visualizer(sessionId).apply {
                captureSize = Visualizer.getCaptureSizeRange()[1]
                setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                    override fun onWaveFormDataCapture(v: Visualizer?, waveform: ByteArray?, samplingRate: Int) {
                        try {
                            waveform?.let {
                                val now = SystemClock.elapsedRealtime()
                                lastDataReceivedMs = now
                                if (now - lastWaveformUpdateMs >= minFrameIntervalMs) {
                                    lastWaveformUpdateMs = now
                                    val copyLen = minOf(it.size, 128)
                                    System.arraycopy(it, 0, waveformBuffer, 0, copyLen)
                                    _waveform.value = waveformBuffer.copyOf(128)
                                    calculateRMS(it)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("VisualizerManager", "Error in onWaveFormDataCapture", e)
                        }
                    }

                    override fun onFftDataCapture(v: Visualizer?, fft: ByteArray?, samplingRate: Int) {
                        try {
                            fft?.let {
                                val now = SystemClock.elapsedRealtime()
                                lastDataReceivedMs = now
                                if (now - lastFftUpdateMs >= minFrameIntervalMs) {
                                    lastFftUpdateMs = now
                                    processFFT(it)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("VisualizerManager", "Error in onFftDataCapture", e)
                        }
                    }
                }, Visualizer.getMaxCaptureRate() / 2, true, true)
                enabled = true
            }
        } catch (e: Exception) {
            Log.e("VisualizerManager", "Error initializing visualizer", e)
        }
    }

    private fun startFallbackTracker() {
        simulationJob?.cancel()
        simulationJob = scope.launch {
            var phase = 0f
            while (isActive) {
                val now = SystemClock.elapsedRealtime()
                // If hardware visualizer hasn't received actual audio frames recently, generate subtle organic visualizer data
                if (now - lastDataReceivedMs > 500L) {
                    phase += 0.15f
                    val simFft = FloatArray(32)
                    val simWave = ByteArray(128)
                    
                    for (i in 0 until 32) {
                        val base = (sin((phase + i * 0.3f).toDouble()).toFloat() + 1f) * 0.35f
                        val peak = (sin((phase * 1.5f + i * 0.5f).toDouble()).toFloat() + 1f) * 0.25f
                        simFft[i] = (base + peak).coerceIn(0.05f, 0.85f)
                    }
                    
                    for (i in 0 until 128) {
                        val waveVal = (sin((phase * 2f + i * 0.1f).toDouble()) * 40).toInt() + 128
                        simWave[i] = waveVal.toByte()
                    }
                    
                    _fftData.value = simFft
                    _waveform.value = simWave
                    _rms.value = (sin(phase.toDouble()).toFloat() * 0.2f + 0.35f).coerceIn(0.1f, 0.7f)
                }
                delay(40)
            }
        }
    }

    private fun processFFT(fft: ByteArray) {
        val n = fft.size
        val binSize = (n / 2) / 32
        
        for (i in 0 until 32) {
            var sum = 0f
            for (j in 0 until binSize) {
                val index = (i * binSize + j) * 2
                if (index + 1 < n) {
                    val real = fft[index].toFloat()
                    val imag = fft[index + 1].toFloat()
                    sum += hypot(real, imag)
                }
            }
            // Normalize and convert to decibels-ish representation
            val mag = (sum / binSize) / 128f
            val db = if (mag > 0) 20 * log10(mag + 1) / 30f else 0f
            
            // Peak hold logic
            val currentVal = db.coerceIn(0f, 1f)
            if (currentVal >= peakHold[i]) {
                peakHold[i] = currentVal
            } else {
                peakHold[i] *= decayRate
            }
            
            // Combine current and peak for visualization into reusable buffer
            fftBuffer[i] = (currentVal * 0.7f + peakHold[i] * 0.3f).coerceIn(0f, 1f)
        }
        _fftData.value = fftBuffer.copyOf(32)
    }

    private fun calculateRMS(waveform: ByteArray) {
        var sum = 0f
        for (i in waveform.indices) {
            val sample = (waveform[i].toInt() and 0xFF) - 128
            sum += (sample * sample).toFloat()
        }
        val rmsValue = sqrt(sum / waveform.size) / 128f
        _rms.value = rmsValue.coerceIn(0f, 1f)
    }

    private fun releaseHardware() {
        try {
            visualizer?.apply {
                enabled = false
                setDataCaptureListener(null, 0, false, false)
                release()
            }
        } catch (e: Exception) {
            Log.e("VisualizerManager", "Error releasing visualizer", e)
        }
        visualizer = null
    }

    fun release() {
        simulationJob?.cancel()
        releaseHardware()
    }
}

