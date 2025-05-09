package com.example.deliveryapp.Models

import com.google.gson.annotations.SerializedName

data class UserProfileModel(
    @SerializedName("id") val id: Int,
    @SerializedName("email") val email: String,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("is_superuser") val isSuperuser: Boolean,
    @SerializedName("is_verified") val isVerified: Boolean,
    @SerializedName("name") val name: String? = null,
    @SerializedName("phone") val phone: String? = null
) 