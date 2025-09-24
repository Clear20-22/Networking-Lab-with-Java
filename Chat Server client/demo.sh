#!/bin/bash

# Enhanced Chat Server Client Demo Script
# This script demonstrates the improved multi-client chat system

echo "╔══════════════════════════════════════════════════════════════╗"
echo "║                🌟 ENHANCED CHAT SYSTEM DEMO 🌟               ║"
echo "║                     Multi-Client Edition                     ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# Check if Java is installed
if ! command -v java &> /dev/null; then
    print_error "Java is not installed. Please install Java 17+ to run this demo."
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    print_warning "Java version $JAVA_VERSION detected. This application requires Java 17+."
    print_warning "Some features may not work correctly."
fi

print_info "Java version: $(java -version 2>&1 | head -n 1)"

# Compile all Java files
echo ""
print_info "Compiling Java files..."
if javac *.java; then
    print_status "All Java files compiled successfully!"
else
    print_error "Compilation failed. Please check for syntax errors."
    exit 1
fi

echo ""
echo "🎯 Available Demo Options:"
echo "═══════════════════════════════════════════════"
echo "1. 🚀 Start Server Only"
echo "2. 👤 Start Single Client"
echo "3. 👥 Start Multiple Clients (2 clients)"
echo "4. 👨‍👩‍👧‍👦 Start Multiple Clients (3 clients)"
echo "5. 📊 Show System Status"
echo "6. 🧹 Clean compiled files"
echo "7. ❓ Show Help"
echo "8. 🚪 Exit"
echo ""

while true; do
    read -p "Choose an option (1-8): " choice

    case $choice in
        1)
            echo ""
            print_info "Starting Chat Server..."
            print_info "Server will run in background. Use Ctrl+C to stop."
            echo ""
            java server &
            SERVER_PID=$!
            print_status "Server started with PID: $SERVER_PID"
            print_info "Press Enter to continue..."
            read
            ;;
        2)
            echo ""
            print_info "Starting Single Chat Client..."
            echo ""
            java client1
            ;;
        3)
            echo ""
            print_info "Starting 2 Chat Clients..."
            print_info "Client 1 will start first, then Client 2 after 3 seconds."
            echo ""

            # Start first client in background
            java client1 &
            CLIENT1_PID=$!

            # Wait a bit then start second client
            sleep 3
            java client2 &
            CLIENT2_PID=$!

            print_status "Client 1 started (PID: $CLIENT1_PID)"
            print_status "Client 2 started (PID: $CLIENT2_PID)"
            print_info "Both clients are now running. Switch between terminal windows to interact."
            print_info "Press Enter to continue..."
            read
            ;;
        4)
            echo ""
            print_info "Starting 3 Chat Clients for full demo..."
            print_info "This will simulate a busy chat room!"
            echo ""

            # Start clients with delays
            java client &
            sleep 2
            java client1 &
            sleep 2
            java client2 &

            print_status "All 3 clients started!"
            print_info "Try these commands in different clients:"
            print_info "  • Type 'users' to see online users"
            print_info "  • Type 'help' for available commands"
            print_info "  • Send messages to chat with others"
            print_info "Press Enter to continue..."
            read
            ;;
        5)
            echo ""
            print_info "System Status:"
            echo "═══════════════════════════════════════════════"

            # Check if server is running
            if pgrep -f "java server" > /dev/null; then
                print_status "Chat Server is running"
            else
                print_warning "Chat Server is not running"
            fi

            # Check for running clients
            CLIENT_COUNT=$(pgrep -f "java client" | wc -l)
            if [ "$CLIENT_COUNT" -gt 0 ]; then
                print_status "$CLIENT_COUNT Chat Client(s) are running"
            else
                print_warning "No Chat Clients are running"
            fi

            # Show Java version
            print_info "Java Version: $(java -version 2>&1 | head -n 1)"

            # Show compiled files
            CLASS_COUNT=$(ls *.class 2>/dev/null | wc -l)
            print_info "Compiled Classes: $CLASS_COUNT"

            echo ""
            print_info "Press Enter to continue..."
            read
            ;;
        6)
            echo ""
            print_info "Cleaning compiled files..."
            rm -f *.class
            print_status "All compiled files removed!"
            ;;
        7)
            echo ""
            echo "📚 Enhanced Chat System Help:"
            echo "═══════════════════════════════════════════════"
            echo ""
            echo "🎯 FEATURES:"
            echo "  • Multi-client support (unlimited clients)"
            echo "  • User authentication with unique usernames"
            echo "  • Real-time message broadcasting"
            echo "  • Timestamped messages"
            echo "  • Online user list"
            echo "  • Graceful connection handling"
            echo "  • Thread-safe operations"
            echo ""
            echo "💬 COMMANDS:"
            echo "  • 'users' - Show all online users"
            echo "  • 'help'  - Show available commands"
            echo "  • 'quit'  - Disconnect from chat"
            echo "  • Any other text - Send message to all users"
            echo ""
            echo "🚀 USAGE:"
            echo "  1. Start the server first (Option 1)"
            echo "  2. Start one or more clients (Options 2-4)"
            echo "  3. Enter a unique username when prompted"
            echo "  4. Start chatting!"
            echo ""
            print_info "Press Enter to continue..."
            read
            ;;
        8)
            echo ""
            print_info "Stopping all running processes..."

            # Kill any running Java processes related to our chat system
            pkill -f "java server" 2>/dev/null && print_status "Server stopped"
            pkill -f "java client" 2>/dev/null && print_status "Clients stopped"

            print_info "Thank you for using the Enhanced Chat System! 👋"
            exit 0
            ;;
        *)
            print_error "Invalid option. Please choose 1-8."
            ;;
    esac

    echo ""
    echo "🎯 Available Demo Options:"
    echo "═══════════════════════════════════════════════"
    echo "1. 🚀 Start Server Only"
    echo "2. 👤 Start Single Client"
    echo "3. 👥 Start Multiple Clients (2 clients)"
    echo "4. 👨‍👩‍👧‍👦 Start Multiple Clients (3 clients)"
    echo "5. 📊 Show System Status"
    echo "6. 🧹 Clean compiled files"
    echo "7. ❓ Show Help"
    echo "8. 🚪 Exit"
    echo ""
done