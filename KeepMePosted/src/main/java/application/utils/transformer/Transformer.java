package application.utils.transformer;

public interface Transformer<S, T> { // try to find a pattern for that
    T transform(S chat);
}
