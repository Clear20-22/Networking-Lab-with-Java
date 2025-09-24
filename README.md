# 🌐 Networking Lab with Java

[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen.svg)]()

A comprehensive networking laboratory project demonstrating **multithreaded server-client architecture** in Java. This project includes two major implementations: a secure banking system and an enhanced multi-client chat system.

## 📁 Project Components

### 🏦 Banking System (`Server-Client-Multithreade-Bank/`)
A secure banking application with concurrent client handling, transaction management, and data persistence.

### 💬 Chat System (`Chat Server client/`)
An enhanced multi-client chat system with real-time messaging, user management, and collaborative features.

## ✨ Banking System Features

- 🔐 **Secure Authentication**: PIN-based card authentication system
- 💰 **Banking Operations**: Balance inquiry and secure withdrawals
- 🧵 **Multithreaded Server**: Concurrent handling of multiple clients
- 🔄 **Transaction Safety**: Duplicate transaction prevention with unique TXIDs
- 💾 **Data Persistence**: Automatic saving of client data to file
- 📊 **Real-time Logging**: Comprehensive server-side transaction logging
- 🚀 **Easy Deployment**: Simple scripts for running multiple clients

## ✨ Chat System Features

- 🚀 **Multi-Client Support**: Handle unlimited simultaneous connections
- 👤 **User Authentication**: Unique username system with validation
- 💬 **Real-Time Messaging**: Instant message broadcasting to all users
- 🕒 **Timestamped Messages**: All messages include timestamps
- 👥 **Online User List**: See who's currently online
- 🛡️ **Thread-Safe Operations**: Concurrent data structures and synchronization
- 🎨 **Beautiful UI**: Enhanced console interface with emojis
- ⚡ **Graceful Shutdown**: Proper connection handling and cleanup

## 🏗️ Architecture

### Server Components
- **Server_17_23.java**: Main server class handling client connections
- **ClientHandler.java**: Thread-based client request processor
- **Data Persistence**: ConcurrentHashMap for thread-safe operations

### Client Components
- **Client_17_23.java**: Interactive CLI client for banking operations
- **MultiClientLauncher.java**: Utility for launching multiple client instances

### Communication Protocol
```
AUTH:card:pin → AUTH_OK/AUTH_FAIL
BALANCE_REQ → BALANCE_RES:amount
WITHDRAW:txid:amount → WITHDRAW_OK/INSUFFICIENT_FUNDS/ALREADY_WITHDRAWN
```

## 📋 Prerequisites

- **Java 17+** installed on your system
- Basic understanding of networking concepts
- Terminal/command line access

## 🚀 Quick Start

### Banking System Demo
```bash
# Navigate to banking system
cd Server-Client-Multithreade-Bank

# Compile and run
javac *.java
java Server_17_23 &
java Client_17_23
```

### Chat System Demo
```bash
# Navigate to chat system
cd "Chat Server client"

# Use the interactive demo
./demo.sh
```

### Manual Chat Setup
```bash
# Terminal 1 - Start Server
cd "Chat Server client"
java server

# Terminal 2 - Start Client 1
java client

# Terminal 3 - Start Client 2
java client1
```

Or launch multiple clients using the launcher:
```bash
java MultiClientLauncher 3  # Launches 3 clients
```

## 📖 Usage Guide

### Server Operations
The server automatically:
- Loads client data from `clients.txt`
- Handles multiple concurrent connections
- Processes authentication and transactions
- Saves updated balances after transactions
- Logs all operations to console

### Client Operations
1. **Authentication**: Enter card number and PIN
2. **Menu Options**:
   - Check Balance
   - Withdraw Funds
   - Exit

### Sample Client Data (`clients.txt`)
```
1235,1234,600
1234,1234,45
5678,5678,1000
```
Format: `card_number,pin,initial_balance`

## 📁 Project Structure

```
Networking-Lab-with-Java/
├── Server-Client-Multithreade-Bank/
│   ├── Server_17_23.java          # Main server implementation
│   ├── Client_17_23.java          # Client application
│   ├── MultiClientLauncher.java   # Multi-client launcher
│   ├── clients.txt                # Client database
│   ├── run_clients.sh             # Bash script for multiple clients
│   ├── *.class                    # Compiled Java classes
│   └── README.md                  # This file
└── .git/                          # Git repository
```

## 🔧 Configuration

### Server Configuration
- **Port**: 6002 (configurable in Server_17_23.java)
- **Data File**: clients.txt (automatically created/updated)

### Client Configuration
- **Server Address**: localhost (configurable in Client_17_23.java)
- **Server Port**: 6002

## 🛡️ Security Features

- **PIN Authentication**: Secure login system
- **Transaction IDs**: Unique identifiers prevent duplicate transactions
- **Balance Validation**: Prevents overdrafts
- **Session Management**: Proper logout handling
- **Thread Safety**: ConcurrentHashMap for data integrity

## 📊 Logging & Monitoring

The server provides comprehensive logging:
- Client connection/disconnection events
- Authentication attempts (success/failure)
- Transaction details (withdrawals, balances)
- Error handling and exceptions

## 🧪 Testing

### Manual Testing
1. Start the server
2. Launch multiple clients
3. Test concurrent operations:
   - Multiple authentications
   - Simultaneous balance checks
   - Concurrent withdrawals

### Automated Testing
```bash
# Compile and run with test data
javac *.java
java Server_17_23 &
sleep 2
java MultiClientLauncher 5
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Built as part of Networking Laboratory coursework
- Demonstrates core networking and multithreading concepts
- Educational resource for Java networking programming

## 📞 Support

For questions or issues:
- Open an issue on GitHub
- Review the code comments for implementation details
- Check server logs for debugging information

---

**Happy Networking! 🚀**