import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Interval;
import org.joda.time.Months;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BigDealsImpl<T> implements BigDeals<T> {
    private List<Node<T>> availableSuppliers = new LinkedList<>();

    @Override
    public void supplyForCertainDates(Supplier<T> supplier, DateTime... dates) {
        for (DateTime date : dates) {
            supplyForCertainIntervals(supplier, new Interval(date.minusDays(1), date.plusDays(1)));
        }
    }

    @Override
    public void supplyForCertainIntervals(Supplier<T> supplier, Interval... intervals) {
        for (Interval interval : intervals) {
            availableSuppliers.add(new Node<T>(supplier, interval));
        }
    }

    @Override
    public void supplyForAWholeMonth(Supplier<T> supplier) {
        DateTime monthFirstDay = getFirstMonthDay();
        Interval monthInterval = new Interval(monthFirstDay, Months.ONE);
        supplyForCertainIntervals(supplier, monthInterval);
    }

    @Override
    public void supplyForMonthExceptWeekends(Supplier<T> supplier) {
        DateTime counterDate = getFirstMonthDay();
        DateTime lastDayOfMonth = DateTime.now().dayOfMonth().withMaximumValue();

        while(counterDate.isBefore(lastDayOfMonth)) {
            if(!isWeekend(counterDate)){
                DateTime endOfWeek = counterDate.withDayOfWeek(DateTimeConstants.FRIDAY).isBefore(lastDayOfMonth) ?
                        counterDate.withDayOfWeek(DateTimeConstants.FRIDAY) : lastDayOfMonth;
                Interval weekInterval = new Interval(counterDate, endOfWeek);
                availableSuppliers.add(new Node<T> (supplier, weekInterval));
                counterDate = endOfWeek.plusDays(2);
            }
            counterDate = counterDate.plusDays(1);
        }
    }

    @Override
    public void consume(Consumer<T> consumer, DateTime time) {
        Optional<T> suppliedValue = searchForAvailableSupplier(time);
        suppliedValue.ifPresent(consumer);
    }

    private Optional<T> searchForAvailableSupplier(DateTime time) {
        Optional<Node<T>> searchingResult = availableSuppliers.stream()
                .filter(tNode -> tNode.interval.contains(time))
                .findFirst();

        if (searchingResult.isPresent()) {
            Node<T> searchNode = searchingResult.get();
            splitSupplierForSeparateIntervals(searchNode, time);
            return Optional.of(searchNode.supplier.get());
        }
        return Optional.empty();
    }

    private void splitSupplierForSeparateIntervals(Node<T> node, DateTime currentDate) {
        Interval initialInterval = node.interval;
        DateTime endDate = initialInterval.getEnd();
        node.interval = initialInterval.withEnd(currentDate.minusDays(1));
        supplyForCertainIntervals(node.supplier, new Interval(currentDate.plusDays(1), endDate));
    }

    private DateTime getFirstMonthDay() {
        return DateTime.now().dayOfMonth().withMinimumValue();
    }

    private boolean isWeekend(DateTime time) {
        return time.getDayOfWeek() == DateTimeConstants.SATURDAY ||
                time.getDayOfWeek() == DateTimeConstants.SUNDAY;
    }
    private static class Node<T> {
        private Interval interval;
        private Supplier<T> supplier;

        Node(Supplier<T> supplier, Interval interval) {
            this.interval = interval;
            this.supplier = supplier;
        }
    }
}
