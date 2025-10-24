import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Task implements Comparable<Task> {
    public enum Priority {
        HIGH(3), MEDIUM(2), LOW(1);
        private final int weight;
        Priority(int w) { this.weight = w; }
        public int weight() { return weight; }
    }

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private String title;
    private Priority priority;
    private LocalDateTime deadline;
    private boolean done;

    public Task(String title, Priority priority, LocalDateTime deadline) {
        this.title = title;
        this.priority = priority;
        this.deadline = deadline;
        this.done = false;
    }

    public String getTitle() { return title; }
    public void setTitle(String t) { this.title = t; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority p) { this.priority = p; }

    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime d) { this.deadline = d; }

    public boolean isDone() { return done; }
    public void setDone(boolean d) { this.done = d; }

    public String deadlineString() {
        return deadline == null ? "" : FMT.format(deadline);
    }

    public static LocalDateTime parseDeadline(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        return LocalDateTime.parse(s.trim(), FMT);
    }

    @Override
    public int compareTo(Task other) {
        if (this.done != other.done) {
            return this.done ? 1 : -1; // incomplete first
        }
        int pr = Integer.compare(other.priority.weight(), this.priority.weight());
        if (pr != 0) return pr;
        if (this.deadline == null && other.deadline == null) return 0;
        if (this.deadline == null) return 1;
        if (other.deadline == null) return -1;
        return this.deadline.compareTo(other.deadline);
    }

    @Override
    public String toString() {
        return (done ? "[x] " : "[ ] ") + title + " (" + priority + ")" + (deadline == null ? "" : (" due " + deadlineString()));
    }
}
