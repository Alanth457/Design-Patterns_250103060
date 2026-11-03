public class Task5Runner {
    static class UserSession {
        String username;
        String role;
        UserSession(String username, String role) {
            this.username = username;
            this.role = role;
        }
    }

    interface StorageDriver {
        byte[] loadRawBlob(String path);
        String getStorageType();
    }

    static class S3StorageDriver implements StorageDriver {
        public byte[] loadRawBlob(String path) {
            System.out.println("[AWS S3] Downloading: " + path);
            return new byte[] {1, 2, 3, 4};
        }
        public String getStorageType() { return "AMAZON_S3_CLOUD"; }
    }

    static class NfsStorageDriver implements StorageDriver {
        public byte[] loadRawBlob(String path) {
            System.out.println("[NFS] Reading: " + path);
            return new byte[] {1, 2, 3, 4};
        }
        public String getStorageType() { return "LOCAL_NFS_CLUSTER"; }
    }

    // Proxy checks the user's role before storage access.
    static class StorageDriverProxy implements StorageDriver {
        private StorageDriver driver;
        private UserSession user;
        StorageDriverProxy(StorageDriver driver, UserSession user) {
            this.driver = driver;
            this.user = user;
        }
        public byte[] loadRawBlob(String path) {
            if (user.role.equalsIgnoreCase("GUEST")) {
                System.out.println("[PROXY BLOCKED] Access denied for " + user.username);
                return new byte[0];
            }
            System.out.println("[PROXY AUDIT] Authorized user: " + user.username);
            return driver.loadRawBlob(path);
        }
        public String getStorageType() {
            return "PROXY_GUARDED(" + driver.getStorageType() + ")";
        }
    }

    // Bridge: media asset does not depend on a specific storage type.
    static abstract class DigitalAsset {
        protected StorageDriver driver;
        protected String path;
        DigitalAsset(StorageDriver driver, String path) {
            this.driver = driver;
            this.path = path;
        }
        abstract void render();
    }

    static class VideoAsset extends DigitalAsset {
        VideoAsset(StorageDriver d, String p) { super(d, p); }
        void render() {
            System.out.println("Initializing Video Player: " + path);
            byte[] data = driver.loadRawBlob(path);
            if (data.length > 0)
                System.out.println("SUCCESS: Video loaded " + data.length + " bytes via " + driver.getStorageType());
            else
                System.out.println("FAILURE: Video buffer empty.");
        }
    }

    static class AudioAsset extends DigitalAsset {
        AudioAsset(StorageDriver d, String p) { super(d, p); }
        void render() {
            System.out.println("Initializing Audio Player: " + path);
            byte[] data = driver.loadRawBlob(path);
            if (data.length > 0)
                System.out.println("SUCCESS: Audio loaded " + data.length + " bytes");
            else
                System.out.println("FAILURE: Audio stream inaccessible.");
        }
    }

    public static void main(String[] args) {
        System.out.println(">>> TASK 5: BRIDGE + PROXY <<<");

        StorageDriver adminStorage = new StorageDriverProxy(
                new S3StorageDriver(), new UserSession("sufyan", "ADMIN"));
        DigitalAsset video = new VideoAsset(adminStorage, "trailers/video.mp4");
        video.render();

        System.out.println();
        StorageDriver guestStorage = new StorageDriverProxy(
                new NfsStorageDriver(), new UserSession("anonymous", "GUEST"));
        DigitalAsset audio = new AudioAsset(guestStorage, "music/audio.flac");
        audio.render();
    }
}
