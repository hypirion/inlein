package inlein.client.signals;

public class Registerer {
    static void dropSignal(String name) {
        try {
            RegistererHelper.dropSignal(name);
        } catch (Throwable t) {
        }
    }

    public static void dropSignals() {
        // TODO: This is... hacky
        dropSignal("INT");
        dropSignal("CONT");
    }
}
