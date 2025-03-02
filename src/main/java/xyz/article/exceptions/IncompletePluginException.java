package xyz.article.exceptions;

public class IncompletePluginException extends RuntimeException {
    public IncompletePluginException (String msg) {
        super(msg);
    }
}
