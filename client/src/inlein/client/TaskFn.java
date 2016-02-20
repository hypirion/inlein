package inlein.client;

public interface TaskFn {
    /**
     * Runs the Task function. {@link ServerConnection} may be null, in which
     * case it is the task's responsibility to ensure that it is initialised --
     * this can be done via {@link ServerConnection#ensureInit(ServerConnection)}.
     */
    public void run(ServerConnection conn);
}
