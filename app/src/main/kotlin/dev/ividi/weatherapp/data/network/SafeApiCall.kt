package dev.ividi.weatherapp.data.network

import dev.ividi.weatherapp.data.model.ApiErrorBody
import java.io.IOException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException

private const val UNKNOWN_ERROR_MESSAGE = "Something went wrong. Please try again."
private const val NETWORK_ERROR_MESSAGE = "Could not reach the server. Check your connection and try again."

/**
 * Runs a suspend Retrofit call and converts any failure into a typed [ApiException],
 * extracting the backend's `message` field from the error body when present.
 */
suspend fun <T> safeApiCall(json: Json, block: suspend () -> T): T {
    return try {
        block()
    } catch (httpException: HttpException) {
        throw httpException.toApiException(json)
    } catch (ioException: IOException) {
        throw ApiException.NetworkError(NETWORK_ERROR_MESSAGE, ioException)
    } catch (serializationException: SerializationException) {
        throw ApiException.UnknownError(UNKNOWN_ERROR_MESSAGE, serializationException)
    }
}

private fun HttpException.toApiException(json: Json): ApiException.HttpError {
    val rawBody = response()?.errorBody()?.string()
    val parsedBody = rawBody?.let { body ->
        runCatching { json.decodeFromString(ApiErrorBody.serializer(), body) }.getOrNull()
    }
    val backendMessage = parsedBody?.message?.takeIf { it.isNotBlank() } ?: message() ?: UNKNOWN_ERROR_MESSAGE
    return ApiException.HttpError(
        statusCode = code(),
        backendMessage = backendMessage,
        path = parsedBody?.path,
    )
}
