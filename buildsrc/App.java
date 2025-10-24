public class App {
    public static void main(String[] args) throws Exception {
        javax.swing.SwingUtilities.invokeLater(() -> {
            TaskManager manager = new TaskManager();
            TaskSchedulerUI ui = new TaskSchedulerUI(manager);
            ui.setVisible(true);
        });
    }
}
