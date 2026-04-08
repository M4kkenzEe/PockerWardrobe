package com.ownstd.project.wardrobe.internal.domain.model

enum class SortOrder { BY_DATE, BY_NAME, BY_CATEGORY }

data class FilterOptions(
    val sortOrder: SortOrder = SortOrder.BY_DATE,
    val seasons: List<String> = emptyList(),
    val colors: List<String> = emptyList(),
) {
    val isActive: Boolean
        get() = seasons.isNotEmpty() || colors.isNotEmpty() || sortOrder != SortOrder.BY_DATE
}
