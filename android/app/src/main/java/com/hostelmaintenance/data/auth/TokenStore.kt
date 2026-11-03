package com.hostelmaintenance.data.auth

interface TokenStore {
    fun read(): String?
    fun save(token: String)
    fun clear()
}
