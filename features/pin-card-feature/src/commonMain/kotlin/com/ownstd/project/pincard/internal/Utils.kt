package com.ownstd.project.pincard.internal

fun String.parseToHost(baseUrl: String): String {
    // Проверяем, содержит ли строка протокол (http:// или https://)
    val protocolEndIndex = indexOf("://")
    if (protocolEndIndex == -1) {
        // Если нет протокола, возвращаем исходную строку
        return this
    }

    // Находим начало пути (после домена и порта)
    val pathStartIndex = indexOf('/', protocolEndIndex + 3).takeIf { it != -1 } ?: return baseUrl

    // Извлекаем путь и параметры запроса (если есть)
    val pathAndQuery = substring(pathStartIndex)

    // Формируем новый URL с baseUrl
    return "$baseUrl$pathAndQuery"
}