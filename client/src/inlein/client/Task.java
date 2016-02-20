package inlein.client;

import java.util.*;

public final class Task implements Comparable<Task> {

    /**
     * All the tasks available to Inlein.
     */
    public static final TreeMap<String, Task> tasks = new TreeMap();

    public final String taskname;
    public final String shortdoc;
    public final String docstring;
    public final TaskFn fn;

    public Task(String taskname, String docstring, String shortdoc, TaskFn fn) {
        this.taskname = taskname;
        this.docstring = docstring;
        this.shortdoc = shortdoc;
        this.fn = fn;
    }

    public int compareTo(Task that) {
        return this.taskname.compareTo(that.taskname);
    }

    public void run(ServerConnection conn) {
        this.fn.run(conn);
    }
}
