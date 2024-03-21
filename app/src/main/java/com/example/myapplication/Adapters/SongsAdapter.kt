package com.example.myapplication.Adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.Model.Track
//import com.example.myapplication.MusicPlayerActivity
import com.example.myapplication.R


class SongsAdapter(private val songs: List<Track>, private val onItemClick: (Track, ImageView) -> Unit) : RecyclerView.Adapter<SongsAdapter.SongViewHolder>() {

    // Tracks the last position so we only animate items once
    private var lastPosition = -1

    class SongViewHolder(view: View, private val onItemClick: (Track, ImageView) -> Unit) : RecyclerView.ViewHolder(view) {
        fun bind(song: Track) {
            val songNameTextView = itemView.findViewById<TextView>(R.id.songName)
            val artistNameTextView = itemView.findViewById<TextView>(R.id.artistName)
            val coverImageView = itemView.findViewById<ImageView>(R.id.coverImage)

            songNameTextView.text = song.name
            artistNameTextView.text = song.artist

            // Construct the full URL for the cover image
            val coverImageUrl = "https://cms.samespace.com/assets/${song.cover}"




            // Use Glide to load the image from the constructed URL
            Glide.with(itemView.context)
                .load(coverImageUrl) // Use the full URL here
                .placeholder(R.drawable.img) // Optional: Placeholder image
                .error(androidx.constraintlayout.widget.R.drawable.abc_btn_check_to_on_mtrl_000) // Optional: Error image
                .into(coverImageView)

            // Set click listener
            itemView.setOnClickListener {
                onItemClick(song,coverImageView)
            }


        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.song_item, parent, false)
        return SongViewHolder(view, onItemClick) // Pass onItemClick to the constructor
    }


    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {

        holder.bind(songs[position])
        // Here we add the animation
        if (position > lastPosition) {
            holder.itemView.translationY = 100f // Start slightly off-screen
            holder.itemView.alpha = 0f
            holder.itemView.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(300)
                .setStartDelay(position * 100L) // Delay so each animation starts one after the other
                .start()

            lastPosition = position
        }

    }

    override fun getItemCount() = songs.size
}
