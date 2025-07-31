#!/bin/bash

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

clear
echo -e "${BLUE}=== BitTorrent Interactive Testing Guide ===${NC}"
echo
echo "This script will guide you through testing the BitTorrent implementation."
echo "You'll need to manually enter commands in the appropriate terminals."
echo
echo -e "${YELLOW}Step 1: Start the components${NC}"
echo "Open 4 terminal windows and run these commands:"
echo
echo "Terminal 1 (Tracker):"
echo -e "${GREEN}java -cp \"lib/*:out\" tracker.TrackerMain 8080${NC}"
echo
echo "Terminal 2 (Peer 1):"
echo -e "${GREEN}java -cp \"lib/*:out\" peer.PeerMain 127.0.0.1:8081 127.0.0.1:8080 ./peer1${NC}"
echo
echo "Terminal 3 (Peer 2):"
echo -e "${GREEN}java -cp \"lib/*:out\" peer.PeerMain 127.0.0.1:8082 127.0.0.1:8080 ./peer2${NC}"
echo
echo "Terminal 4 (Peer 3):"
echo -e "${GREEN}java -cp \"lib/*:out\" peer.PeerMain 127.0.0.1:8083 127.0.0.1:8080 ./peer3${NC}"
echo
read -p "Press Enter when all components are running..."

clear
echo -e "${BLUE}=== Test Sequence ===${NC}"
echo
echo -e "${YELLOW}Test 1: Initial State Check${NC}"
echo "Run these commands to check initial state:"
echo
echo "In Peer 1 terminal:"
echo -e "${GREEN}list${NC}"
echo "Expected: Should show file1.txt, file2.txt, and other files"
echo
echo "In Peer 2 terminal:"
echo -e "${GREEN}list${NC}"
echo "Expected: Should show file3.txt and other files"
echo
echo "In Peer 3 terminal:"
echo -e "${GREEN}list${NC}"
echo "Expected: Repository is empty."
echo
echo "In Tracker terminal:"
echo -e "${GREEN}list_peers${NC}"
echo "Expected: Shows all three peers (127.0.0.1:8081, 8082, 8083)"
echo
read -p "Press Enter after checking initial state..."

clear
echo -e "${YELLOW}Test 2: Basic Download${NC}"
echo "In Peer 3 terminal, run:"
echo -e "${GREEN}download file1.txt${NC}"
echo "Expected: File downloaded successfully: file1.txt"
echo
echo "Then run:"
echo -e "${GREEN}list${NC}"
echo "Expected: file1.txt should appear with its MD5 hash"
echo
read -p "Press Enter after completing basic download test..."

clear
echo -e "${YELLOW}Test 3: File Already Exists${NC}"
echo "In Peer 3 terminal, run again:"
echo -e "${GREEN}download file1.txt${NC}"
echo "Expected: You already have the file!"
echo
read -p "Press Enter to continue..."

clear
echo -e "${YELLOW}Test 4: File Not Found${NC}"
echo "In any peer terminal, run:"
echo -e "${GREEN}download nonexistent.txt${NC}"
echo "Expected: No peer has the file!"
echo
read -p "Press Enter to continue..."

clear
echo -e "${YELLOW}Test 5: Hash Conflict${NC}"
echo "In Peer 3 terminal, run:"
echo -e "${GREEN}download conflict.txt${NC}"
echo "Expected: Multiple hashes found!"
echo "(This is because peer1 and peer2 have different versions of conflict.txt)"
echo
read -p "Press Enter to continue..."

clear
echo -e "${YELLOW}Test 6: Multiple Downloads${NC}"
echo "In Peer 3 terminal, run these commands quickly:"
echo -e "${GREEN}download file2.txt${NC}"
echo -e "${GREEN}download file3.txt${NC}"
echo -e "${GREEN}download document.pdf${NC}"
echo
echo "Then check with:"
echo -e "${GREEN}list${NC}"
echo "Expected: All files should be downloaded successfully"
echo
read -p "Press Enter to continue..."

clear
echo -e "${YELLOW}Test 7: Tracker File Listing${NC}"
echo "In Tracker terminal, run:"
echo -e "${GREEN}list_files 127.0.0.1:8081${NC}"
echo "Expected: Shows all files in peer1"
echo
echo -e "${GREEN}list_files 127.0.0.1:8083${NC}"
echo "Expected: Shows files now in peer3 after downloads"
echo
read -p "Press Enter to continue..."

clear
echo -e "${YELLOW}Test 8: Send/Receive Tracking${NC}"
echo "In Tracker terminal, run:"
echo -e "${GREEN}get_sends 127.0.0.1:8081${NC}"
echo "Expected: Shows files sent by peer1 to peer3"
echo
echo -e "${GREEN}get_receives 127.0.0.1:8083${NC}"
echo "Expected: Shows files received by peer3 from peer1"
echo
read -p "Press Enter to continue..."

clear
echo -e "${YELLOW}Test 9: File Refresh${NC}"
echo "First, create a new file:"
echo -e "${GREEN}echo 'New test content' > peer1/dynamic_test.txt${NC}"
echo "(Run this in a new terminal)"
echo
echo "In Tracker terminal, run:"
echo -e "${GREEN}list_files 127.0.0.1:8081${NC}"
echo "Expected: New file NOT shown yet"
echo
echo "Then run:"
echo -e "${GREEN}refresh_files${NC}"
echo
echo "And check again:"
echo -e "${GREEN}list_files 127.0.0.1:8081${NC}"
echo "Expected: New file should now appear"
echo
read -p "Press Enter to continue..."

clear
echo -e "${YELLOW}Test 10: Special Filenames${NC}"
echo "In Peer 3 terminal, run:"
echo -e "${GREEN}download file with spaces.txt${NC}"
echo "Expected: File downloaded successfully: file with spaces.txt"
echo
echo -e "${GREEN}download file(1).txt${NC}"
echo "Expected: File downloaded successfully: file(1).txt"
echo
read -p "Press Enter to continue..."

clear
echo -e "${YELLOW}Test 11: Invalid Peer${NC}"
echo "In Tracker terminal, run:"
echo -e "${GREEN}list_files 192.168.1.1:9999${NC}"
echo "Expected: Peer not found."
echo
read -p "Press Enter to continue..."

clear
echo -e "${YELLOW}Test 12: Connection Reset${NC}"
echo "In Tracker terminal, run:"
echo -e "${GREEN}reset_connections${NC}"
echo
echo "Then verify with:"
echo -e "${GREEN}list_peers${NC}"
echo "Expected: All peers should still be listed"
echo
read -p "Press Enter to continue..."

clear
echo -e "${YELLOW}Test 13: Clean Exit${NC}"
echo "Exit each component in order:"
echo
echo "In Peer 3 terminal:"
echo -e "${GREEN}exit${NC}"
echo
echo "In Peer 2 terminal:"
echo -e "${GREEN}exit${NC}"
echo
echo "In Peer 1 terminal:"
echo -e "${GREEN}exit${NC}"
echo
echo "In Tracker terminal:"
echo -e "${GREEN}exit${NC}"
echo
echo "Expected: All components should shut down cleanly"
echo
read -p "Press Enter to finish..."

clear
echo -e "${BLUE}=== Testing Complete ===${NC}"
echo
echo -e "${YELLOW}Summary of what was tested:${NC}"
echo "✓ Basic file downloads"
echo "✓ Error handling (file exists, not found, hash conflict)"
echo "✓ Multiple simultaneous downloads"
echo "✓ Tracker commands (list peers/files, get sends/receives)"
echo "✓ File refresh functionality"
echo "✓ Special filename handling"
echo "✓ Invalid peer handling"
echo "✓ Connection reset"
echo "✓ Clean shutdown"
echo
echo -e "${YELLOW}Cleanup commands:${NC}"
echo "rm -f peer3/*.txt peer3/*.pdf peer1/dynamic_test.txt"
echo
echo "Thank you for testing!"