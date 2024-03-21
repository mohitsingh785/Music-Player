package com.example.myapplication.Fragements

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.API.ApiService
import com.example.myapplication.Adapters.SongsAdapter
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.CustomItemAnimator
import com.example.myapplication.Model.Track
import com.example.myapplication.MusicPlayerActivity
import com.example.myapplication.R

class ForYouFrag : Fragment() {

    object RetrofitClient {
        private const val BASE_URL = "https://cms.samespace.com/"

        private val retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        val apiService: ApiService by lazy {
            retrofit.create(ApiService::class.java)
        }
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = activity?.window
            window?.statusBarColor = ContextCompat.getColor(requireContext(), android.R.color.black)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_for_you, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.songsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        lifecycleScope.launch {
            // Make the network request
            val response = RetrofitClient.apiService.fetchTracks()

            // Check if the response is successful
            if (response.isSuccessful && response.body() != null) {
                // Use the data from the response
                val tracks = response.body()!!.data // Access the list of tracks from the ApiResponse object
                // Set the custom item animator
                recyclerView.itemAnimator = CustomItemAnimator()
                // Inside onCreateView after setting up the RecyclerView and its adapter
                recyclerView.adapter = SongsAdapter(tracks) { track, imageView ->
                    // Here, you can handle track item clicks, for example, navigate to a detail view
                    val intent = Intent(context, MusicPlayerActivity::class.java).apply {
                        putExtra("trackName", track.name)
                        putExtra("artistName", track.artist)
                        putExtra("trackUrl", track.url)
                        putExtra("coverImageUrl", "https://cms.samespace.com/assets/${track.cover}")
                        putExtra("tracks", ArrayList<Track>(tracks))
                        putExtra("index", tracks.indexOf(track))
                        // You can add more extras if needed
                    }
                    // Correctly pass the ImageView for the transition
                    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        requireActivity(),
                        imageView, // This now correctly refers to the ImageView being clicked
                        ViewCompat.getTransitionName(imageView) ?: ""
                    )

                    startActivity(intent, options.toBundle())
                }
            } else {
                // Handle API error response here, for example, show an error message to the user
            }
        }


        return view
    }
}
