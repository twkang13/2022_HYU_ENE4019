import java.io.*;
import java.net.*;

public class ClientReceive extends Thread {
	public Socket client;
	public String message;
	public boolean inGroup;
	
	public ClientReceive(Socket client){
		this.client = client;
	}
	
	public void run() {
		try{
			DataInputStream in = new DataInputStream(client.getInputStream());
			
			while(true) {
				message = in.readUTF();
				
				if (message.equals("#TRUE")) {
					message = in.readUTF();
					inGroup = true;
				}
				else if (message.equals("#FALSE")) {
					message = in.readUTF();
					inGroup = false;
				}
				
				System.out.println(message);
			}
		}
		catch(Exception e) {
			return;
		}
	}
	
	public boolean getInGroup() {
		return inGroup;
	}
}
