# Anonymous Chat Application
This project is designed as a term project for Computer Networks course. It enables users to communicate on a secure chat. All the
transferred data are encrypted with RSA encrpytion. Additionally, IP and MAC adresses are changed to enable fully anonimity.

**Important Note**: This project is designed for devices working on the same local network aimed to learning and prototyping network anonimity. Changing IP and MAC adresses are not secure operations.

## Installation
Clone or download the repository to your local machine.

## Dependencies
Application uses following dependincies:

- Java 17 or higher
- Python 3.x
- Scapy library

You can install Python's scapy library using pip:

```bash
pip install scapy
```

## Running the application
Run the following commands in the src file:

```bash
javac Main.java
java Main.java
```

## How it works
### P2P Architecture

- **Decentralized design**: No central server
- **Port listening**: All peers listen on same port for incoming messages

### Key Exchange & Encryption

1. **User joins**: Broadcasts username + RSA public key
2. **Key storage**: Peers store public keys in a map
3. **Message encryption**: Sender encrypts with recipients' public key
4. **Message decryption**: Recipient decrypts with their private key

### Message Transfer

- **Protocol**: Custom application-layer protocol (see format below)
- **Encryption**: RSA encryption on all packets
- **Fragmentation**: Automatic splitting for messages >128 bytes
- **Transport**: UDP-based communication
- **Packet manipulation**: Scapy library for low-level operations

### Packet Structure

`<id fragmentedBit messageType nickname message>`

| Field | Description |
|-------|-------------|
| id | Unique message id |
| fragmentedBit | Indicates if message is fragmented (T -> fragmented, F -> not fragmented) |
| messageType | Type of message (MSG = chat message, NCK = new user broadcasts their username and pk) |
| nickname | Sender's nickname |
| message | message content |
