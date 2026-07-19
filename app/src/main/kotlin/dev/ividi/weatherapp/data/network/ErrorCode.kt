package dev.ividi.weatherapp.data.network

/** Every stable `errorCode` value the backend is documented to return. */
enum class ErrorCode(val wireValue: String) {
    CITY_NOT_FOUND("CITY_NOT_FOUND"),
    PROVIDER_UNAVAILABLE("PROVIDER_UNAVAILABLE"),
    PROVIDER_QUOTA_EXCEEDED("PROVIDER_QUOTA_EXCEEDED"),
    VALIDATION_FAILED("VALIDATION_FAILED"),
    EMAIL_ALREADY_REGISTERED("EMAIL_ALREADY_REGISTERED"),
    INVALID_CREDENTIALS("INVALID_CREDENTIALS"),
    FAVORITE_ALREADY_EXISTS("FAVORITE_ALREADY_EXISTS"),
    UNAUTHENTICATED("UNAUTHENTICATED"),
    ACCESS_DENIED("ACCESS_DENIED"),
    RATE_LIMIT_EXCEEDED("RATE_LIMIT_EXCEEDED"),
    INTERNAL_ERROR("INTERNAL_ERROR");

    companion object {
        fun fromWireValue(value: String?): ErrorCode? = entries.find { it.wireValue == value }
    }
}
