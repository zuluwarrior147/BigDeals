import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Andrii Markovych
 */
public interface BigDeals<T> {
    void supplyForCertainDates(Supplier<T> supplier, DateTime... dates);
    void supplyForCertainIntervals(Supplier<T> supplier, Interval... intervals);
    void supplyForAWholeMonth(Supplier<T> supplier);
    void supplyForMonthExceptWeekends(Supplier<T> supplier);
    void consume(Consumer<T> consumer, DateTime time);

}
