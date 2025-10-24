import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Predicate;

public class TaskManager {
    private final PriorityQueue<Task> queue = new PriorityQueue<>();

    public synchronized void add(Task t) {
        queue.add(t);
    }

    public synchronized void remove(Task t) {
        queue.remove(t);
    }

    public synchronized List<Task> all() {
        return new ArrayList<>(queue);
    }

    public synchronized List<Task> ordered() {
        List<Task> list = new ArrayList<>(queue);
        Collections.sort(list);
        return list;
    }

    public synchronized List<Task> filtered(Predicate<Task> pred) {
        List<Task> list = new ArrayList<>();
        for (Task t : queue) if (pred.test(t)) list.add(t);
        Collections.sort(list);
        return list;
    }

    public static Predicate<Task> todayFilter() {
        LocalDate today = LocalDate.now();
        return t -> t.getDeadline() != null && t.getDeadline().toLocalDate().equals(today);
    }

    public static Predicate<Task> highPriorityFilter() {
        return t -> t.getPriority() == Task.Priority.HIGH;
    }

    public static Predicate<Task> dueThisWeekFilter() {
        LocalDate today = LocalDate.now();
        LocalDate end = today.plusDays(7);
        return t -> {
            LocalDateTime d = t.getDeadline();
            if (d == null) return false;
            LocalDate ld = d.toLocalDate();
            return !ld.isBefore(today) && !ld.isAfter(end);
        };
    }
}
