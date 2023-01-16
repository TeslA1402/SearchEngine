package searchengine.exception;

public record ErrorDescription(boolean result, String error) {
    public ErrorDescription(String error) {
        this(false, error);
    }

    public ErrorDescription(CustomException e) {
        this(e.getError());
    }
}
