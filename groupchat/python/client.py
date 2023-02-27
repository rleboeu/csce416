import socket
import select
import sys

def main() -> None:
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    ip_addr = str(sys.argv[1])
    port = int(sys.argv[2])

    server.connect((ip_addr, port))
    
    while True:
        stream_list = [sys.stdin, server]

        read_socks, write_sock, error_sock = select.select(stream_list,[],[])

        for socks in read_socks:
            if socks == server:
                message = socks.recv(2048)
                print(message)
            else:
                message = sys.stdin.readline()
                server.send(message)
                sys.stdout.write(f"[local]: {message}")
                sys.stdout.flush()
                
    server.close()


def validate_args() -> None:
    if len(sys.argv) != 3:
        raise SystemExit(f"Usage: {sys.argv[0]} <ip_address> <port number>")

if __name__ == "__main__":
    validate_args()
    main()