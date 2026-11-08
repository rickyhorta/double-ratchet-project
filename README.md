# Double Ratchet Messaging System
The main goal is to implement a double ratchet algorithm and test the performance of the updates.

A secure end-to-end encrypted messaging application implementing the Double Ratchet protocol with a modern GUI interface. This system provides forward secrecy and post-compromise security through cryptographic ratcheting.

## 🔐 Features

- **Double Ratchet Protocol**: Implementation of the Signal Protocol's Double Ratchet algorithm
- **End-to-End Encryption**: AES-GCM encryption with perfect forward secrecy
- **Modern GUI**: Dark-themed chat interface with message bubbles
- **Performance Metrics**: Real-time monitoring of encryption/decryption performance
- **Network Communication**: TCP-based peer-to-peer messaging
- **Key Exchange**: Elliptic Curve Diffie-Hellman (ECDH) for initial key agreement

## 🏗️ Architecture

The system consists of several key components:

- **Cryptographic Layer** (`crypto/`)
  - [`DoubleRatchet`](src/main/java/crypto/DoubleRatchet.java): Core Double Ratchet implementation
  - [`AEAD`](src/main/java/crypto/AEAD.java): AES-GCM authenticated encryption
  - [`DiffieHellman`](src/main/java/crypto/DiffieHellman.java): ECDH key agreement
  - [`KDF`](src/main/java/crypto/KDF.java): HMAC-based key derivation

- **Messaging Layer** (`messaging/`)
  - [`NetworkManager`](src/main/java/messaging/NetworkManager.java): TCP communication handling
  - [`MessageHandler`](src/main/java/messaging/MessageHandler.java): Message processing logic
  - [`Message`](src/main/java/messaging/Message.java): Message data structure

- **GUI Layer** (`message_gui/`)
  - [`MessageGUI`](src/main/java/message_gui/MessageGUI.java): Modern Swing-based chat interface

- **Metrics** (`metrics/`)
  - [`PerformanceMetrics`](src/main/java/metrics/PerformanceMetrics.java): Performance monitoring

## 🚀 Getting Started

### Prerequisites

- Java 11 or higher
- Maven 3.6 or higher
- Two network endpoints for testing (can be localhost)

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd cps-double-ratchet
   ```

2. **Build the project**
   ```bash
   mvn clean compile
   ```

3. **Package the application (optional)**
   ```bash
   mvn package
   ```

### Running the Application

The application requires two instances to communicate. Each instance needs unique ports and names.

#### Method 1: Using Maven

**Terminal 1 (Alice [or other name]):**
```bash
mvn exec:java -Dexec.mainClass="Main" -Dexec.args="Alice Bob localhost 8081 8082"
```

**Terminal 2 (Bob [or other name]):**
```bash
mvn exec:java -Dexec.mainClass="Main" -Dexec.args="Bob Alice localhost 8082 8081"
```

#### Method 2: Using JAR

```bash
# Build the JAR with dependencies
mvn package

# Run Alice
java -jar target/double-ratchet-messaging-1.0-SNAPSHOT-jar-with-dependencies.jar Alice Bob localhost 8081 8082

# Run Bob (in another terminal)
java -jar target/double-ratchet-messaging-1.0-SNAPSHOT-jar-with-dependencies.jar Bob Alice localhost 8082 8081
```

### Usage

1. **Start both instances** as shown above
2. **Initiate key exchange**: The initiator (determined by lexicographic name order) types `init`
3. **GUI launches automatically** after successful key exchange
4. **Send messages** using the modern chat interface
5. **View performance stats** using the "STATS" button in the GUI

#### Command Line Arguments

```
<name> <remote_name> <remote_ip> <local_port> <remote_port>
```

- `name`: Your username
- `remote_name`: The other user's name
- `remote_ip`: IP address of the remote peer (use `localhost` for testing)
- `local_port`: Port to listen on
- `remote_port`: Port to connect to on remote peer

## 🔒 Security Features

### Double Ratchet Protocol

The implementation provides:

- **Forward Secrecy**: Past messages remain secure even if current keys are compromised
- **Post-Compromise Security**: Future messages are secure after key compromise recovery
- **Message Ordering**: Handles out-of-order message delivery
- **Missed Messages**: Automatically handles skipped messages

### Cryptographic Algorithms

- **Key Exchange**: ECDH with secp256r1 (NIST P-256)
- **Encryption**: AES-256-GCM
- **Key Derivation**: HMAC-SHA256
- **Random Generation**: SecureRandom for IVs and keys

## 📊 Performance Monitoring

The application includes comprehensive performance metrics:

- Encryption/decryption timing
- Key exchange duration
- Message throughput
- Session statistics

Access metrics through the GUI's "STATS" button or console output.

## 🎨 GUI Features

The modern chat interface includes:

- **Dark Theme**: Professional dark color scheme
- **Message Bubbles**: Distinct styling for sent/received messages
- **Real-time Updates**: Automatic message display
- **System Messages**: Status and error notifications
- **Performance Stats**: Built-in metrics viewer
- **Responsive Design**: Adaptive message bubble sizing

## 🏃‍♂️ Example Session

1. **Start Alice:**
   ```bash
   mvn exec:java -Dexec.mainClass="Main" -Dexec.args="Alice Bob localhost 8081 8082"
   ```

2. **Start Bob:**
   ```bash
   mvn exec:java -Dexec.mainClass="Main" -Dexec.args="Bob Alice localhost 8082 8081"
   ```

3. **In Alice's terminal, type:** `init`

4. **GUI windows open automatically**

5. **Start chatting securely!**

## 🛠️ Development

### Project Structure

```
src/main/java/
├── Main.java                 # Application entry point
├── crypto/                   # Cryptographic implementations
│   ├── DoubleRatchet.java   # Core protocol
│   ├── AEAD.java            # Encryption/decryption
│   ├── DiffieHellman.java   # Key agreement
│   └── KDF.java             # Key derivation
├── messaging/               # Network communication
│   ├── NetworkManager.java # TCP handling
│   ├── MessageHandler.java # Message processing
│   └── Message.java        # Message structure
├── message_gui/            # User interface
│   └── MessageGUI.java     # Chat GUI
└── metrics/                # Performance monitoring
    └── PerformanceMetrics.java
```

### Maven Configuration

The project uses Maven with the following key plugins:

- **exec-maven-plugin**: For running the application
- **maven-assembly-plugin**: For creating JAR with dependencies
- **maven-compiler-plugin**: Java 11 compilation

## 🐛 Troubleshooting

### Common Issues

1. **Connection Refused**
   - Ensure both instances are running
   - Check port numbers are correct and available
   - Verify firewall settings

2. **GUI Not Appearing**
   - Ensure display is available (not running headless)
   - Check Java AWT/Swing support

3. **Key Exchange Fails**
   - Verify network connectivity
   - Check that initiator starts key exchange first
   - Ensure ports are not blocked

4. **Build Errors**
   - Verify Java 11+ is installed
   - Check Maven is properly configured
   - Ensure all dependencies are available

### Debug Mode

The application includes extensive debug logging. Look for `[DEBUG]` messages in the console output to trace execution flow.

## 📝 Protocol Details

### Message Flow

1. **Initial Setup**: Both parties generate ECDH key pairs
2. **Key Exchange**: Initiator sends public key to responder
3. **Key Derivation**: Both derive root key and initial chain keys
4. **Message Exchange**: Encrypted messages with ratcheting
5. **DH Ratchet**: New key pairs generated periodically for forward secrecy

### Message Format

Each message contains:
- **Header**: Public key + message number
- **Ciphertext**: AES-GCM encrypted payload
- **Authentication**: GCM tag for integrity

## 🔗 References

- [Signal Protocol Documentation](https://signal.org/docs/)
- [Double Ratchet Algorithm](https://signal.org/docs/specifications/doubleratchet/)
- [X3DH Key Agreement Protocol](https://signal.org/docs/specifications/x3dh/)
