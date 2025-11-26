<img width="3440" height="1440" alt="boxauth" src="https://github.com/user-attachments/assets/02b5908e-52f9-4b77-894a-1820fc3bdd06" />
⠀

BoxAuth is a lightweight Java-based wrapper designed to launch a Minecraft server with a custom authentication backend using Authlib Injector. It automates configuration, dependency retrieval, and process management, making it suitable for environments such as Pterodactyl, Docker, or standalone server hosts.

---

## Features

### • Interactive Terminal Setup

On first launch, BoxAuth guides the user through a simple TUI (text-based installer):

* Prompts for the custom authentication server URL
* Detects available `.jar` files in the working directory
* Allows selecting a server executable (excluding BoxAuth.jar)
* Generates a `boxauth.yml` configuration file

### • Automatic Authlib Injector Download

BoxAuth automatically downloads the official Authlib Injector (v1.2.6) from GitHub Releases.
The binary is not bundled inside BoxAuth and is retrieved at runtime to ensure authenticity.

### • Configuration Management

BoxAuth creates and maintains:

```
boxauth.yml
```

Example:

```yaml
authserver: "https://example.com"
serverfile: "server.jar"
```

If the configured server jar is missing, BoxAuth will re-run the JAR selection interface.

### • Full Console Passthrough

All Minecraft server output is streamed to the console running BoxAuth, along with input forwarding for commands typed by the user.

### • Logging

BoxAuth logs all server output to:

```
boxauth-server.log
```

### • Restart Logic

The wrapper monitors server exit codes:

* Exit code `0` → BoxAuth shuts down
* Non-zero exit code → BoxAuth automatically restarts the server

This behavior is designed for hosting environments where uptime and crash recovery are required.

### • Hosting Environment Compatibility

* No custom JVM flags
* No ANSI color codes
* Startup command is standard and compliant with panel restrictions

---

## Requirements

* Java 17 or higher
* A Minecraft server `.jar` file placed in the same directory as BoxAuth
* Internet connection for automatic dependency download

---

## Installation

1. Download the latest release from the GitHub Releases page.
2. Place `BoxAuth.jar` in the same directory as your Minecraft server `.jar`.
3. Run:

```
java -jar BoxAuth.jar
```

4. Follow the on-screen setup instructions.

BoxAuth will automatically create:

* `boxauth.yml`
* `meta/` directory
* `authlibinjector.jar`
* `boxauth-server.log`

---

## Usage

### Start the server:

```
java -jar BoxAuth.jar
```

### During runtime:

* Type commands directly into the console; they will be forwarded to the Minecraft server.
* Server logs will appear both in the console and in `boxauth-server.log`.

---

## File Structure (Generated at Runtime)

```
BoxAuth.jar
boxauth.yml
boxauth-server.log
meta/
 └── authlibinjector.jar
server.jar        (your selected Minecraft server file)
```

---

## Supported Server Types

BoxAuth works with any standard Java-based Minecraft server, including:

* Paper
* Purpur
* Spigot
* Bukkit
* Fabric
* Vanilla
* Most modded servers, as long as they support `java -jar`

---

## Legal Notice

BoxAuth does **not** redistribute Authlib Injector.
Instead, it downloads the **official unmodified binary** directly from the Authlib Injector GitHub repository, in accordance with its AGPL license and additional exception permitting usage as a Java Agent.

Users remain responsible for complying with licensing requirements for any external tools or software used alongside BoxAuth.

---

## Disclaimer

BoxAuth is provided without warranty.
Use at your own risk.
Ensure you comply with your hosting provider’s terms of service before using custom authentication servers.
