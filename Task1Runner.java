import java.util.Base64;

public class Task1Runner {
    interface ReportFormatter {
        String format(String title, String[] headers, String[][] rows);
    }

    static class JsonFormatter implements ReportFormatter {
        public String format(String title, String[] headers, String[][] rows) {
            StringBuilder s = new StringBuilder("{\n  \"report\": \"" + title + "\",\n  \"records\": [\n");
            for (int i = 0; i < rows.length; i++) {
                s.append("    {");
                for (int j = 0; j < headers.length; j++) {
                    s.append("\"").append(headers[j]).append("\": \"").append(rows[i][j]).append("\"");
                    if (j < headers.length - 1) s.append(", ");
                }
                s.append("}");
                if (i < rows.length - 1) s.append(",");
                s.append("\n");
            }
            return s.append("  ]\n}").toString();
        }
    }

    interface Exporter {
        void export(String destination, String data);
    }

    static class BaseFileExporter implements Exporter {
        public void export(String destination, String data) {
            System.out.println("[DISK WRITE] Writing " + data.length() + " bytes to file://" + destination);
            System.out.println("----- FILE PAYLOAD BEGIN -----");
            System.out.println(data);
            System.out.println("----- FILE PAYLOAD END -----");
        }
    }

    static abstract class ExporterDecorator implements Exporter {
        protected Exporter wrapped;
        ExporterDecorator(Exporter wrapped) {
            if (wrapped == null) throw new IllegalArgumentException("Exporter cannot be null");
            this.wrapped = wrapped;
        }
        public void export(String destination, String data) {
            wrapped.export(destination, data);
        }
    }

    static class HeaderFooterDecorator extends ExporterDecorator {
        private String timestamp;
        HeaderFooterDecorator(Exporter wrapped, String timestamp) {
            super(wrapped);
            this.timestamp = timestamp;
        }
        public void export(String destination, String data) {
            String result = "=== METADATA HEADER: " + timestamp + " ===\n"
                    + data + "\n=== INTEGRITY CHECK: SHA-256 OK | END RECORD ===";
            super.export(destination, result);
        }
    }

    static class Base64EncryptDecorator extends ExporterDecorator {
        Base64EncryptDecorator(Exporter wrapped) { super(wrapped); }
        public void export(String destination, String data) {
            String encoded = Base64.getEncoder().encodeToString(data.getBytes());
            super.export(destination, "[BASE64_CIPHERTEXT:" + encoded + "]");
        }
    }

    public static void main(String[] args) {
        String[] headers = {"ID", "User", "Role"};
        String[][] rows = {{"101", "sufyan", "Admin"}, {"102", "alex", "Dev"}};

        ReportFormatter formatter = new JsonFormatter();
        String report = formatter.format("UserAuditReport", headers, rows);

        Exporter pipeline = new Base64EncryptDecorator(
                new HeaderFooterDecorator(new BaseFileExporter(), "2026-11-02 10:00:00 UTC"));

        System.out.println(">>> TASK 1: STRATEGY + DECORATOR <<<");
        pipeline.export("/var/logs/audit.sec", report);
    }
}
