import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JOptionPane;

public class ReminderService {
    private final TaskManager manager;
    private final Timer timer = new Timer(true);

    public ReminderService(TaskManager manager) {
        this.manager = manager;
    }

    public void start() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkDue();
            }
        }, 5_000L, 60_000L); // first after 5s, then every minute
    }

    private void checkDue() {
        List<Task> tasks = manager.ordered();
        LocalDateTime now = LocalDateTime.now();
        for (Task t : tasks) {
            if (t.isDone()) continue;
            if (t.getDeadline() == null) continue;
            long minutes = Duration.between(now, t.getDeadline()).toMinutes();
            if (minutes >= 0 && minutes <= 5) {
                JOptionPane.showMessageDialog(null,
                        "Upcoming task in " + minutes + " min: " + t.getTitle(),
                        "Task Reminder",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
}
