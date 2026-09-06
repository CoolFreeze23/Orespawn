package danger.orespawn.bench;

/**
 * Phase G slice (d): the seam between the server half of the harness (common code) and the client
 * sampler ({@code danger.orespawn.client.bench.BenchClientSampler}, client-only). The client installs
 * its controller at client setup; on a dedicated server nothing is installed and every call is a
 * no-op that the report records as "client not available". No client class is referenced here, so
 * the game-test server loads this class.
 */
public final class BenchClientBridge {

    /** The client sampler's surface, as the server command drives it (calls arrive on the server thread). */
    public interface ClientController {
        void start(int seconds);

        void stop();

        BenchClientSnapshot snapshot();
    }

    private static volatile ClientController controller;

    private BenchClientBridge() {
    }

    public static void install(ClientController clientController) {
        controller = clientController;
    }

    public static boolean available() {
        return controller != null;
    }

    public static void start(int seconds) {
        ClientController c = controller;
        if (c != null) {
            c.start(seconds);
        }
    }

    public static void stop() {
        ClientController c = controller;
        if (c != null) {
            c.stop();
        }
    }

    /** The client's snapshot, or {@code null} without a client. */
    public static BenchClientSnapshot snapshot() {
        ClientController c = controller;
        return c == null ? null : c.snapshot();
    }
}
