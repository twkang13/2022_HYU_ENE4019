import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.*;
import java.io.*;

public class ClientReceive extends Thread {
	public SocketChannel socket;
	public boolean inGroup;
	
	public ClientReceive(SocketChannel socket) {
		this.socket = socket;
	}
	
	public void run() {
		Charset charset = Charset.forName("UTF-8");
		ByteBuffer inputBuffer = ByteBuffer.allocate(100);
		
		while(true) {
			try {
				socket.read(inputBuffer);
				inputBuffer.flip();
				
				String message = charset.decode(inputBuffer).toString();
				
				if (message.equals("#TRUE")) {
					inGroup = true;
					inputBuffer.clear();
					continue;
				}
				else if (message.equals("#FALSE")){
					inGroup = false;
					inputBuffer.clear();
					continue;
				}
				
				System.out.println(message);
				
				inputBuffer.clear();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean getInGroup() {
		return inGroup;
	}
}