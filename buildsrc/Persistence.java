import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Persistence {
    private static final String FILE_NAME = "tasks.csv";

    public static void save(List<Task> tasks, File baseDir) throws IOException {
        File f = new File(baseDir, FILE_NAME);
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8))) {
            pw.println("title,priority,deadline,done");
            for (Task t : tasks) {
                pw.printf("%s,%s,%s,%s%n",
                        escape(t.getTitle()),
                        t.getPriority().name(),
                        t.getDeadline() == null ? "" : t.deadlineString(),
                        t.isDone());
            }
        }
    }

    public static List<Task> load(File baseDir) throws IOException {
        File f = new File(baseDir, FILE_NAME);
        List<Task> tasks = new ArrayList<>();
        if (!f.exists()) return tasks;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            String line = br.readLine(); // header
            while ((line = br.readLine()) != null) {
                String[] parts = parseCSV(line);
                if (parts.length < 4) continue;
                String title = unescape(parts[0]);
                Task.Priority p = Task.Priority.valueOf(parts[1]);
                LocalDateTime dl = Task.parseDeadline(parts[2]);
                boolean done = Boolean.parseBoolean(parts[3]);
                Task t = new Task(title, p, dl);
                t.setDone(done);
                tasks.add(t);
            }
        }
        return tasks;
    }

    private static String escape(String s) {
        if (s == null) return "";
        String v = s.replace("\"", "\"\"");
        if (v.contains(",") || v.contains("\n") || v.contains("\"")) {
            return '"' + v + '"';
        }
        return v;
    }

    private static String unescape(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length() - 1).replace("\"\"", "\"");
        }
        return s;
    }

    private static String[] parseCSV(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        sb.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else sb.append(c);
            } else {
                if (c == ',') {
                    out.add(sb.toString());
                    sb.setLength(0);
                } else if (c == '"') {
                    inQuotes = true;
                } else sb.append(c);
            }
        }
        out.add(sb.toString());
        return out.toArray(new String[0]);
    }
}
