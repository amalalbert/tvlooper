package com.example.tvvideolooper

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerView
import com.example.tvvideolooper.models.model
import com.github.angads25.filepicker.model.DialogConfigs
import com.github.angads25.filepicker.model.DialogProperties
import com.github.angads25.filepicker.view.FilePickerDialog
import java.io.File


class MainActivity : Activity(){

    private val playbackStateListener: Player.Listener = playbackStateListener()
    private var player: ExoPlayer? = null

    private var playWhenReady = true
    private var currentItem = 0
    private var playbackPosition = 0L
    private  var dpath: Array<String>? = null
    lateinit var videoView: PlayerView
    val READ_STORAGE_PERMISSION_REQUEST_CODE = 41;



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        videoView = findViewById(R.id.video_view)


    }

    public override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            if (!checkPermission()) {
                requestPermissionForReadExtertalStorage()
        }
            showFilePicker()
    }

    public override fun onResume() {
        Log.d("devhell", "onResume: ")
        super.onResume()
        hideSystemUi()
        if (Util.SDK_INT <= 23 || player == null) {
            if (!checkPermission()) {
                requestPermissionForReadExtertalStorage()
            } else {
                initializePlayer()
            }
        }
    }

    public override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    public override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    fun showFilePicker() {
        val properties = DialogProperties()
        val systemPath = "/storage/emulated/0/"
        properties.selection_mode = DialogConfigs.MULTI_MODE
        properties.selection_type = DialogConfigs.FILE_SELECT
//        properties.root = File(DialogConfigs.DEFAULT_DIR)
        properties.root = File(systemPath)
        properties.error_dir = File(systemPath)
        properties.offset = File(systemPath)
        properties.extensions = null

        val dialog = FilePickerDialog(this@MainActivity, properties)
        dialog.setTitle("Select a File")
        dialog.setNegativeBtnName("")
        dialog.show()
        dialog.setDialogSelectionListener {
            Log.d("devhell", "showFilePicker: ${it.size}")

            dpath = it.clone()
            initializePlayer()
        }
//        dialog.setDialogSelectionListener { it ->
//            Log.d("devhell", "showFilePicker: ${it}")
//            dpath = it.clone()
//            //files is the array of the paths of files selected by the Application User.
//        }


    }

    private fun initializePlayer() {
        Log.d("devhell", "initializePlayer: ")
        val trackSelector = DefaultTrackSelector(this).apply {
            setParameters(buildUponParameters().setMaxVideoSizeSd())
        }
        player = ExoPlayer.Builder(this)
            .setTrackSelector(trackSelector)
            .build()
            .also { exoPlayer ->
                videoView.player = exoPlayer

                addMediaItems(exoPlayer)

                exoPlayer.playWhenReady = playWhenReady

//                exoPlayer.seekTo(currentItem, playbackPosition)
//                exoPlayer.addListener(playbackStateListener)
                exoPlayer.repeatMode = ExoPlayer.REPEAT_MODE_ALL
                exoPlayer.prepare()
            }
    }

    private fun checkPermission(): Boolean {
        val result =
            applicationContext.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissionForReadExtertalStorage() {
        try {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_STORAGE_PERMISSION_REQUEST_CODE
            )
        } catch (e: Exception) {
            e.printStackTrace()
            throw e;
        }
    }

    private fun addMediaItems(exoPlayer: ExoPlayer) {
        Log.d("devhell", "addMediaItems: ${dpath?.size}")
        //adding videos from Downloads/demo-videos:-

//        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/demo-videos"
        dpath?.forEach {
            val path = it
            exoPlayer.addMediaItem(MediaItem.fromUri("$path"))
//            val directory = File(path)
//            if (directory.exists()) {
//                val files = directory.list()
//                Log.d("devhell", "addMediaItems: $files ")
//                if (files != null)
//                    for (i in files) {
//    //                    if (i.endsWith("mp4"))
//                        exoPlayer.addMediaItem(MediaItem.fromUri("$path/$i"))
//                    }
//            }
        }

        //adding videos to exoplayer playlist from assets:-
//
//        val videos = assets.list("demo-videos")
//        videos?.forEach {
//            Log.d("ajayfile", "string= ${it}")
//            if (it.endsWith("mp4"))
//                exoPlayer.addMediaItem(MediaItem.fromUri("asset:///demo-videos/$it"))
//        }
    }

    private fun releasePlayer() {
        player?.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            currentItem = exoPlayer.currentMediaItemIndex
            playWhenReady = exoPlayer.playWhenReady
//            exoPlayer.removeListener(playbackStateListener)
            exoPlayer.release()
        }
        player = null
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, videoView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun playbackStateListener() = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            val stateString: String = when (playbackState) {
                ExoPlayer.STATE_IDLE -> "ExoPlayer.STATE_IDLE      -"
                ExoPlayer.STATE_BUFFERING -> "ExoPlayer.STATE_BUFFERING -"
                ExoPlayer.STATE_READY -> "ExoPlayer.STATE_READY     -"
                ExoPlayer.STATE_ENDED -> "ExoPlayer.STATE_ENDED     -"
                else -> "UNKNOWN_STATE             -"
            }
            Log.d("Tv Video player", "changed state to $stateString")
        }
    }
}
