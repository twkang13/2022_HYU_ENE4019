import java.io.*;
import java.net.*;

public class ClientSend extends Thread {
	public Socket client;
	
	public String command, message, fileName, groupName, userName;
	public int portNum;
	
	public DataOutputStream out;
	
	public ClientSend(String command, Socket client) {
		this.client = client;
		this.command = command;
	}
	
	public ClientSend(String command, String message, Socket client) {
		this.client = client;
		this.message = message;
		this.command = command;
	}
	
	public ClientSend(String command, String groupName, String userName, Socket client) {
		this.client = client;
		this.command = command;
		this.groupName = groupName;
		this.userName = userName;
	}
	
	public ClientSend(String command, String fileName, String groupName, String userName, Socket client, int portNum) {
		this.command = command;
		this.fileName = fileName;
		this.groupName = groupName;
		this.userName = userName;
		this.client = client;
		this.portNum = portNum;
	}
	
	public synchronized void run() {
		try {
			out = new DataOutputStream(client.getOutputStream());
			
			if (command.equals("#CREATE") || command.equals("#JOIN")) {
				out.writeUTF(command);
				out.writeUTF(groupName);
				out.writeUTF(userName);
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
				out.writeUTF(command);
				out.writeUTF(message);
			}
			else if (command.equals("#STATUS") || command.equals("#EXIT")) {
				out.writeUTF(command);
			}
			else if (command.equals("#QUIT")) {
				out.writeUTF(command);
				client.close();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void put() {
		try {
			File file = new File("client_upload/" + fileName);
			
			if (!file.exists()) {
				System.out.println("----- " + fileName + " not exists! -----");
			}
			else {
				out.writeUTF(command);
				out.writeUTF(fileName);
				
				FileInputStream fis = new FileInputStream(file);
			
				Socket socket = new Socket(client.getInetAddress(), portNum);
				OutputStream os = socket.getOutputStream();
			
				int putCnt = 0;
				byte[] buffer = new byte[65536]; //64KB buffer
		
				while((putCnt = fis.read(buffer)) > 0) {
					os.write(buffer, 0, putCnt);
					System.out.print('#');
				}
				System.out.println();
		
				fis.close();
				os.close();
				socket.close();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void get() {
		try {
			out.writeUTF(command);
			out.writeUTF(fileName);
			
			Socket socket = new Socket(client.getInetAddress(), portNum);
			InputStream is = socket.getInputStream();
			
			File file = new File("client_download/", fileName);
			FileOutputStream fo = new FileOutputStream(file);
				
			int readCnt = 0;
			byte[] buffer = new byte[65536]; //64KB buffer
			
			while ((readCnt = is.read(buffer)) > 0) {
				fo.write(buffer, 0, readCnt);
				System.out.print('#');
			}
			
			if (file.length() == 0) {
				file.delete();
			}
			else {
				System.out.println();
				System.out.println("----- " + fileName + " Downloaded -----");
			}
		
			fo.close();
			is.close();
			socket.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}