package com.hostelmaintenance.data.local

interface TokenStore {
    fun read(): String?
    fun save(token: String)
    fun clear()
}
