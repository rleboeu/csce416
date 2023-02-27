import socket
import select
import sys
from _thread import *

MAX_CONNECTIONS = 100
clients = list()

# remove a connection from clients list
def remove_connection(conn: socket.socket) -> None:
    if conn in clients:
        clients.remove(conn)

# print msg from connection to all clients exception connectionf
def broadcast(msg: str, connection: socket.socket) -> None:

    client: socket.socket
    for client in clients:
        if client != connection:
            try:
                client.send(msg)
            except:
                client.close()
                remove_connection(client)


# the client thread
def client(connection: socket.socket, address) -> None: 
    connection.send("Welcome to CockyChat!")

    while True:
        try:
            message = connection.recv(2048)
            if message:
                formatted_message = f"[{address[0]}]: {message}"
                # print message to server
                print(formatted_message)

                # print message to all other clients
                broadcast(formatted_message, connection)
            else:
                remove_connection(connection)
        except:
            continue

def main() -> None:

    # configure server socket settings
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)

    # store ip and port
    ip_addr = str(sys.argv[1])
    port = int(sys.argv[2])

    # bind (or assign) ip address and port to server
    server.bind((ip_addr, port))

    # listen for up to MAX_CONNECTIONS connections
    server.listen(MAX_CONNECTIONS)

    while True:
        conn, addr = server.accept()

        clients.append(conn)

        # notify server of a new connection
        print(f"{addr[0]} connected.")

        start_new_thread(client, (conn,addr))

    conn.close()
    server.close()

def validate_args() -> None:
    if len(sys.argv) != 3:
        raise SystemExit(f"Usage: {sys.argv[0]} <ip_address> <port number>")

if __name__ == "__main__":
    validate_args()
    main()