public class Task3Runner {
    interface DispatchStrategy {
        boolean shouldDispatch(int severity, int hour);
    }

    static class ImmediateStrategy implements DispatchStrategy {
        public boolean shouldDispatch(int severity, int hour) { return true; }
    }

    static class BusinessHoursStrategy implements DispatchStrategy {
        public boolean shouldDispatch(int severity, int hour) {
            return severity >= 5 || (hour >= 9 && hour <= 17);
        }
    }

    interface MessageChannel {
        void transmit(String recipient, String message);
    }

    static class EmailChannel implements MessageChannel {
        public void transmit(String recipient, String message) {
            System.out.println("[EMAIL -> " + recipient + "]");
            System.out.println(message);
        }
    }

    static class SmsChannel implements MessageChannel {
        public void transmit(String recipient, String message) {
            System.out.println("[SMS -> " + recipient + "] " + message.replace("\n", " -- "));
        }
    }

    // Bridge: Alert uses a channel. Strategy decides if it can send.
    static abstract class Alert {
        protected MessageChannel channel;
        protected DispatchStrategy strategy;

        Alert(MessageChannel channel, DispatchStrategy strategy) {
            this.channel = channel;
            this.strategy = strategy;
        }

        abstract int severity();
        abstract String format(String message);

        public void send(String recipient, String message, int hour) {
            if (strategy.shouldDispatch(severity(), hour)) {
                channel.transmit(recipient, format(message));
            } else {
                System.out.println("[DISPATCH SUPPRESSED] Outside business hours");
            }
        }
    }

    static class CriticalSystemAlert extends Alert {
        CriticalSystemAlert(MessageChannel c, DispatchStrategy s) { super(c, s); }
        int severity() { return 5; }
        String format(String message) {
            return "!!! CRITICAL ALERT !!!\n" + message + "\nACTION REQUIRED";
        }
    }

    static class DailySummaryAlert extends Alert {
        DailySummaryAlert(MessageChannel c, DispatchStrategy s) { super(c, s); }
        int severity() { return 1; }
        String format(String message) { return "Daily Digest:\n" + message; }
    }

    public static void main(String[] args) {
        DispatchStrategy business = new BusinessHoursStrategy();
        System.out.println(">>> TASK 3: BRIDGE + STRATEGY <<<");

        Alert emergency = new CriticalSystemAlert(new SmsChannel(), business);
        emergency.send("+1555019283", "Database problem", 2);

        Alert night = new DailySummaryAlert(new EmailChannel(), business);
        night.send("team@example.com", "99.98% uptime", 23);

        Alert day = new DailySummaryAlert(new EmailChannel(), business);
        day.send("team@example.com", "99.98% uptime", 11);
    }
}
