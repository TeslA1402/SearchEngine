package searchengine.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
abstract class CustomException extends RuntimeException {
    private final String error;
}
