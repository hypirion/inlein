package inlein.client.tasks;

import inlein.client.*;
import java.io.*;
import java.net.*;
import java.util.*;

public final class Help extends Task {

    private final Map<String, Task> tasks;

    public Help(Map<String, Task> tasks) {
        super("--help",
              "Prints this banner, or extended information about a task.",
              "Prints this banner, or extended information about a task.");
        this.tasks = tasks;
    }

    public void run(ServerConnection conn, String[] args) {
        if (args.length == 0) {
            printBanner();
            System.out.println("");
            System.out.println("Several tasks are available:");
            printTasks();
        }
        else {
            assertArgcount(args, 1);
            String tname = args[0];
            if (! tname.startsWith("--")) {
                tname = "--" + tname;
            }
            Task t = tasks.get(tname);
            if (t == null) {
                System.out.println("Inlein has no task with name " + tname);
                System.out.println("\nMaybe you meant one of these?\n");
                printTasks();
                System.exit(1);
            }
            System.out.println("Information for task " + tname + ":");
            System.out.println(t.docstring);
        }
    }

    private void printBanner() {
        System.out.println("inlein is a tool to handle Clojure scripts with dependencies");
        System.out.println("");
        System.out.println("Usage: inlein [--run] file [args...]");
        System.out.println("       (to run a clojure script)");
        System.out.println("   or  inlein --task [args...]");
        System.out.println("       (to run an inlein task)");
    }

    private void printTasks() {
        for (Task task : tasks.values()) {
            System.out.println(task.taskname + " " + task.shortdoc);
        }
    }
}
