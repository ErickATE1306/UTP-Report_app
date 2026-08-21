package com.example.utpreportapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform