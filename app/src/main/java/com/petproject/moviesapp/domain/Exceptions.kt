package com.petproject.moviesapp.domain

class InternetConnectionException: RuntimeException()

class ServerException(s: String) : RuntimeException()