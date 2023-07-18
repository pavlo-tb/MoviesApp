package com.petproject.moviesapp.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.petproject.moviesapp.R
import com.petproject.moviesapp.data.repository.RepositoryImpl
import com.petproject.moviesapp.domain.InternetConnectionException
import com.petproject.moviesapp.domain.ServerException
import com.petproject.moviesapp.domain.entities.Movie
import com.petproject.moviesapp.domain.usecases.GetMoviesUseCase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MoviesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = RepositoryImpl.getInstance(application)
    private val getMoviesUseCase = GetMoviesUseCase(repository)
    private val movieList = mutableListOf<Movie>()
    private val _currentMovieList = MutableLiveData<List<Movie>>()
    val currentMovieList: LiveData<List<Movie>> get() = _currentMovieList
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading
    private val _errorMessageRes = MutableLiveData<Int>()
    val errorMessageRes: LiveData<Int> get() = _errorMessageRes

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _isLoading.value = false
        _errorMessageRes.value =
            when (throwable) {
                is InternetConnectionException -> R.string.error_no_internet
                is ServerException -> R.string.error_bad_response
                else -> R.string.error_general
            }
    }

    init {
        loadMovies()
    }

    fun loadMovies() {
        if (isLoading.value == true) {
            return
        }
        viewModelScope.launch(exceptionHandler) {
            _isLoading.value = true
            movieList.addAll(
                withContext(Dispatchers.IO) {
                    getMoviesUseCase()
                }
            )
            _currentMovieList.value = movieList
            _isLoading.value = false
        }

    }
}