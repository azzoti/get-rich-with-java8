package etf.analyzer;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Partial implementation of equivalent of C# double? which allows chaining of arithmentic operations on potentially empty/(aka null) values.
 *
 */
public class OptionalDouble implements Comparable<OptionalDouble>  
{
	private final Optional<Double> self;

	private OptionalDouble(Optional<Double> value) {
		self = value;
	}
	
	interface DoubleOperator {
		Double apply(Double lValue, Double rValue);
	}

	public Optional<Double> apply(OptionalDouble other, DoubleOperator f) {
		return self.flatMap(x -> other.self.map(y -> f.apply(x, y)));
	}
	
	public OptionalDouble add(OptionalDouble other) {
		return new OptionalDouble(apply(other, (x,y) -> x + y));
	}

	public OptionalDouble divide(OptionalDouble other) {
		return new OptionalDouble(apply(other, (x,y) -> x / y));
	}
	
	public Double get() {
		return self.get();
	}

	public boolean isPresent() {
		return self.isPresent();
	}

	public void ifPresent(Consumer<? super Double> consumer) {
		self.ifPresent(consumer);
	}

	public Optional<Double> filter(Predicate<? super Double> predicate) {
		return self.filter(predicate);
	}

	public <U> Optional<U> map(Function<? super Double, ? extends U> mapper) {
		return self.map(mapper);
	}

	public <U> Optional<U> flatMap(
			Function<? super Double, Optional<U>> mapper) {
		return self.flatMap(mapper);
	}

	public Double orElse(Double other) {
		return self.orElse(other);
	}

	public Double orElseGet(Supplier<? extends Double> other) {
		return self.orElseGet(other);
	}

	public <X extends Throwable> Double orElseThrow(
			Supplier<? extends X> exceptionSupplier) throws X {
		return self.orElseThrow(exceptionSupplier);
	}

	public boolean equals(Object obj) {
		return self.equals(obj);
	}

	public int hashCode() {
		return self.hashCode();
	}

	public static OptionalDouble ofNullable(Double value) {
		return new OptionalDouble(Optional.ofNullable(value));
	}

	public static OptionalDouble of(Double value) {
		return new OptionalDouble((Optional.of(value)));
	}

	
	public static OptionalDouble empty() {
		return new OptionalDouble(Optional.empty());
	}

    @Override
    public String toString() {
        return self.toString();
    }

    @Override
    public int compareTo(OptionalDouble other) {
        Optional<Integer> oi = self.flatMap(x -> other.self.map(y -> x.compareTo(y)));
        // throws an NPE if either self or other is empty which is fine 
        return oi.get();
    }



}