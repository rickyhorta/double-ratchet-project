// metrics/PerformanceMetrics.java
package metrics;

import java.time.Duration;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PerformanceMetrics {
    private List<Long> encryptionTimes = new CopyOnWriteArrayList<>();
    private List<Long> decryptionTimes = new CopyOnWriteArrayList<>();
    private List<Long> keyExchangeTimes = new CopyOnWriteArrayList<>();

    private int messagesSent = 0;
    private int messagesReceived = 0;
    private long startTime = System.currentTimeMillis();

    public void recordEncryption(long nanoseconds) {
        encryptionTimes.add(nanoseconds);
    }

    public void recordDecryption(long nanoseconds) {
        decryptionTimes.add(nanoseconds);
    }

    public void recordKeyExchange(long nanoseconds) {
        keyExchangeTimes.add(nanoseconds);
    }

    public void incrementMessagesSent() {
        messagesSent++;
    }

    public void incrementMessagesReceived() {
        messagesReceived++;
    }

    public void displayMetrics() {
        System.out.println("\n===== Performance Metrics =====");

        // Session statistics
        long runtime = System.currentTimeMillis() - startTime;
        Duration runDuration = Duration.ofMillis(runtime);
        System.out.printf("Session runtime: %02d:%02d:%02d\n",
                runDuration.toHours(),
                runDuration.toMinutesPart(),
                runDuration.toSecondsPart());

        System.out.println("Messages sent: " + messagesSent);
        System.out.println("Messages received: " + messagesReceived);

        // Time metrics
        displayTimeMetrics("Encryption", encryptionTimes);
        displayTimeMetrics("Decryption", decryptionTimes);
        displayTimeMetrics("Key Exchange", keyExchangeTimes);

        System.out.println("==============================\n");
    }

    private void displayTimeMetrics(String operation, List<Long> timings) {
        if (timings.isEmpty()) {
            System.out.println(operation + " operations: 0");
            return;
        }

        // Convert to milliseconds for better readability
        List<Double> milliseconds = new ArrayList<>();
        for (Long nanos : timings) {
            milliseconds.add(nanos / 1_000_000.0);
        }

        DoubleSummaryStatistics stats = milliseconds.stream().mapToDouble(Double::doubleValue).summaryStatistics();

        System.out.printf("%s operations: %d\n", operation, stats.getCount());
        System.out.printf("  - Avg time: %.3f ms\n", stats.getAverage());
        System.out.printf("  - Min time: %.3f ms\n", stats.getMin());
        System.out.printf("  - Max time: %.3f ms\n", stats.getMax());
        System.out.printf("  - Total time: %.3f ms\n", stats.getSum());
    }

    public String generateReport() {
        StringBuilder report = new StringBuilder();
        report.append("# Double Ratchet Performance Report\n\n");

        // Session information
        long runtime = System.currentTimeMillis() - startTime;
        Duration runDuration = Duration.ofMillis(runtime);
        report.append("## Session Information\n");
        report.append(String.format("- Runtime: %02d:%02d:%02d\n",
                runDuration.toHours(),
                runDuration.toMinutesPart(),
                runDuration.toSecondsPart()));
        report.append("- Messages sent: ").append(messagesSent).append("\n");
        report.append("- Messages received: ").append(messagesReceived).append("\n\n");

        // Time metrics
        report.append("## Performance Metrics\n\n");
        appendTimeMetricsToReport(report, "Encryption", encryptionTimes);
        appendTimeMetricsToReport(report, "Decryption", decryptionTimes);
        appendTimeMetricsToReport(report, "Key Exchange", keyExchangeTimes);

        return report.toString();
    }

    private void appendTimeMetricsToReport(StringBuilder report, String operation, List<Long> timings) {
        report.append("### ").append(operation).append(" Performance\n");

        if (timings.isEmpty()) {
            report.append("No ").append(operation.toLowerCase()).append(" operations performed.\n\n");
            return;
        }

        // Convert to milliseconds for better readability
        List<Double> milliseconds = new ArrayList<>();
        for (Long nanos : timings) {
            milliseconds.add(nanos / 1_000_000.0);
        }

        DoubleSummaryStatistics stats = milliseconds.stream().mapToDouble(Double::doubleValue).summaryStatistics();

        report.append("- Total operations: ").append(stats.getCount()).append("\n");
        report.append(String.format("- Average time: %.3f ms\n", stats.getAverage()));
        report.append(String.format("- Minimum time: %.3f ms\n", stats.getMin()));
        report.append(String.format("- Maximum time: %.3f ms\n", stats.getMax()));
        report.append(String.format("- Total time: %.3f ms\n\n", stats.getSum()));
    }
}