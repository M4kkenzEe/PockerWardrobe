package com.ownstd.project.network.api

/**
 * Central configuration for server connection.
 *
 * IMPORTANT: When your development server IP changes, update SERVER_HOST here.
 * This change will automatically apply to:
 * - All API calls (NetworkModule)
 * - Image URL replacements (Utils.kt)
 */
object ServerConfig {
    /**
     * Server IP address and port.
     * Change this value when your development server IP changes.
     *
     * Examples:
     * - Local network: "192.168.0.57:8080"
     * - Localhost: "localhost:8080"
     * - Production: "api.example.com"
     */
    const val SERVER_HOST = "194.87.190.248:80"

    /**
     * Base URL for API calls (includes /api/v1/ path)
     */
    const val BASE_URL = "http://$SERVER_HOST/api/v1/"
}
