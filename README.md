Smart Task Scheduler
====================

A small Java Swing-based task scheduler with reminders and CSV persistence.

Features
- GUI (Swing) to add/edit/delete tasks.
- Task model with priority, optional deadline, and done flag.
- Background reminder service that shows dialogs for tasks due within 5 minutes.
- Save/load tasks to/from `tasks.csv` in the working directory.

Prerequisites
- Java SE 8 or newer (OpenJDK/Oracle JDK). Java 11+ recommended.
- On Windows, PowerShell is used in the examples below.

Project layout (important paths)
- Source files are in the `src` folder (or in this workspace you may also see a `buildsrc` copy created by tooling).
- Compiled classes go into an `out` folder.
- `tasks.csv` is saved/loaded from the current working directory when you click Save/Load in the UI.

Quick build & run (PowerShell)

1) From the project root (example path shown):

```powershell
cd "C:\Users\Lenovo\Tasks Scheduler"

# If your sources are in a normal src folder
javac -d out .\src\*.java

# OR if you have the helper copy created during tooling, compile from buildsrc
javac -d out .\buildsrc\*.java

# Run (starts the Swing GUI)
java -cp out App
```

Notes
- This is a GUI application: running `java -cp out App` opens a window titled "Smart Task Scheduler". It won't print useful information to console.
- The ReminderService uses Swing dialogs (JOptionPane). Reminder popups are GUI dialogs and won't appear in console logs.
- `tasks.csv` will be created in whichever directory you launch `java` from. If you run from the project root, `tasks.csv` will appear there.

Packaging into a runnable JAR (optional)

1) Create a simple manifest file `manifest.txt` containing:

```text
Main-Class: App
```

2) Create the JAR from the `out` folder:

```powershell
cd "C:\Users\Lenovo\Tasks Scheduler"
jar cfm tasks-scheduler.jar manifest.txt -C out .

# Run the jar
java -jar tasks-scheduler.jar
```

Troubleshooting & tips
- Strange/empty folder names: If you see odd paths (for example a folder name that looks blank/contains a trailing space), compilation may fail when using relative globs. Workarounds:
  - Move the `src` folder to a normal path with no trailing spaces.
  - Compile using absolute paths (quoted) for each file.
  - Use the `buildsrc` directory (included/copied during local tooling) and compile from there as shown above.

- No console output: The app is GUI-first. To get textual output for testing, create a simple CLI runner (see next section).

Adding a headless/CLI mode for automated runs (suggestion)
- If you want to run the program in a non-GUI mode for CI or logging, add a small conditional to `App.main` that checks for an argument like `--nogui` and runs a text mode. Example (conceptual):

```java
public static void main(String[] args) throws Exception {
    if (args.length > 0 && "--nogui".equals(args[0])) {
        TaskManager manager = new TaskManager();
        // create sample tasks and print them
        System.out.println("Tasks:");
        for (Task t : manager.ordered()) System.out.println(t);
        return;
    }
    javax.swing.SwingUtilities.invokeLater(() -> { ... });
}
```

This keeps the UI intact while allowing automated runs.

Next steps (recommended)
- If you want, I can:
  - Add a CLI runner and a small test that creates tasks and prints reminder checks to console.
  - Implement a `--nogui` flag in `App` so you can run the program in headless/test mode.
  - Generate a proper `tasks-scheduler.zip` or an installer for distribution.

License
- No license file is included. Add a LICENSE file (MIT/Apache/etc.) if you plan to publish.

Contact / Support
- If you want changes (headless mode, sample tasks, or a test harness), tell me which and I will implement them and run the program to show output.
