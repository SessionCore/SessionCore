<img width="3440" height="1440" alt="sessioncore" src="https://github.com/user-attachments/assets/225f5eff-24b2-4375-a091-df9c3c4a8a9d" />
&nbsp;


SessionCore is a lightweight Java-based wrapper designed to launch a Minecraft server with a custom authentication backend using Authlib Injector. It automates configuration, dependency retrieval, and process management, making it suitable for environments such as Pterodactyl, Docker, or standalone server hosts.

---

## Features

### • Interactive Terminal Setup

On first launch, SessionCore guides the user through a simple TUI (text-based installer):

* Prompts for the custom authentication server URL
* Detects available `.jar` files in the working directory
* Allows selecting a server executable (excluding SessionCore.jar)
* Generates a `SessionCore.yml` configuration file

### • Automatic Authlib Injector Download

SessionCore automatically downloads the official Authlib Injector (v1.2.6) from GitHub Releases.
The binary is not bundled inside SessionCore and is retrieved at runtime to ensure authenticity.

### • Configuration Management

SessionCore creates and maintains:

```
SessionCore.yml
```

Example:

```yaml
authserver: "https://example.com"
serverfile: "server.jar"
```

If the configured server jar is missing, SessionCore will re-run the JAR selection interface.

### • Full Console Passthrough

All Minecraft server output is streamed to the console running SessionCore, along with input forwarding for commands typed by the user.

### • Logging

SessionCore logs all server output to:

```
SessionCore-server.log
```

### • Restart Logic

The wrapper monitors server exit codes:

* Exit code `0` → SessionCore shuts down
* Non-zero exit code → SessionCore automatically restarts the server

This behavior is designed for hosting environments where uptime and crash recovery are required.

### • Hosting Environment Compatibility

* No custom JVM flags
* No ANSI color codes
* Startup command is standard and compliant with panel restrictions

---

## Requirements

* Java 17 or higher
* A Minecraft server `.jar` file placed in the same directory as SessionCore
* Internet connection for automatic dependency download

---

## Installation

1. Download the latest release from the GitHub Releases page.
2. Place `SessionCore.jar` in the same directory as your Minecraft server `.jar`.
3. Run:

```
java -jar SessionCore.jar
```

4. Follow the on-screen setup instructions.

SessionCore will automatically create:

* `SessionCore.yml`
* `meta/` directory
* `authlibinjector.jar`
* `SessionCore-server.log`

---

## Usage

### Start the server:

```
java -jar SessionCore.jar
```

### During runtime:

* Type commands directly into the console; they will be forwarded to the Minecraft server.
* Server logs will appear both in the console and in `SessionCore-server.log`.

---

## File Structure (Generated at Runtime)

```
SessionCore.jar
SessionCore.yml
SessionCore-server.log
meta/
 └── authlibinjector.jar
server.jar        (your selected Minecraft server file)
```

---

## Supported Server Types

SessionCore works with any standard Java-based Minecraft server, including:

* Paper
* Purpur
* Spigot
* Bukkit
* Fabric
* Vanilla
* Most modded servers, as long as they support `java -jar`

---

## Legal Notice

SessionCore does **not** redistribute Authlib Injector.
Instead, it downloads the **official unmodified binary** directly from the Authlib Injector GitHub repository, in accordance with its AGPL license and additional exception permitting usage as a Java Agent.

Users remain responsible for complying with licensing requirements for any external tools or software used alongside SessionCore.

---

## Disclaimer

SessionCore is provided without warranty.
Use at your own risk.
Ensure you comply with your hosting provider’s terms of service before using custom authentication servers.
