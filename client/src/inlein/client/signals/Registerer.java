package inlein.client.signals;

public class Registerer {
    static void dropSignal(String name) {
        try {
            RegistererHelper.dropSignal(name);
        } catch (Throwable t) {
        }
    }

    public static void dropSignals() {
        // TODO: This is... hacky? :)
        for (String s : new String[]{"HUP", "INT", "TRAP", "ABRT", "BUS", "USR2",
                                     "PIPE", "ARLM", "TERM", "STKFLT", "CHLD",
                                     "CONT", "TSTP", "TTIN", "TTOU", "URG",
                                     "XCPU", "XFSZ", "VTALRM", "PROF", "WINCH",
                                     "IO", "PWR", "SYS"}) {
            dropSignal(s);
        }
    }
}
