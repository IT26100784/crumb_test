# Common Module (`com.crumb.common`)

## Overview
The `common` package provides foundational services, cross-cutting configurations, exception management, authentication utilities, and uniform API response envelopes across the Crumb bakery platform.

## Classes & Structure
- `config/AppConfig.java`: Spring configuration registering shared singleton beans including `PasswordEncoder` (BCrypt).
- `dto/ApiResponse.java`: Generic response envelope wrapping all JSON payloads (`success`, `message`, `data`, `timestamp`).
- `exception/ErrorResponse.java`: Structured error payload returned for REST exceptions.
- `exception/ResourceNotFoundException.java`: HTTP 404 exception for missing records.
- `exception/BadRequestException.java`: HTTP 400 exception for business rule violations.
- `exception/UnauthorizedException.java`: HTTP 401 exception for missing authentication.
- `exception/ForbiddenException.java`: HTTP 403 exception for insufficient privileges.
- `exception/GlobalExceptionHandler.java`: Controller advice mapping exceptions and bean validation failures to standard HTTP responses.
- `auth/SessionAuthHelper.java`: Session management helper managing user logins, admin checks, and role enforcement.

## OOP Principles Demonstrated
- **Encapsulation**: Private fields across DTOs and configuration models with controlled accessors via Lombok.
- **Abstraction**: High-level error handling hiding runtime stack details from end-users; centralized session management abstracting direct servlet session calls.
