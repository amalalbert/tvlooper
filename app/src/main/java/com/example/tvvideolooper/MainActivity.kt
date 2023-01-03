package com.example.tvvideolooper

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
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
import com.github.angads25.filepicker.model.DialogConfigs
import com.github.angads25.filepicker.model.DialogProperties
import com.github.angads25.filepicker.view.FilePickerDialog
import java.io.File


class MainActivity : Activity() {

    private val playbackStateListener: Player.Listener = playbackStateListener()
    private var player: ExoPlayer? = null

    private var playWhenReady = true
    private var currentItem = 0
    private var playbackPosition = 0L
    private var dpath: Array<String>? = null
    private var videoView: PlayerView? = null
    val READ_STORAGE_PERMISSION_REQUEST_CODE = 41
    var properties: DialogProperties? = null
    var dialog: FilePickerDialog? = null
    val logo: ImageView? = null
    var launchBtn: Button? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        videoView = findViewById(R.id.video_view)
        launchBtn = findViewById(R.id.btn_launch)
        videoView?.visibility = View.GONE
        logo?.visibility = View.VISIBLE
        launchBtn?.setOnClickListener {
            alertBuilder()
        }
    }

    private fun alertBuilder() {
        Log.d("devhell", "entered")
        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle("Loopster")
        builder.setMessage("Videos to loop should be placed in Internal Storage/Movies.\nClick Next to Continue")

        builder.setPositiveButton("Next") { dialog, which ->
            dialog.dismiss()
            showFilePicker()
        }

        builder.setNeutralButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun alertBuilderBack() {
        Log.d("devhell", "entered")
        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle("Loopster")
        builder.setMessage("Do you really want to exit?")

        builder.setPositiveButton("Exit") { dialog, which ->
            dialog.dismiss()
            finish()
        }

        builder.setNeutralButton("cancel") { dialog, which ->
            dialog.dismiss()
        }
        builder.show()
    }

    public override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            if (!checkPermission()) {
                requestPermissionForReadExtertalStorage()
            } else {
                showFilePicker()
            }
        }
    }

    public override fun onResume() {
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

    override fun onBackPressed() {
        alertBuilderBack()
    }
    private fun showFilePicker() {
        properties = DialogProperties()
        properties?.selection_mode = DialogConfigs.MULTI_MODE
        properties?.selection_type = DialogConfigs.FILE_SELECT
        properties?.root = File("/storage/emulated/0/Movies")
        properties?.error_dir = File("/storage/emulated/0/Movies")
        properties?.offset = File(DialogConfigs.DEFAULT_DIR)
        properties?.extensions = null

        dialog = FilePickerDialog(this@MainActivity, properties)
        dialog?.setTitle("Select Media")
        dialog?.setDialogSelectionListener {
            //files is the array of the paths of files selected by the Application User.
            dpath = it.clone()
            launchBtn?.visibility = View.GONE
            videoView?.visibility = View.VISIBLE
            initializePlayer()
//            window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setNegativeBtnName("back")
        dialog?.show()
        dialog?.findViewById<TextView>(com.github.angads25.filepicker.R.id.dir_path)?.visibility =
            View.GONE
        dialog?.findViewById<Button>(com.github.angads25.filepicker.R.id.cancel)?.setOnClickListener {
            Log.d("devhell", "showFilePicker: ")
            dialog!!.dismiss()
        }
    }

    private fun initializePlayer() {

        dialog?.dismiss()
        val trackSelector = DefaultTrackSelector(this).apply {
            setParameters(buildUponParameters().setMaxVideoSizeSd())
        }
        player = ExoPlayer.Builder(this)
            .setTrackSelector(trackSelector)
            .build()
            .also { exoPlayer ->
                videoView?.player = exoPlayer

                addMediaItems(exoPlayer)

                exoPlayer.playWhenReady = playWhenReady
//                exoPlayer.seekTo(currentItem, playbackPosition)
                exoPlayer.addListener(playbackStateListener)
                exoPlayer.repeatMode = ExoPlayer.REPEAT_MODE_ALL
                exoPlayer.prepare()
            }
    }

    private fun checkPermission(): Boolean {
        val result =
            applicationContext.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
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
            throw e
        }
    }

    private fun addMediaItems(exoPlayer: ExoPlayer) {
        dpath?.forEach {
            val path = it
            exoPlayer.addMediaItem(MediaItem.fromUri(path))
        }

        //adding videos to exoplayer playlist from assets:-
//
//        val videos = assets.list("demo-videos")
//        videos?.forEach {
//            Log.d("amalFile", "string= ${it}")
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
        videoView?.let {
            WindowInsetsControllerCompat(window, it).let { controller ->
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
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
