package etf.analyzer;

import static java.lang.System.out;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

class Event {
	public Event(LocalDate date, double price) {
		this.date = date;
		this.price = price;
	}
	public LocalDate getDate() {
		return date;
	}
	public double getPrice() {
		return price;
	}
	private LocalDate date;
	private double price;
}

class Summary {
	public Summary(String ticker, String name, String assetClass,
			String assetSubClass, OptionalDouble weekly,
			OptionalDouble fourWeeks, OptionalDouble threeMonths,
			OptionalDouble sixMonths, OptionalDouble oneYear,
			OptionalDouble stdDev, double price, OptionalDouble mav200) {
		this.ticker = ticker;
		this.name = name;
		this.assetClass = assetClass;
		// this.assetSubClass = assetSubClass;
		// Abracadabra ...
		this.lrs = fourWeeks.add(threeMonths).add(sixMonths).add(oneYear).divide(OptionalDouble.of(4.0d));
		this.weekly = weekly;
		this.fourWeeks = fourWeeks;
		this.threeMonths = threeMonths;
		this.sixMonths = sixMonths;
		this.oneYear = oneYear;
		this.stdDev = stdDev;
		this.mav200 = mav200;
		this.price = price;
	}
	private String ticker;
	private String name;
	private String assetClass;
	// private String assetSubClass;
	public OptionalDouble lrs;
	private OptionalDouble weekly;
	private OptionalDouble fourWeeks;
	private OptionalDouble threeMonths;
	private OptionalDouble sixMonths;
	private OptionalDouble oneYear;
	private OptionalDouble stdDev;
	private OptionalDouble mav200;
	private double price;
	public long time;

	static void banner() {
		out.printf("%-6s", "Ticker");
		out.printf("%-50s", "Name");
		out.printf("%-12s", "Asset Class");
		out.printf("%4s", "RS");
		out.printf("%4s", "1Wk");
		out.printf("%4s", "4Wk");
		out.printf("%4s", "3Ms");
		out.printf("%4s", "6Ms");
		out.printf("%4s", "1Yr");
		out.printf("%6s", "Vol");
		out.printf("%2s\n", "Mv");
	}

	void print() {
		out.printf("%-6s", ticker);
		out.printf("%-50s", name);
		out.printf("%-12s", assetClass);
		out.printf("%4.0f", lrs.orElse(0.0d) * 100);
		out.printf("%4.0f", weekly.orElse(0.0d) * 100);
		out.printf("%4.0f", fourWeeks.orElse(0.0d) * 100);
		out.printf("%4.0f", threeMonths.orElse(0.0d) * 100);
		out.printf("%4.0f", sixMonths.orElse(0.0d) * 100);
		out.printf("%4.0f", oneYear.orElse(0.0d) * 100);
		out.printf("%6.0f", stdDev.orElse(0.0d) * 100);
		if (price <= mav200.orElse(-Double.MAX_VALUE))
			out.printf("%2s\n", "X");
		else
			out.println();
	}
}

class TimeSeries {
    private String ticker;
    private LocalDate _start;
    private Map<LocalDate, Double> _adjDictionary;
    private String _name;
    private String _assetClass;
    private String _assetSubClass;

    public TimeSeries(String ticker, String name, String assetClass, String assetSubClass, List<Event> events) {
		super();
		this.ticker = ticker;
		this._name = name;
		this._assetClass = assetClass;
		this._assetSubClass = assetSubClass;
		this._adjDictionary = events.stream().collect(toMap(Event::getDate, Event::getPrice));
		this._start = events.get(events.size() - 1).getDate();
	}

	private static final class FindPriceAndShift {
		public FindPriceAndShift(boolean found, double aPrice, int shift) {
		    this.found = found;
		    this.price = aPrice;
		    this.shift = shift;
		}
		private boolean found;
		private double price;
		private int shift;
	}

	private FindPriceAndShift getPrice(LocalDate whenp) {
		LocalDate when = LocalDate.of(whenp.getYear(), whenp.getMonth(), whenp.getDayOfMonth());
		boolean found = false;
		int shift = 1;
		double aPrice = 0.0d;
		while ((when.equals(_start) || when.isAfter(_start)) && !found) {
			if (_adjDictionary.containsKey(when)) {
				aPrice = _adjDictionary.get(when);
				found = true;
			}
			when = when.minusDays(1);
			shift -= 1;
		}
		return new FindPriceAndShift(found, aPrice, shift);
	}

	OptionalDouble getReturn(LocalDate start, LocalDate endDate) {

		FindPriceAndShift foundEnd = getPrice(endDate);
		FindPriceAndShift foundStart = getPrice(start.plusDays(foundEnd.shift));
		if (!foundStart.found || !foundEnd.found)
			return OptionalDouble.empty();
		else {
			return OptionalDouble.of(foundEnd.price / foundStart.price - 1.0d);
		}
	}

	private OptionalDouble lastWeekReturn() {
		return getReturn(LocalDate.now().minusDays(7), LocalDate.now());
	}
	private OptionalDouble last4WeeksReturn() {
		return getReturn(LocalDate.now().minusDays(28), LocalDate.now());
	}
	private OptionalDouble last3MonthsReturn() {
		return getReturn(LocalDate.now().minusMonths(3), LocalDate.now());
	}
	private OptionalDouble last6MonthsReturn() {
		return getReturn(LocalDate.now().minusMonths(6), LocalDate.now());
	}
	private OptionalDouble lastYearReturn() {
		return getReturn(LocalDate.now().minusYears(1), LocalDate.now());
	}
	private Double sum(Collection<Double> d) {
		return d.parallelStream().reduce(0d, Double::sum);
	}
	private Double avg(Collection<Double> d) {
		return sum(d) / d.size();
	}
	private OptionalDouble stdDev() {
		LocalDate now = LocalDate.now();
		LocalDate limit = now.minusYears(3);
		List<Double> rets = new ArrayList<>();
		while (now.compareTo(_start.plusDays(12)) >= 0 && now.compareTo(limit) >= 0) {
			OptionalDouble ret = getReturn(now.minusDays(7), now);
			rets.add(ret.orElse(0d));
			now = now.minusDays(7);
		}

		Double mean = avg(rets);
		Double variance = avg(rets.parallelStream().map(r -> Math.pow(r - mean, 2))
				.collect(toList()));
		Double weeklyStdDev = Math.sqrt(variance);
		return OptionalDouble.of(weeklyStdDev * Math.sqrt(40));
	}
	private OptionalDouble MAV200() {
	    return OptionalDouble.of( 
	        _adjDictionary.entrySet()
	        .parallelStream()
	        .sorted(comparing((Entry<LocalDate,Double> p) -> p.getKey()).reversed())
	        .limit(200)
	        .mapToDouble(e -> e.getValue())
	        .average().getAsDouble()
        );
	}
	private double todayPrice() {
		return getPrice(LocalDate.now()).price;
	}
	public Summary getSummary() {
		return new Summary(ticker, _name, _assetClass, _assetSubClass,
				lastWeekReturn(), last4WeeksReturn(), last3MonthsReturn(),
				last6MonthsReturn(), lastYearReturn(), stdDev(), todayPrice(),
				MAV200());
	}
}

public class Program {
	private static final OptionalDouble OZERO = OptionalDouble.of(0d);

    static String createUrl(String ticker, LocalDate start, LocalDate end) {
		return "http://ichart.finance.yahoo.com/table.csv?s=" + ticker + "&a="
				+ (start.getMonthValue() - 1) + "&b=" + start.getDayOfMonth()
				+ "&c=" + start.getYear() + "&d=" + (end.getMonthValue() - 1)
				+ "&e=" + end.getDayOfMonth() + "&f=" + end.getYear()
				+ "&g=d&ignore=.csv";
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {

        List<String[]> tickers = Files.lines(FileSystems.getDefault().getPath("ETFs.csv"))
        		.skip(1)
        		.parallel()
        		.map(line -> line.split(",", 4))
        		.filter(v -> !v[2].equals("Leveraged"))
        		.collect(toList());
        
        int len = tickers.size();
        
        LocalDate start = LocalDate.now().minusYears(2);
        LocalDate end = LocalDate.now();
        CountDownLatch cevent = new CountDownLatch(len);
        Summary[] summaries = new Summary[len]; 
        
        try (WebClient webClient = new WebClient()) {
        	for (int i = 0; i < len; i++) {
        		String[] t = tickers.get(i);
        		final int index = i;
                webClient.downloadStringAsync(createUrl(t[0], start, end), result -> {
                    summaries[index] = downloadStringCompleted(t[0], t[1], t[2], t[3], result);
                    cevent.countDown();
                }); 
        	}
        	cevent.await();
        }
        
        Stream<Summary> top15perc = 
                Arrays.stream(summaries)
                .filter(s -> s.lrs.isPresent())
                .sorted(comparing((Summary p) -> p.lrs.get()).reversed())
                .limit((int)(len * 0.15));
        Stream<Summary> bottom15perc = 
                Arrays.stream(summaries)
                .filter(s -> s.lrs.isPresent())
                .sorted(comparing((Summary p) -> p.lrs.get()))
                .limit((int)(len * 0.15));
        
        System.out.println();
        Summary.banner();
        System.out.println("TOP 15%");
        top15perc.forEach(s -> s.print());
        
        System.out.println();
        Summary.banner();
        System.out.println("BOTTOM 15%");			
        bottom15perc.forEach(s -> s.print());

	}

    public static Summary downloadStringCompleted(
            String ticker, String name, String asset, String subAsset, DownloadStringAsyncCompletedArgs e
    ) {
        Summary summary;
        if (e.getError() == null) {
            List<Event> adjustedPrices = 
                    Arrays.stream(e.getResult().split("\n"))
                    .skip(1)
                    .parallel()
                    .map(line -> line.split(",", 7))
                    .filter(l -> l.length == 7)
                    .map(v -> new Event(LocalDate.parse(v[0], DateTimeFormatter.ISO_LOCAL_DATE), Double.valueOf(v[6]))).collect(toList());
            TimeSeries timeSeries = new TimeSeries(ticker, name, asset, subAsset, adjustedPrices);
            summary = timeSeries.getSummary();
        } else {
            System.err.printf("[%s ERROR]", ticker);
            summary = new Summary(ticker, name, "ERROR", "ERROR", OZERO, OZERO, OZERO, OZERO, OZERO, OZERO, 0d, OZERO);
        }
        return summary;
    }
}
