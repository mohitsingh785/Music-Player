package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide

//import com.example.myapplication.adapter.ViewPagerAdapter

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewPager: ViewPager2 = findViewById(R.id.view_pager)
        viewPager.adapter = ViewPagerAdapter(this) // Your adapter needs to be set up properly
//        val trackName = intent.getStringExtra("songTitle") ?: ""
//        val artistName = intent.getStringExtra("artistName") ?: ""
//        val coverImageUrl = intent.getStringExtra("coverImageUrl") ?: ""
//        val status_code = intent.getStringExtra("status_code") ?: ""
//
//        Log.e("CHECKKKKKKKKKKKKKKKKKKKKKKKK","DDDDDDDDDDDDDDDDDDD")
//        if(status_code.equals("200")){
//            Log.e("CHECKKKKKKKKKKKKKKKKKKKKKKKK","DDDDDDDDDDDDDDDDDDD")
//            updateMiniPlayer(trackName, artistName, coverImageUrl)
//        }
        val tabLayout: TabLayout = findViewById(R.id.tab_layout) // Make sure to have a TabLayout with this ID in your XML

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "For You"
                1 -> "Top Tracks"
                // Add more cases for more tabs
                else -> null
            }

            // Optionally, if you need even finer control or have a custom view, you might need to set the layout parameters
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            // Adjust the start and end margins to decrease the space
            val margin = 10.dpToPx(this) // Adjust the 4dp margin to your preference
            layoutParams.marginStart = margin
            layoutParams.marginEnd = margin
            tab.view.layoutParams = layoutParams
        }.attach()

// Be sure to call this function on the UI thread.
        viewPager.post {
            tabLayout.requestLayout()
        }


    }
    fun Int.dpToPx(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()




    fun updateMiniPlayer(songTitle: String?, artistName: String?, coverImageUrl: String?) {
        // Make sure to reference your mini player's views correctly
        findViewById<TextView>(R.id.float_artistName).text = songTitle ?: "Unknown Title"
        findViewById<TextView>(R.id.float_trackName).text = artistName ?: "Unknown Artist"
        Glide.with(this).load(coverImageUrl).into(findViewById<ImageView>(R.id.float_coverImage))

        // Optionally, make the mini player visible if it was hidden
        findViewById<View>(R.id.floating_player).visibility = View.VISIBLE
    }

}
