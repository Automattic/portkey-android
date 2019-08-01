package com.automattic.photoeditor.state

import android.content.Context
import android.preference.PreferenceManager
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event.ON_CREATE
import androidx.lifecycle.Lifecycle.Event.ON_DESTROY
import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.Lifecycle.Event.ON_STOP
import androidx.lifecycle.Lifecycle.Event.ON_RESUME
import androidx.lifecycle.Lifecycle.Event.ON_PAUSE
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.automattic.photoeditor.camera.Camera2BasicHandling
import com.automattic.photoeditor.camera.VideoPlayingBasicHandling
import com.automattic.photoeditor.views.PhotoEditorView


class BackgroundSurfaceManager(
    private val context: Context,
    private val lifeCycle: Lifecycle,
    private val photoEditorView: PhotoEditorView,
    private val supportFragmentManager: FragmentManager) : LifecycleObserver {
    private lateinit var camera2BasicHandler: Camera2BasicHandling
    private lateinit var videoPlayerHandling: VideoPlayingBasicHandling

    // state flags
    private var isCameraVisible : Boolean = false
    private var isVideoPlayerVisible: Boolean = false
    private var isCameraRecording: Boolean = false

    @OnLifecycleEvent(ON_CREATE)
    fun onCreate(source: LifecycleOwner) {
        // clear surfaceTexture listeners
        photoEditorView.listeners.clear()
        getStateFromPrefs()

        // ask FragmentManager to add the headless fragment so it receives the Activity's lifecycle callback calls
        val cameraFragment = supportFragmentManager.findFragmentByTag(KEY_CAMERA_HANDLING_FRAGMENT_TAG)
        if (cameraFragment == null ) {
            camera2BasicHandler = Camera2BasicHandling.getInstance(photoEditorView.textureView)
            supportFragmentManager
                .beginTransaction().add(camera2BasicHandler, KEY_CAMERA_HANDLING_FRAGMENT_TAG).commit()
        } else {
            // get the existing camera2BasicHandler object reference in this new Activity instance
            camera2BasicHandler = cameraFragment as Camera2BasicHandling
            // the photoEditorView layout has been recreated so, re-assign its TextureView
            camera2BasicHandler.textureView = photoEditorView.textureView
        }
        // add camera handling texture listener
        photoEditorView.listeners.add(camera2BasicHandler.surfaceTextureListener)


        // ask FragmentManager to add the headless fragment so it receives the Activity's lifecycle callback calls
        val videoPlayerFragment = supportFragmentManager.findFragmentByTag(KEY_VIDEOPLAYER_HANDLING_FRAGMENT_TAG)
        if (videoPlayerFragment == null ) {
            videoPlayerHandling = VideoPlayingBasicHandling.getInstance(photoEditorView.textureView)
            supportFragmentManager
                .beginTransaction().add(videoPlayerHandling, KEY_VIDEOPLAYER_HANDLING_FRAGMENT_TAG).commit()
        } else {
            // get the existing VideoPlayingBasicHandling object reference in this new Activity instance
            videoPlayerHandling = videoPlayerFragment as VideoPlayingBasicHandling
            // the photoEditorView layout has been recreated so, re-assign its TextureView
            videoPlayerHandling.textureView = photoEditorView.textureView
        }
        // add video player texture listener
        photoEditorView.listeners.add(videoPlayerHandling.surfaceTextureListener)
    }

    @OnLifecycleEvent(ON_DESTROY)
    fun onDestroy(source: LifecycleOwner) {
        if (lifeCycle.currentState.isAtLeast(Lifecycle.State.DESTROYED)) {
            // clear surfaceTexture listeners
            photoEditorView.listeners.clear()
            saveStateToPrefs()
        }
        // stop listening to events - should be safe to not remove it as per mentioned here
        // https://github.com/googlecodelabs/android-lifecycles/issues/5#issuecomment-303717013
        // but, removing it in ON_DESTROY just in case
        lifeCycle.removeObserver(this)
    }

    @OnLifecycleEvent(ON_START)
    fun onStart(source: LifecycleOwner) {
        // TODO: get state and restart fragments / camera preview?
        getStateFromPrefs()
    }
    @OnLifecycleEvent(ON_STOP)
    fun onStop(source: LifecycleOwner) {
        // TODO: save state and pause fragments / camera preview?
        saveStateToPrefs()
    }

    @OnLifecycleEvent(ON_RESUME)
    fun onResume(source: LifecycleOwner) {
        // TODO: get state and restart fragments / camera preview?
    }

    @OnLifecycleEvent(ON_PAUSE)
    fun onPause(source: LifecycleOwner) {
        // TODO: save state and pause fragments / camera preview?
    }

    private fun saveStateToPrefs() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = prefs.edit()
        editor.putBoolean(KEY_IS_CAMERA_VISIBLE, isCameraVisible)
        editor.putBoolean(KEY_IS_VIDEO_PLAYER_VISIBLE, isVideoPlayerVisible)
        editor.putBoolean(KEY_IS_CAMERA_RECORDING, isCameraRecording)
        editor.apply()
    }

    private fun getStateFromPrefs() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        isCameraVisible = prefs.getBoolean(KEY_IS_CAMERA_VISIBLE, true)
        isVideoPlayerVisible = prefs.getBoolean(KEY_IS_VIDEO_PLAYER_VISIBLE, false)
        isCameraRecording = prefs.getBoolean(KEY_IS_CAMERA_RECORDING, false)
    }

    companion object {
        private const val KEY_CAMERA_HANDLING_FRAGMENT_TAG = "CAMERA_TAG"
        private const val KEY_IS_CAMERA_VISIBLE = "key_is_camera_visible"
        private const val KEY_VIDEOPLAYER_HANDLING_FRAGMENT_TAG = "VIDEOPLAYER_TAG"
        private const val KEY_IS_VIDEO_PLAYER_VISIBLE = "key_is_video_player_visible"
        private const val KEY_IS_CAMERA_RECORDING = "key_is_camera_recording"
    }
}
