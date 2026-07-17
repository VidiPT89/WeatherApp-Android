package dev.ividi.weatherapp.data.network

/**
 * Typed exception for any failed API call, carrying the backend's user-facing `message`
 * (from [dev.ividi.weatherapp.data.model.ApiErrorBody]) whenever it was available.
 */
sealed class ApiException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    /** The backend responded with a non-2xx status and a parsed error body. */
    class HttpError(
        val statusCode: Int,
        val backendMessage: String,
        val path: String? = null,
    ) : ApiException(backendMessage)

    /** No usable connection to the backend (timeout, DNS, connection refused, etc). */
    class NetworkError(message: String, cause: Throwable? = null) : ApiException(message, cause)

    /** Anything else: malformed response, unexpected serialization failure, etc. */
    class UnknownError(message: String, cause: Throwable? = null) : ApiException(message, cause)
}
