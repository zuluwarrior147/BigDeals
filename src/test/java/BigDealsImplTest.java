import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Interval;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.function.Consumer;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Andrii Markovych
 */
@RunWith(MockitoJUnitRunner.class)
public class BigDealsImplTest {
    private static final Object TEST_SUPPLIED_INSTANCE = new Object();

    @Mock
    private Consumer<Object> consumer;

    private BigDealsImpl<Object> testingInstance = new BigDealsImpl<>();

    private DateTime certainTime = DateTime.now();

    @Test
    public void shouldCallSupplyMethod() {
        testingInstance.supplyForCertainDates(Object::new, new DateTime());
    }

    @Test
    public void shouldConsumeSuppliedInstanceWithCertainDateAvailability() {
        testingInstance.supplyForCertainDates(() -> TEST_SUPPLIED_INSTANCE, certainTime);
        testingInstance.consume(consumer, certainTime);

        verify(consumer).accept(TEST_SUPPLIED_INSTANCE);
    }

    @Test
    public void shouldBeNotAvailableAfterConsume() {
        testingInstance.supplyForCertainDates(() -> TEST_SUPPLIED_INSTANCE, certainTime);
        testingInstance.consume(consumer, certainTime);

        verify(consumer).accept(TEST_SUPPLIED_INSTANCE);
        verifyNoMoreInteractions(consumer);

        testingInstance.consume(consumer, certainTime);
    }

    @Test
    public void shouldConsumeWithCertainIntervalAvailability() {
       testingInstance.supplyForCertainIntervals(() -> TEST_SUPPLIED_INSTANCE,
                new Interval(certainTime.minusDays(1), certainTime.plusDays(1)));
        testingInstance.consume(consumer, certainTime);

        verify(consumer).accept(TEST_SUPPLIED_INSTANCE);
        verifyNoMoreInteractions(consumer);

        testingInstance.consume(consumer, certainTime);
    }

    @Test
    public void shouldConsumeWithWholeMonthAvailability() {
        testingInstance.supplyForAWholeMonth(() -> TEST_SUPPLIED_INSTANCE);
        testingInstance.consume(consumer, certainTime);

        verify(consumer).accept(TEST_SUPPLIED_INSTANCE);
    }

    @Test
    public void shouldConsumeWhenMoreThanOneSupplierAvailable() {
        testingInstance.supplyForAWholeMonth(() -> TEST_SUPPLIED_INSTANCE);
        testingInstance.supplyForCertainDates(() -> TEST_SUPPLIED_INSTANCE, certainTime);
        testingInstance.consume(consumer, certainTime);
        testingInstance.consume(consumer, certainTime);

        verify(consumer, times(2)).accept(TEST_SUPPLIED_INSTANCE);
    }

    @Test
    public void shouldConsumeWhenMonthWithoutWeekendsAvailable() {
        DateTime weekDay = certainTime.withDayOfWeek(DateTimeConstants.WEDNESDAY);

        testingInstance.supplyForMonthExceptWeekends(() -> TEST_SUPPLIED_INSTANCE);
        testingInstance.consume(consumer, weekDay);

        verify(consumer).accept(TEST_SUPPLIED_INSTANCE);
    }

    @Test
    public void shouldNotConsumeWeekendsWhenOnlyWeekdaysAvailable() {
        DateTime weekend = DateTime.now().withDayOfWeek(DateTimeConstants.SUNDAY);

        testingInstance.supplyForMonthExceptWeekends(() -> TEST_SUPPLIED_INSTANCE);
        testingInstance.consume(consumer, weekend);

        verify(consumer, never()).accept(TEST_SUPPLIED_INSTANCE);
    }
}