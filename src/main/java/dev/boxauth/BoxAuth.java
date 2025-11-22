package dev.boxauth;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class BoxAuth {

    private static final String CONFIG_FILE = "boxauth.yml";
    private static final String META_DIR = "meta";
    private static final String AUTHLIB_PATH = META_DIR + "/authlibinjector.jar";
    private static final String LOG_FILE = "boxauth-server.log";

    private static final String AUTHLIB_URL =
            "https://github.com/yushijinhun/authlib-injector/releases/download/v1.2.6/authlib-injector-1.2.6.jar";

    // Current MC server process
    private static volatile Process runningProcess;

    public static void main(String[] args) throws Exception {
        println("[BoxAuth] Starting BoxAuth wrapper...");

        ensureMetaDir();
        downloadAuthlib();

        Map<String, Object> config = loadOrSetupConfig();
        String authServer = config.get("authserver").toString();
        String serverFile = config.get("serverfile").toString();

        println("[BoxAuth] Using server JAR: " + serverFile);
        println("[BoxAuth] Using auth server: " + authServer);

        startInputThread();

        while (true) {
            Process p = startServer(serverFile, authServer);
            int exit = p.waitFor();
            runningProcess = null;

            println("[BoxAuth] Server exit code: " + exit);

            if (exit == 0) {
                println("[BoxAuth] Clean shutdown detected. Exiting BoxAuth...");
                break;
            } else {
                println("[BoxAuth] Non-zero exit code. Restarting server...");
            }
        }

        println("[BoxAuth] Wrapper finished.");
    }

    // --------------------------------------------------------------------
    // Setup & Config
    // --------------------------------------------------------------------

    private static void ensureMetaDir() {
        File f = new File(META_DIR);
        if (!f.exists()) {
            f.mkdirs();
            println("[BoxAuth] Created meta directory.");
        }
    }

    private static void downloadAuthlib() throws IOException {
        File file = new File(AUTHLIB_PATH);
        if (file.exists()) {
            println("[BoxAuth] Authlib Injector exists.");
            return;
        }

        println("[BoxAuth] Downloading Authlib Injector...");
        try (InputStream in = new URL(AUTHLIB_URL).openStream()) {
            Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        println("[BoxAuth] Download complete.");
    }

    private static Map<String, Object> loadOrSetupConfig() throws Exception {
        File f = new File(CONFIG_FILE);
        Yaml yaml = new Yaml();

        if (!f.exists()) {
            return runFirstTimeSetup(yaml);
        }

        try (FileInputStream in = new FileInputStream(f)) {
            Map<String, Object> cfg = yaml.load(in);
            if (cfg == null) cfg = new HashMap<>();

            if (!cfg.containsKey("serverfile") || !new File(cfg.get("serverfile").toString()).exists()) {
                println("[BoxAuth] Server file missing. Re-running JAR picker...");
                String jar = chooseServerJar();
                cfg.put("serverfile", jar);
                saveConfig(cfg, yaml);
            }

            return cfg;
        }
    }

    private static Map<String, Object> runFirstTimeSetup(Yaml yaml) throws Exception {
        println("[BoxAuth] No config found — running installer...");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        print("Enter auth server URL: ");
        String auth = br.readLine().trim();

        String jar = chooseServerJar();

        Map<String, Object> cfg = new HashMap<>();
        cfg.put("authserver", auth);
        cfg.put("serverfile", jar);

        saveConfig(cfg, yaml);
        return cfg;
    }

    private static void saveConfig(Map<String, Object> cfg, Yaml yaml) throws Exception {
        try (FileWriter fw = new FileWriter(CONFIG_FILE)) {
            yaml.dump(cfg, fw);
        }
        println("[BoxAuth] Saved boxauth.yml");
    }

    // --------------------------------------------------------------------
    // JAR Selection TUI
    // --------------------------------------------------------------------

    private static String chooseServerJar() throws Exception {
        List<File> jars = listServerJars();

        if (jars.isEmpty()) {
            println("[BoxAuth] No server JARs found. Put your server.jar in this directory.");
            System.exit(0);
        }

        println("\nChoose your Minecraft server JAR:");
        for (int i = 0; i < jars.size(); i++) {
            println(" " + (i + 1) + ") " + jars.get(i).getName());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int choice = -1;

        while (choice < 1 || choice > jars.size()) {
            print("Enter choice number: ");
            try {
                choice = Integer.parseInt(br.readLine().trim());
            } catch (Exception ignored) {}
        }

        File selected = jars.get(choice - 1);
        println("[BoxAuth] Selected: " + selected.getName());
        return selected.getName();
    }

    private static List<File> listServerJars() throws Exception {
        File current = new File(".");
        File[] found = current.listFiles((dir, name) -> name.endsWith(".jar"));
        List<File> list = new ArrayList<>();
        if (found == null) return list;

        File self = getSelfJar();

        for (File f : found) {
            if (self != null && f.getCanonicalFile().equals(self)) continue;
            list.add(f);
        }
        return list;
    }

    private static File getSelfJar() {
        try {
            String path = BoxAuth.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            File f = new File(path);
            if (f.isFile() && f.getName().endsWith(".jar")) return f.getCanonicalFile();
        } catch (Exception ignored) {}
        return null;
    }

    // --------------------------------------------------------------------
    // Server Process
    // --------------------------------------------------------------------

    private static Process startServer(String serverFile, String authServer) throws IOException {
        println("[BoxAuth] Starting server...");

        ProcessBuilder pb = new ProcessBuilder(
                "java",
                "-javaagent:" + AUTHLIB_PATH + "=" + authServer,
                "-jar",
                serverFile,
                "nogui"
        );

        pb.redirectErrorStream(true);
        Process p = pb.start();
        runningProcess = p;

        Thread out = new Thread(() -> pipeOutput(p));
        out.setDaemon(true);
        out.start();

        return p;
    }

    private static void pipeOutput(Process p) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter logWriter = new BufferedWriter(
                     new FileWriter(LOG_FILE, true))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String formatted = "[SERVER] " + line;
                println(formatted);

                logWriter.write(formatted);
                logWriter.newLine();
                logWriter.flush();
            }
        } catch (Exception ignored) {}
    }

    // --------------------------------------------------------------------
    // Input forwarding
    // --------------------------------------------------------------------

    private static void startInputThread() {
        Thread t = new Thread(() -> {
            try (BufferedReader console = new BufferedReader(
                    new InputStreamReader(System.in, StandardCharsets.UTF_8))) {

                String cmd;
                while ((cmd = console.readLine()) != null) {
                    cmd = cmd.trim();
                    if (cmd.isEmpty()) continue;
                    Process p = runningProcess;
                    if (p == null) continue;

                    try {
                        OutputStream os = p.getOutputStream();
                        synchronized (os) {
                            os.write((cmd + "\n").getBytes(StandardCharsets.UTF_8));
                            os.flush();
                        }
                    } catch (Exception ignored) {}
                }

            } catch (Exception ignored) {}
        });

        t.setDaemon(true);
        t.start();
    }

    // --------------------------------------------------------------------
    // Logging helpers
    // --------------------------------------------------------------------

    private static void println(String s) {
        System.out.println(s);
    }

    private static void print(String s) {
        System.out.print(s);
    }
}
