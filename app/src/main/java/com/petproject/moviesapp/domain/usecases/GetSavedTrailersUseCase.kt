package com.petproject.moviesapp.domain.usecases

import com.petproject.moviesapp.domain.repository.Repository

class GetSavedTrailersUseCase(private val repository: Repository) {
    suspend operator fun invoke(movieId: Int) = repository.getSavedTrailers(movieId)
}