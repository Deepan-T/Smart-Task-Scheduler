import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;

public class TaskSchedulerUI extends JFrame {
    private final TaskManager manager;
    private final ReminderService reminders;
    private final TaskTableModel tableModel = new TaskTableModel();
    private final JTable table = new JTable(tableModel);
    private final JComboBox<String> filterBox = new JComboBox<>(new String[]{"All", "Today", "High Priority", "This Week"});

    public TaskSchedulerUI(TaskManager manager) {
        super("Smart Task Scheduler");
        this.manager = manager;
        this.reminders = new ReminderService(manager);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton delBtn = new JButton("Delete");
        JButton toggleDoneBtn = new JButton("Toggle Done");
        JButton saveBtn = new JButton("Save");
        JButton loadBtn = new JButton("Load");
        top.add(addBtn);
        top.add(editBtn);
        top.add(delBtn);
        top.add(toggleDoneBtn);
        top.add(new JLabel("Filter:"));
        top.add(filterBox);
        top.add(saveBtn);
        top.add(loadBtn);
        add(top, BorderLayout.NORTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        addBtn.addActionListener(this::onAdd);
        editBtn.addActionListener(this::onEdit);
        delBtn.addActionListener(this::onDelete);
        toggleDoneBtn.addActionListener(this::onToggleDone);
        saveBtn.addActionListener(this::onSave);
        loadBtn.addActionListener(this::onLoad);
        filterBox.addActionListener(e -> refresh());

        refresh();
        reminders.start();
    }

    private void onAdd(ActionEvent e) {
        Task t = TaskDialog.showDialog(this, null);
        if (t != null) {
            manager.add(t);
            refresh();
        }
    }

    private void onEdit(ActionEvent e) {
        int idx = table.getSelectedRow();
        if (idx < 0) return;
        Task current = tableModel.getAt(table.convertRowIndexToModel(idx));
        Task edited = TaskDialog.showDialog(this, current);
        if (edited != null) {
            current.setTitle(edited.getTitle());
            current.setPriority(edited.getPriority());
            current.setDeadline(edited.getDeadline());
            refresh();
        }
    }

    private void onDelete(ActionEvent e) {
        int idx = table.getSelectedRow();
        if (idx < 0) return;
        Task t = tableModel.getAt(table.convertRowIndexToModel(idx));
        manager.remove(t);
        refresh();
    }

    private void onToggleDone(ActionEvent e) {
        int idx = table.getSelectedRow();
        if (idx < 0) return;
        Task t = tableModel.getAt(table.convertRowIndexToModel(idx));
        t.setDone(!t.isDone());
        refresh();
    }

    private void onSave(ActionEvent e) {
        try {
            Persistence.save(manager.ordered(), new File("."));
            JOptionPane.showMessageDialog(this, "Saved to tasks.csv");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onLoad(ActionEvent e) {
        try {
            List<Task> tasks = Persistence.load(new File("."));
            for (Task t : tasks) manager.add(t);
            refresh();
            JOptionPane.showMessageDialog(this, "Loaded " + tasks.size() + " tasks");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Load failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Predicate<Task> currentFilter() {
        String f = (String) filterBox.getSelectedItem();
        if ("Today".equals(f)) return TaskManager.todayFilter();
        if ("High Priority".equals(f)) return TaskManager.highPriorityFilter();
        if ("This Week".equals(f)) return TaskManager.dueThisWeekFilter();
        return t -> true;
    }

    private void refresh() {
        tableModel.setData(manager.filtered(currentFilter()));
    }

    static class TaskTableModel extends AbstractTableModel {
        private final String[] cols = {"Done", "Title", "Priority", "Deadline"};
        private java.util.List<Task> data = java.util.Collections.emptyList();

        public void setData(java.util.List<Task> d) {
            this.data = d;
            fireTableDataChanged();
        }

        public Task getAt(int row) { return data.get(row); }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Task t = data.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return t.isDone();
                case 1:
                    return t.getTitle();
                case 2:
                    return t.getPriority();
                case 3:
                    return t.getDeadline() == null ? "" : t.deadlineString();
                default:
                    return "";
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return Boolean.class;
                default:
                    return String.class;
            }
        }
    }

    static class TaskDialog {
        public static Task showDialog(Component parent, Task existing) {
            JTextField title = new JTextField(existing == null ? "" : existing.getTitle());
            JComboBox<Task.Priority> priority = new JComboBox<>(Task.Priority.values());
            if (existing != null) priority.setSelectedItem(existing.getPriority());
            JTextField deadline = new JTextField(existing == null ? "" : (existing.getDeadline() == null ? "" : existing.deadlineString()));

            JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
            panel.add(new JLabel("Title")); panel.add(title);
            panel.add(new JLabel("Priority")); panel.add(priority);
            panel.add(new JLabel("Deadline (yyyy-MM-dd HH:mm)")); panel.add(deadline);

            int res = JOptionPane.showConfirmDialog(parent, panel, existing == null ? "Add Task" : "Edit Task", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (res == JOptionPane.OK_OPTION) {
                String t = title.getText().trim();
                if (t.isEmpty()) return null;
                Task.Priority p = (Task.Priority) priority.getSelectedItem();
                LocalDateTime d = null;
                String ds = deadline.getText().trim();
                if (!ds.isEmpty()) {
                    try { d = Task.parseDeadline(ds); } catch (Exception ex) {
                        JOptionPane.showMessageDialog(parent, "Invalid date format.");
                        return null;
                    }
                }
                return new Task(t, p, d);
            }
            return null;
        }
    }
}
