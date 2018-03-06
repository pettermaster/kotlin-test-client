package dynamictest

enum class ResponseCode(value: Int) {
    UNKNOWN_ERROR(0),
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    CONFLICT(409)
}