#!/bin/bash

# BitTorrent Test Repository Initialization Script
# This script creates test directories and files for BitTorrent testing

echo "=== BitTorrent Test Repository Initialization ==="
echo

# Create peer directories
echo "Creating peer directories..."
mkdir -p peer1
mkdir -p peer2
mkdir -p peer3
mkdir -p peer4

# Function to create a file with specific content
create_file() {
    local filepath=$1
    local content=$2
    echo "$content" > "$filepath"
    echo "Created: $filepath (MD5: $(md5sum "$filepath" | cut -d' ' -f1))"
}

# Function to create a large file
create_large_file() {
    local filepath=$1
    local size_mb=$2
    dd if=/dev/urandom of="$filepath" bs=1M count=$size_mb 2>/dev/null
    echo "Created: $filepath (${size_mb}MB, MD5: $(md5sum "$filepath" | cut -d' ' -f1))"
}

echo
echo "=== Scenario 1: Basic File Sharing ==="
create_file "peer1/file1.txt" "This is file 1 from peer 1"
create_file "peer1/file2.txt" "This is file 2 from peer 1"
create_file "peer2/file3.txt" "This is file 3 from peer 2"
echo "Peer3 is empty (for testing downloads)"

echo
echo "=== Scenario 2: Shared Files (Same Content) ==="
create_file "peer1/shared.txt" "This is a shared file with same content"
create_file "peer2/shared.txt" "This is a shared file with same content"

echo
echo "=== Scenario 3: Hash Conflict Files ==="
create_file "peer1/conflict.txt" "This is version 1 of the conflict file"
create_file "peer2/conflict.txt" "This is version 2 of the conflict file - different content!"

echo
echo "=== Additional Test Files ==="

# Various file types
create_file "peer1/document.pdf" "PDF_CONTENT_SIMULATION_1234567890"
create_file "peer1/image.jpg" "JPG_IMAGE_DATA_SIMULATION"
create_file "peer2/video.mp4" "MP4_VIDEO_DATA_SIMULATION"
create_file "peer2/music.mp3" "MP3_AUDIO_DATA_SIMULATION"

# Files with special names
create_file "peer1/file with spaces.txt" "This file has spaces in its name"
create_file "peer1/file(1).txt" "This file has parentheses"
create_file "peer2/file[bracket].txt" "This file has brackets"
create_file "peer2/file-dash_underscore.txt" "This file has dashes and underscores"

# Empty file
touch "peer1/empty.txt"
echo "Created: peer1/empty.txt (empty file)"

# Multiple files with similar names
create_file "peer1/test1.txt" "Test file 1"
create_file "peer1/test2.txt" "Test file 2"
create_file "peer1/test3.txt" "Test file 3"
create_file "peer2/test4.txt" "Test file 4"
create_file "peer2/test5.txt" "Test file 5"

# Files for performance testing
echo
echo "=== Performance Test Files ==="
create_large_file "peer1/medium_file.bin" 10
create_large_file "peer2/large_file.bin" 50

# Create some zip files
echo "Creating zip files..."
cd peer1
echo "Zip content 1" > temp1.txt
echo "Zip content 2" > temp2.txt
zip archive1.zip temp1.txt temp2.txt >/dev/null 2>&1
rm temp1.txt temp2.txt
cd ..
echo "Created: peer1/archive1.zip"

cd peer2
echo "Archive 2 content" > temp.txt
zip archive2.zip temp.txt >/dev/null 2>&1
rm temp.txt
cd ..
echo "Created: peer2/archive2.zip"

# Create a specific test case for the example in the document
create_file "peer1/rick_and_morty_season1.zip" "Rick and Morty Season 1 - Simulated Content"
create_file "peer2/breaking_bad.mkv" "Breaking Bad - Simulated Video Content"

# Files for send/receive tracking
create_file "peer1/tracked1.txt" "This file will be tracked when sent"
create_file "peer2/tracked2.txt" "This file will be tracked when received"

# Very long filename (but within filesystem limits)
long_name="very_long_filename_that_tests_the_system_handling_of_extended_names_1234567890"
create_file "peer1/${long_name}.txt" "File with very long name"

# Unicode filename (if filesystem supports it)
create_file "peer2/файл.txt" "File with unicode name" 2>/dev/null || echo "Unicode filename not supported"

# Create summary files
echo
echo "=== Creating Summary Files ==="

# Create inventory files for each peer
for i in 1 2 3 4; do
    if [ -d "peer$i" ] && [ "$(ls -A peer$i)" ]; then
        echo "Files in peer$i:" > "peer$i/inventory.txt"
        ls -la peer$i >> "peer$i/inventory.txt"
        echo >> "peer$i/inventory.txt"
        echo "MD5 Hashes:" >> "peer$i/inventory.txt"
        for file in peer$i/*; do
            if [ -f "$file" ] && [ "$(basename "$file")" != "inventory.txt" ]; then
                echo "$(basename "$file"): $(md5sum "$file" | cut -d' ' -f1)" >> "peer$i/inventory.txt"
            fi
        done
    fi
done

echo
echo "=== Repository Initialization Complete ==="
echo
echo "Directory structure:"
tree -L 2 2>/dev/null || (echo "peer1/"; ls peer1/; echo; echo "peer2/"; ls peer2/; echo; echo "peer3/"; ls peer3/; echo; echo "peer4/"; ls peer4/)

echo
echo "=== Quick Test Commands ==="
echo "1. Start tracker:"
echo "   java -cp \"lib/*:out\" tracker.TrackerMain 8080"
echo
echo "2. Start peers:"
echo "   java -cp \"lib/*:out\" peer.PeerMain 127.0.0.1:8081 127.0.0.1:8080 ./peer1"
echo "   java -cp \"lib/*:out\" peer.PeerMain 127.0.0.1:8082 127.0.0.1:8080 ./peer2"
echo "   java -cp \"lib/*:out\" peer.PeerMain 127.0.0.1:8083 127.0.0.1:8080 ./peer3"
echo
echo "3. Test downloads:"
echo "   On Peer3: download file1.txt"
echo "   On Peer3: download file3.txt"
echo "   On Peer1: download breaking_bad.mkv"
echo
echo "4. Test hash conflict:"
echo "   On Peer3: download conflict.txt"
echo
echo "5. Test file not found:"
echo "   On any peer: download nonexistent.txt"
echo
echo "=== Setup Complete! ==="