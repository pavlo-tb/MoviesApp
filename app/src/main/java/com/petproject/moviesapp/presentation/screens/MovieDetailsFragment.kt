package com.petproject.moviesapp.presentation.screens

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.bumptech.glide.Glide
import com.petproject.moviesapp.R
import com.petproject.moviesapp.databinding.FragmentMovieDetailsBinding
import com.petproject.moviesapp.domain.MovieDetailsMode
import com.petproject.moviesapp.domain.entities.Movie
import com.petproject.moviesapp.presentation.adapters.review.ReviewAdapter
import com.petproject.moviesapp.presentation.adapters.trailer.TrailerAdapter
import com.petproject.moviesapp.presentation.viewmodels.MovieDetailsViewModel
import com.petproject.moviesapp.presentation.viewmodels.MovieDetailsViewModelFactory

class MovieDetailsFragment : Fragment() {
    private lateinit var movie: Movie
    private lateinit var mode: MovieDetailsMode

    private var _binding: FragmentMovieDetailsBinding? = null
    private val binding get() = _binding ?: throw NullPointerException("binding is null")

    private val onTrailerClickListener: (String) -> Unit = { url ->
        val intent = Intent(Intent.ACTION_VIEW).apply { data = url.toUri() }
        startActivity(intent)
    }

    private val trailerAdapter = TrailerAdapter(onTrailerClickListener)
    private val reviewAdapter = ReviewAdapter()

    private val viewModel by lazy {
        ViewModelProvider(
            this,
            MovieDetailsViewModelFactory(requireActivity().application, movie, mode)
        )[MovieDetailsViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val parsedArgs = parseArgs()
        movie = parsedArgs.first
        mode = parsedArgs.second
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setMovieImage()
        binding.textViewName.text = movie.name
        binding.textViewDescription.text = movie.description
        binding.textViewYear.text = movie.year.toString()
        setOnStarClickListener()
        setObservers()
        setUpReviewAdapter()
        setUpTrailerAdapter()
    }

    private fun setObservers() {
        viewModel.errorMessageRes.observe(viewLifecycleOwner) {
            binding.textViewErrorMessage.text = getString(it)
            setVisibilityOnError()
        }
        viewModel.isFavorite.observe(viewLifecycleOwner) {
            binding.imageViewStar.setImageResource(if (it) IMAGE_STAR_ON else IMAGE_STAR_OFF)
        }
        viewModel.reviews.observe(viewLifecycleOwner) {
            binding.textViewErrorReviews.visibility = View.GONE
            reviewAdapter.submitList(it)
        }
        viewModel.trailers.observe(viewLifecycleOwner) {
            binding.textViewErrorTrailers.visibility = View.GONE
            trailerAdapter.submitList(it)
        }
    }

    private fun setVisibilityOnError() {
        binding.imageViewStar.visibility = View.GONE
        binding.recyclerViewReviews.visibility = View.GONE
        binding.recyclerViewTrailers.visibility = View.GONE
        binding.textViewErrorMessage.visibility = View.VISIBLE
        binding.textViewHint.visibility = View.GONE
    }

    private fun setOnStarClickListener() {
        binding.imageViewStar.setOnClickListener {
            viewModel.onStarPressed()
        }
    }

    private fun setUpTrailerAdapter() {
        binding.recyclerViewTrailers.adapter = trailerAdapter

    }

    private fun setUpReviewAdapter() {
        with(binding.recyclerViewReviews) {
            PagerSnapHelper().attachToRecyclerView(this)
            adapter = reviewAdapter
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
        }
    }

    private fun setMovieImage() {
        Glide.with(requireContext())
            .load(movie.posterUrl)
            .error(R.drawable.image_error_placeholder)
            .into(binding.imageViewMovieImage)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun parseArgs(): Pair<Movie, MovieDetailsMode> {
        val args = requireArguments()
        val movie: Movie?
        val mode: MovieDetailsMode?
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            @Suppress("DEPRECATION")
            movie = args.getParcelable(MOVIE_KEY)
            @Suppress("DEPRECATION")
            mode = args.getParcelable(MODE_KEY)
        } else {
            movie = args.getParcelable(MOVIE_KEY, Movie::class.java)
            mode = args.getParcelable(MODE_KEY, MovieDetailsMode::class.java)
        }
        if (movie == null) {
            throw IllegalArgumentException("no 'movie' argument is given ")
        }
        if (mode == null) {
            throw IllegalArgumentException("no 'mode' argument is given ")
        }
        return Pair(movie, mode)
    }

    companion object {
        fun newInstance(movie: Movie, mode: MovieDetailsMode) = MovieDetailsFragment().apply {
            arguments = Bundle().apply {
                putParcelable(MOVIE_KEY, movie)
                putParcelable(MODE_KEY, mode)
            }
        }

        private const val MODE_KEY = "mode"
        private const val IMAGE_STAR_ON = android.R.drawable.star_big_on
        private const val IMAGE_STAR_OFF = android.R.drawable.star_big_off
        private const val MOVIE_KEY = "movie"
    }
}