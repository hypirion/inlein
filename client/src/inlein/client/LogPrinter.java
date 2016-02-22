package inlein.client;

public final class LogPrinter {
    public static enum Level {
        DEBUG("debug"),
        INFO("info"),
        WARN("warn"),
        ERROR("error");

        public final String stringLevel;
        Level(String stringLevel) {
            this.stringLevel = stringLevel;
        }

        public boolean printLog(Level level) {
            return this.compareTo(level) <= 0;
        }

        public static Level toLevel(String str) {
            for (Level l : Level.values()) {
                if (l.stringLevel.equals(str)) {
                    return l;
                }
            }
            throw new IllegalArgumentException("Unknown loglevel " + str);
        }
    }
    private final Level logLevel;

    public LogPrinter(Level level) {
        logLevel = level;
    }

    public void printLog(Level level, String message) {
        if (logLevel.printLog(level)) {
            System.err.printf("[%s] %s\n", level.name().substring(0, 4), message);
        }
    }
}
