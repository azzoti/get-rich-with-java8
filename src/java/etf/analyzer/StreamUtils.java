package etf.analyzer;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class StreamUtils {

  public static <T> Optional<T> findFirst(Stream<T> stream, Predicate<T> f) {

    Iterator<T> iterator = stream.iterator();
    while (iterator.hasNext()) {
      T d = iterator.next();
      if (f.test(d)) {
        return Optional.of(d);
      }
    }
    return Optional.empty();
  }

  public static <T> Optional<T> findFirstUsingExceptionReturn(Stream<T> stream, Predicate<T> f) {

    final class ValueHolderException extends RuntimeException {

      private static final long serialVersionUID = 1L;

      private final T value;

      public ValueHolderException(T value) {
        super();
        this.value = value;
      }

      @Override
      public synchronized Throwable fillInStackTrace() {
        return this;
      }
    }

    try {
      stream.iterator().forEachRemaining(d -> {
        if (f.test(d))
          throw new ValueHolderException(d);
      });
    } catch (ValueHolderException e) {
      return Optional.of(e.value);
    }
    return Optional.empty();
  }

}
