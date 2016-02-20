package inlein.client;

public abstract class Task implements Comparable<Task> {
    public final String taskname;
    public final String shortdoc;
    public final String docstring;

    protected Task(String taskname, String shortdoc, String docstring) {
        this.taskname = taskname;
        this.docstring = docstring;
        this.shortdoc = shortdoc;
    }

    public int compareTo(Task that) {
        return this.taskname.compareTo(that.taskname);
    }

    /**
     * Runs the Task function. {@link ServerConnection} may be null, in which
     * case it is the task's responsibility to ensure that it is initialised --
     * this can be done via {@link ServerConnection#ensureInit(ServerConnection)}.
     */
    public abstract void run(ServerConnection conn, String[] args);

    protected void assertArgcount(String[] args, int expected) {
        if (args.length != expected) {
            System.out.printf("Task %s expected %d arguments as input, but got %d.\n",
                              taskname,
                              expected, args.length);
            System.exit(1);
        }
    }
}
