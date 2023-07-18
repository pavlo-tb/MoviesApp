package com.petproject.moviesapp.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.petproject.moviesapp.domain.InternetConnectionException
import com.petproject.moviesapp.domain.ServerException
import retrofit2.HttpException

class ExceptionWrapper(private val context: Context) {
    //todo()
    suspend operator fun <T> invoke(block: suspend () -> T): T {
        if (!isInternetAvailable(context)) {
            throw InternetConnectionException()
        }
        return try {
            block()
        } catch (e: HttpException) {
            throw ServerException("Response status code is not OK: ${e.message}")
        } catch (e: Exception) {
            throw Exception()
        }
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            else -> false
        }
    }

}