import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.*;
import java.io.*;
import java.net.InetSocketAddress;

public class ClientSend extends Thread {
	public String command, string, fileName;
	public String serverIP;
	
	public Client client;
	public SocketChannel clientChannel, fileChannel;
	public Charset charset;
	
	public ClientSend(String command, SocketChannel clientChannel) {
		this.command = command;
		this.clientChannel = clientChannel;
	}
	
	public ClientSend(String command, String string, Client client, SocketChannel clientChannel) {
		this.command = command;
		this.string = string; // string == message || full command
		this.client = client;
		this.clientChannel = clientChannel;
	}
	
	public ClientSend(String command, String fileName, String string, String serverIP, SocketChannel clientChannel, SocketChannel fileChannel) {
		this.command = command;
		this.fileName = fileName;
		this.string = string;
		this.serverIP = serverIP;
		this.clientChannel = clientChannel;
		this.fileChannel = fileChannel;
	}
	
	public void write(String str) {
		try {
			Charset charset = Charset.forName("UTF-8");
			ByteBuffer outputBuffer = charset.encode(str);
			
			clientChannel.write(outputBuffer);
			outputBuffer.clear();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		if (command.equals("#CREATE") || command.equals("#JOIN")) {
			write(string);
		}
		/* UPLOAD FILE */
		else if (command.equals("#PUT")) {
			put();
		}
		/* DOWNLOAD FILE */
		else if (command.equals("#GET")) {
			get();
		}
		/* SEND MESSAGE */
		else if (command.equals("#SEND")) {
			write("#SEND " + string);
		}
		else if (command.equals("#STATUS") || command.equals("#EXIT")) {
			write(command);
		}
		else if (command.equals("#QUIT")) {
			write(command);
		}
	}
	
	public void put() {
		try {
			File file = new File("client_upload/" + fileName);
		
			if (!file.exists()) {
				System.out.println("----- " + fileName + " not exists! -----");
			}
			else {
				write(string);
			
				FileInputStream fis = new FileInputStream(file);
				FileChannel outChannel = fis.getChannel();
				
				int putCnt = 0;
				ByteBuffer buffer = ByteBuffer.allocate(65536); //64KB buffer
				
				while((putCnt = outChannel.read(buffer)) > 0) {
					fileChannel.write(buffer);
					buffer.flip();
					System.out.print('#');
				}
				System.out.println();
				
				fis.close();
				fileChannel.close();
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void get() {
		write(string);
		
		
	}
}
