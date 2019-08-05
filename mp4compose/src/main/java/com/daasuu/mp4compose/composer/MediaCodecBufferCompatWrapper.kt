package com.daasuu.mp4compose.composer

import android.media.MediaCodec
import android.os.Build

import java.nio.ByteBuffer

// Refer: https://github.com/ypresto/android-transcoder/blob/master/lib/src/main/java/net/ypresto/androidtranscoder/compat/MediaCodecBufferCompatWrapper.java

/**
 * A Wrapper to MediaCodec that facilitates the use of API-dependent get{Input/Output}Buffer methods,
 * in order to prevent: http://stackoverflow.com/q/30646885
 */

internal class MediaCodecBufferCompatWrapper(private val mediaCodec: MediaCodec) {
    private val inputBuffers: Array<ByteBuffer>
    private val putputBuffers: Array<ByteBuffer>?

    init {

        if (Build.VERSION.SDK_INT < 21) {
            inputBuffers = mediaCodec.inputBuffers
            putputBuffers = mediaCodec.outputBuffers
        } else {
            putputBuffers = null
            inputBuffers = putputBuffers
        }
    }

    fun getInputBuffer(index: Int): ByteBuffer? {
        return if (Build.VERSION.SDK_INT >= 21) {
            mediaCodec.getInputBuffer(index)
        } else inputBuffers[index]
    }

    fun getOutputBuffer(index: Int): ByteBuffer? {
        return if (Build.VERSION.SDK_INT >= 21) {
            mediaCodec.getOutputBuffer(index)
        } else putputBuffers!![index]
    }
}