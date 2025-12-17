package gdg.travodobackend.app.travel.repository;

import gdg.travodobackend.app.travel.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByTripIdAndDate(Long tripId, LocalDate date);

    List<Expense> findByTripIdAndDayIndex(Long tripId, Integer dayIndex);

    Optional<Expense> findByIdAndTripId(Long id, Long tripId);

    interface ExpenseSummaryRow {
        Integer getDayIndex();
        LocalDate getDate();
        Long getAmount(); // SUM -> Long
    }

    @Query("""
        select e.dayIndex as dayIndex, e.date as date, sum(e.amount) as amount
        from Expense e
        where e.trip.id = :tripId
        group by e.dayIndex, e.date
        order by e.dayIndex asc
    """)
    List<ExpenseSummaryRow> summarizeByTrip(@Param("tripId") Long tripId);

    @Query("""
        select coalesce(sum(e.amount), 0)
        from Expense e
        where e.trip.id = :tripId
    """)
    Long totalAmountByTrip(@Param("tripId") Long tripId);
}
