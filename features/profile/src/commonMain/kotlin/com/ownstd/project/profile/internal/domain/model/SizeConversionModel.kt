package com.ownstd.project.profile.internal.domain.model

data class SizeRowModel(
    val label: String,
    val ru: String,
    val eu: String,
    val us: String,
    val jp: String,
    val kr: String,
    val cn: String,
)

val CLOTHING_SIZES = listOf(
    SizeRowModel("Верх",  ru = "44",   eu = "S",  us = "XS/S", jp = "S",  kr = "85",  cn = "165/88A"),
    SizeRowModel("Низ",   ru = "44",   eu = "42", us = "8",    jp = "M",  kr = "28",  cn = "170/74A"),
    SizeRowModel("Обувь", ru = "37.5", eu = "38", us = "7.5",  jp = "24", kr = "240", cn = "240"),
)
