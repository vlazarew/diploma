package application.utils.transformer;

public interface Transformer<From, To> {
    To transform(From chat);
}
