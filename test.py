import socket

# 1. Connect to your Java server
s = socket.create_connection(("localhost", 6379))

# 2. Send data to the server
while(1):
    
    message = input() 
    message+="\r\n"  
    s.sendall(message.encode())

    # 3. Read the response
    response = s.recv(1024)

    # 4. Print what the server said
    print("Received:", response.decode())

s.close()