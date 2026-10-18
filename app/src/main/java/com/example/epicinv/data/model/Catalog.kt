package com.example.epicinv.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CatalogItemsResponse(
    @SerialName("records") val records: List<CatalogRecord> = emptyList()
)

@Serializable
data class CatalogRecord(
    @SerialName("id") val id: String,
    @SerialName("namespace") val namespace: String,
    @SerialName("title") val title: String = "",
    @SerialName("description") val description: String = "",
    @SerialName("keyImages") val keyImages: List<KeyImage> = emptyList(),
    @SerialName("categories") val categories: List<Category> = emptyList(),
    @SerialName("price") val price: Price? = null
)

@Serializable
data class KeyImage(
    @SerialName("type") val type: String = "",
    @SerialName("url") val url: String = ""
)

@Serializable
data class Category(
    @SerialName("path") val path: String = ""
)

@Serializable
data class Price(
    @SerialName("totalPrice") val totalPrice: PriceValue? = null,
    @SerialName("discountPrice") val discountPrice: PriceValue? = null
)

@Serializable
data class PriceValue(
    @SerialName("fmtPrice") val formatted: PriceFmt? = null,
    @SerialName("originalPrice") val originalPrice: Int = 0
)

@Serializable
data class PriceFmt(
    @SerialName("originalPrice") val original: String = "",
    @SerialName("discountPrice") val discount: String = ""
)
