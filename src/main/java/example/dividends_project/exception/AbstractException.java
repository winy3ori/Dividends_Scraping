package example.dividends_project.exception;

public abstract class AbstractException extends RuntimeException{

    abstract public int getStatusCode();
    abstract public String getMessage();
}
