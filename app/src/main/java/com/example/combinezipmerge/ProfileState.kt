package com.example.combinezipmerge

import android.accounts.AuthenticatorDescription

data class ProfileState(
    val profilePicUrl: String? = null,
    val username: String? = null,
    val description: String? = null,
    val posts: List<Post> = emptyList(),
)
