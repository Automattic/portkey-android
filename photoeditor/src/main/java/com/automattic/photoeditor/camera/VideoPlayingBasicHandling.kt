/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.automattic.photoeditor.camera

import android.graphics.SurfaceTexture
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.TextureView
import java.io.File
import android.media.AudioManager
import android.media.MediaPlayer
import androidx.fragment.app.Fragment
import com.automattic.photoeditor.camera.interfaces.SurfaceFragmentHandler
import com.automattic.photoeditor.views.background.video.AutoFitTextureView
import java.io.FileInputStream
import java.io.IOException

class VideoPlayingBasicHandling : Fragment(), SurfaceFragmentHandler {
    // holds the File handle to the current video file to be played
    var currentFile: File? = null

    /**
     * [TextureView.SurfaceTextureListener] handles several lifecycle events on a
     * [TextureView].
     */
    val surfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) {
            if (active) {
                startVideoPlay(texture)
            }
        }

        override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) {
            // TODO figure out what to do here
            // configureTransform(width, height)
        }

        override fun onSurfaceTextureDestroyed(texture: SurfaceTexture) = true

        override fun onSurfaceTextureUpdated(texture: SurfaceTexture) = Unit
    }

    /**
     * An [AutoFitTextureView] for camera preview.
     */
    lateinit var textureView: AutoFitTextureView

    private var active: Boolean = false

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onResume() {
        super.onResume()
        startUp()
    }

    override fun onPause() {
        // pause playing video
        windDown()
        super.onPause()
    }

    fun stopVideoPlay() {
        if (active) {
            if (mediaPlayer?.isPlaying() == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.reset()
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    override fun activate() {
        active = true
        // perform all needed tasks to make Video Player up
        startUp()
    }

    override fun deactivate() {
        stopVideoPlay()
        active = false
    }

    private fun startUp() {
        if (textureView.isAvailable && active) {
            startVideoPlay(textureView.surfaceTexture)
        }
    }

    private fun windDown() {
        stopVideoPlay()
    }

    fun startVideoPlay(texture: SurfaceTexture) {
        val s = Surface(texture)
        try {
            if (mediaPlayer != null) {
                stopVideoPlay()
            }

            currentFile?.takeIf { it.exists() }?.let { file ->
                val inputStream = FileInputStream(file)
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(inputStream.getFD())
                    setSurface(s)
                    prepare()
                    // TODO check whether we want fine grained error handling by setting these listeners
    //                setOnBufferingUpdateListener(this)
    //                setOnCompletionListener(this)
    //                setOnPreparedListener(this)
    //                setOnVideoSizeChangedListener(this)
                    setAudioStreamType(AudioManager.STREAM_MUSIC)
                    start()
                }
            }
        } catch (e: IllegalArgumentException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        } catch (e: SecurityException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }

    companion object {
        private val instance = VideoPlayingBasicHandling()

        /**
         * Tag for the [Log].
         */
        private val TAG = "VideoPlayingBasicHandling"

        @JvmStatic fun getInstance(textureView: AutoFitTextureView): VideoPlayingBasicHandling {
            instance.textureView = textureView
            return instance
        }
    }
}
