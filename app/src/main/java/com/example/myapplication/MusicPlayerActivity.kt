package com.example.myapplication
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.audiofx.BassBoost
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.transition.TransitionInflater
import android.util.Log
import android.view.GestureDetector
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.myapplication.Model.Track
import kotlinx.coroutines.SupervisorJob
import kotlin.math.max
import kotlin.math.roundToInt

class MusicPlayerActivity : AppCompatActivity() {
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var buttonPlayPause: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var textViewTrackName: TextView
    private lateinit var textViewArtist: TextView
    private lateinit var imageViewCover: ImageView
    private lateinit var tracks: ArrayList<Track>
    private var currentIndex: Int = 0
    private var isMediaPlayerPrepared = false
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var gestureDetector: GestureDetector
    private lateinit var progressBarCoverImage: ProgressBar
    private lateinit var textViewCurrentTime: TextView
    private lateinit var textViewTotalTime: TextView

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        // Let the GestureDetector try to process the event
        if (ev != null) {
            gestureDetector.onTouchEvent(ev)
        }
        return super.dispatchTouchEvent(ev)
    }
    // Adjust MIN_SWIPE_DISTANCE and MIN_SWIPE_VELOCITY according to your needs
    companion object {
        const val MIN_SWIPE_DISTANCE = 120
        const val MIN_SWIPE_VELOCITY = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_player)
        // Change the status bar color programmatically
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.BLACK
        }
        // Initialize views
        textViewTrackName = findViewById(R.id.textViewTrackName)
        textViewArtist = findViewById(R.id.textViewArtist)
        imageViewCover = findViewById(R.id.imageViewCover)
        progressBar = findViewById(R.id.simpleProgressBar)
        buttonPlayPause = findViewById(R.id.buttonPlayPause)
        progressBarCoverImage = findViewById(R.id.progressBarCoverImage)
        textViewCurrentTime = findViewById(R.id.textViewCurrentTime)
        textViewTotalTime = findViewById(R.id.textViewTotalTime)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            with(window) {
                sharedElementEnterTransition = TransitionInflater.from(this@MusicPlayerActivity).inflateTransition(R.transition.change_image_trans)
                sharedElementReturnTransition = TransitionInflater.from(this@MusicPlayerActivity).inflateTransition(R.transition.change_image_trans
                )
            }
        }



        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                event1: MotionEvent,
                event2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                // Your swipe detection logic...
                val diffX = event2.x - event1.x
                if (Math.abs(diffX) > MIN_SWIPE_DISTANCE && Math.abs(velocityX) > MIN_SWIPE_VELOCITY) {
                    if (diffX > 0) {
                        // Swipe right
                        prevTrack()
                    } else {
                        // Swipe left
                        nextTrack()
                    }
                    return true // Swipe was handled
                }
                return false // Swipe not handled, let others try
            }
        })
        // Retrieve data from intent
        val trackName = intent.getStringExtra("trackName") ?: ""
        val artistName = intent.getStringExtra("artistName") ?: ""
        val trackUrl = intent.getStringExtra("trackUrl") ?: ""
        val coverImageUrl = intent.getStringExtra("coverImageUrl") ?: ""
        tracks = intent.getParcelableArrayListExtra<Track>("tracks") ?: ArrayList()
        currentIndex = intent.getIntExtra("index", 0)

        // Set initial UI
        textViewTrackName.text = trackName
        textViewArtist.text = artistName
        Glide.with(this)
            .asBitmap()
            .load(coverImageUrl).apply(RequestOptions().transform(RoundedCorners(30)))
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    imageViewCover.setImageBitmap(resource)
                    extractColors(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Handle if needed
                }
            })

        imageViewCover.drawable?.let { drawable ->
            val bitmap = (drawable as BitmapDrawable).bitmap
            Palette.from(bitmap).generate { palette ->
                // Use the palette to extract the vibrant color
                val vibrantColor = palette?.vibrantSwatch?.rgb
                    ?: ContextCompat.getColor(this, android.R.color.black) // Fallback color
                // Now set the background gradient
                setActivityBackgroundGradient(vibrantColor, ContextCompat.getColor(this, android.R.color.black))
            }
        }

        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(trackUrl)

            // Show the ProgressBar when preparing the MediaPlayer
            progressBarCoverImage.visibility = View.VISIBLE

            prepareAsync()

            setOnPreparedListener {
                isMediaPlayerPrepared = true
                buttonPlayPause.isEnabled = true
                progressBar.max = mediaPlayer.duration
                textViewTotalTime.text = formatTime(mediaPlayer.duration)

                mediaPlayer.start() // Start playing the track once it's prepared
                updateProgressBar()
                buttonPlayPause.setImageResource(R.drawable.pause)

                // Hide the ProgressBar once the song is ready
                progressBarCoverImage.visibility = View.GONE
            }

            setOnBufferingUpdateListener { mp, percent ->
                if (percent < 100) {
                    // Show buffering progress or indicate buffering state
                    progressBarCoverImage.visibility = View.VISIBLE
                } else {
                    // Hide the ProgressBar when buffering is complete
                    progressBarCoverImage.visibility = View.GONE
                }
            }

            setOnErrorListener { _, _, _ ->
                // Hide the ProgressBar if there is an error
                progressBarCoverImage.visibility = View.GONE
                true
            }
        }


        // Play/Pause Button
        buttonPlayPause.setOnClickListener {
            if (isMediaPlayerPrepared) {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()

                    buttonPlayPause.setImageResource(R.drawable.play)
                    // Since music is paused, stop updating the progress bar
                    handler.removeCallbacksAndMessages(null)
                } else {
                    mediaPlayer.start()
                    buttonPlayPause.setImageResource(R.drawable.pause)

                    // Music is playing, start updating the progress bar
                    updateProgressBar()
                }
            }
        }


        // Next Button
        findViewById<ImageButton>(R.id.buttonNext).setOnClickListener {
            nextTrack()
        }

        // Previous Button
        findViewById<ImageButton>(R.id.buttonPrev).setOnClickListener {
            prevTrack()
        }


                val rootView = findViewById<View>(R.id.rootView)
        rootView.setOnTouchListener(object : View.OnTouchListener {
            private var initialTouchY = 0f
            private var initialY = 0f

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialY = v?.y ?: 0f
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        // Calculate the distance moved
                        val movedY = event.rawY - initialTouchY
                        // Move the view
                        v?.translationY = initialY + movedY
                        // Optionally, scale the view based on the distance moved
                        val scale = 1 - Math.min(Math.max(movedY / v!!.height, 0f), 0.9f)
                        v?.scaleX = scale
                        v?.scaleY = scale
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        // Finalize the position and transition or reset based on the condition
                        if ((v?.translationY ?: 0f) > (v?.height?.times(0.5f) ?: 0f)) {

                            val currentTrack = tracks[currentIndex]

                            // Prepare intent with song details to send to MainActivity
                            val resultIntent = Intent().apply {
                                putExtra("songTitle", currentTrack.name)
                                putExtra("artistName", currentTrack.artist)
                                putExtra("coverImageUrl", "https://cms.samespace.com/assets/${currentTrack.cover}")
                                putExtra("status_code", "200")
                            }
                            // Set result and finish with transition
                            setResult(RESULT_OK, resultIntent)
                            finishAfterTransition() // Or use custom transition logic
                        } else {
                            // Reset the view position if not dragged enough
                            v?.animate()?.translationY(initialY)?.scaleX(1f)?.scaleY(1f)?.setDuration(200)?.start()
                        }
                        return true
                    }
                }
                return false
            }
        })
    }
    // Method to format time from milliseconds to MM:ss format
    private fun formatTime(milliseconds: Int): String {
        val minutes = milliseconds / 1000 / 60
        val seconds = milliseconds / 1000 % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    fun extractColors(bitmap: Bitmap) {
        Palette.from(bitmap).generate { palette ->
            // Attempting to use a more reliable color extraction strategy
            val vibrantColor = palette?.vibrantSwatch?.rgb
                ?: palette?.dominantSwatch?.rgb
                ?: ContextCompat.getColor(this, android.R.color.black) // Fallback color

            setActivityBackgroundGradient(vibrantColor, ContextCompat.getColor(this, android.R.color.black))
        }
    }

    fun setActivityBackgroundGradient(fromColor: Int, toColor: Int) {
        // Darken the 'fromColor' to start the gradient with a darker version
        val darkerFromColor = darkenColor(fromColor, 0.8f) // Adjust the factor to get the desired darkness

        val gradient = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(darkerFromColor, toColor)
        ).apply {
            cornerRadius = 0f
        }

        val rootView = findViewById<View>(R.id.rootView) // Adjust this ID to match your layout's root view
        rootView.background = gradient
    }

    /**
     * Darkens a given color by a specified factor.
     *
     * @param color The original color to be darkened.
     * @param factor The factor by which to darken the color (0..1), where 1 means no change.
     * @return The darkened color.
     */
    fun darkenColor(color: Int, factor: Float = 0.5f): Int { // Adjusted factor to 0.5f
        val a = Color.alpha(color)
        val r = (Color.red(color) * factor).roundToInt()
        val g = (Color.green(color) * factor).roundToInt()
        val b = (Color.blue(color) * factor).roundToInt()
        return Color.argb(a, max(r, 0), max(g, 0), max(b, 0))
    }




    private fun nextTrack() {
        currentIndex = (currentIndex + 1) % tracks.size
        if (currentIndex == tracks.size) {
            currentIndex = 0 // Set current index to 0 if it equals the size of the track list
        }
//        provideHapticFeedback()

        playTrackAtIndex(currentIndex)

    }

    private fun prevTrack() {
        currentIndex = (currentIndex - 1 + tracks.size) % tracks.size
        if (currentIndex == -1) {
            currentIndex = tracks.size-1 // Set current index to 0 if it equals the size of the track list
        }
//        provideHapticFeedback()

        playTrackAtIndex(currentIndex)
    }

    private fun playTrackAtIndex(index: Int) {
        val track = tracks[index]
        mediaPlayer.reset()
        mediaPlayer.setDataSource(track.url)
        mediaPlayer.prepareAsync()

        textViewTrackName.text = track.name
        textViewArtist.text = track.artist

        Glide.with(this)
            .asBitmap()
            .load("https://cms.samespace.com/assets/${track.cover}")
            .apply(RequestOptions().transform(RoundedCorners(30)))
            .transition(BitmapTransitionOptions.withCrossFade(500))
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    imageViewCover.setImageBitmap(resource)
                    extractColors(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Handle if needed
                }
            })

        imageViewCover.drawable?.let { drawable ->
            val bitmap = (drawable as BitmapDrawable).bitmap
            Palette.from(bitmap).generate { palette ->
                // Use the palette to extract the vibrant color
                val vibrantColor = palette?.vibrantSwatch?.rgb
                    ?: ContextCompat.getColor(this, android.R.color.black) // Fallback color
                // Now set the background gradient
                setActivityBackgroundGradient(vibrantColor, ContextCompat.getColor(this, android.R.color.black))
            }
        }
    }

    // Method to update progress bar and current time text view
    private fun updateProgressBar() {
        if (isMediaPlayerPrepared) {
            progressBar.progress = mediaPlayer.currentPosition
            textViewCurrentTime.text = formatTime(mediaPlayer.currentPosition)
            handler.postDelayed({
                updateProgressBar()
            }, 1000)
        }
    }
    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null) // This stops the handler from updating the progress bar
    }
    override fun onResume() {
        super.onResume()
        if (isMediaPlayerPrepared && mediaPlayer.isPlaying) {
            updateProgressBar()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }

//    override fun onDown(event: MotionEvent): Boolean {
//        return true
//    }
    private fun changeTrackWithAnimation(nextTrack: Boolean) {
        imageViewCover.animate()
            .alpha(0f)
            .setDuration(300)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (nextTrack) {
                        nextTrack()
                    } else {
                        prevTrack()
                    }
                    imageViewCover.animate().alpha(1f).setDuration(300).setListener(null)
                }
            })
    }


//    override fun onFling(
//        event1: MotionEvent,
//        event2: MotionEvent,
//        velocityX: Float,
//        velocityY: Float
//    ): Boolean {
//        val diffX = event2.x - event1.x
//        val diffY = event2.y - event1.y
//        val MIN_DISTANCE = 120 // Consider adjusting these values
//        val MIN_VELOCITY = 200 // Consider adjusting these values
//
//        if (Math.abs(diffX) > Math.abs(diffY) && Math.abs(diffX) > MIN_DISTANCE && Math.abs(velocityX) > MIN_VELOCITY) {
//            if (diffX < 0) {
//                // Swipe left - Next track
//
//
//                changeTrackWithAnimation(true)
//            } else {
//                // Swipe right - Previous track
//
//
//                changeTrackWithAnimation(false)
//            }
//            return true
//        }
//        return false
//    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

//    // Unused GestureDetector methods
//    override fun onShowPress(event: MotionEvent) {}
//    override fun onSingleTapUp(event: MotionEvent): Boolean { return false }
//    override fun onScroll(event1: MotionEvent, event2: MotionEvent, distanceX: Float,
//                          distanceY: Float): Boolean { return false }
//    override fun onLongPress(event: MotionEvent) {}


    override fun onBackPressed() {
        // Your custom logic here
        finishAfterTransition()
        finish()
    }




}
