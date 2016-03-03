package inlein.client.signals;

import sun.misc.Signal;
import sun.misc.SignalHandler;

class RegistererHelper {
    static void dropSignal(String signame) {
        Signal.handle(new Signal(signame), dropper);
    }

    static SignalHandler dropper = new SignalHandler() {
            public void handle (Signal sig) {
            }
        };
}
