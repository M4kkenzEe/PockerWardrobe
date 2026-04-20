package com.ownstd.project.wardrobe.internal.domain.model

enum class SortOrderModel { BY_DATE, BY_NAME, BY_CATEGORY }

data class FilterOptionsModel(
    val sortOrder: SortOrderModel = SortOrderModel.BY_DATE,
    val seasons: List<String> = emptyList(),
    val colors: List<String> = emptyList(),
) {
    val isActive: Boolean
        get() = seasons.isNotEmpty() || colors.isNotEmpty() || sortOrder != SortOrderModel.BY_DATE
}
