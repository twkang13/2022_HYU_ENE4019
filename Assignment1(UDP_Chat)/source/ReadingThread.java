import java.io.*;
import java.net.*;

public class ReadingThread extends Thread {
	public MulticastSocket peer = null;
	
	public ReadingThread(MulticastSocket peer) {
		this.peer = peer;
	}
	
	public void run() {
		while(!peer.isClosed()) {		
			try {
				byte[] data = new byte[512];
				
				DatagramPacket dp = new DatagramPacket(data, 512);
				
				synchronized(this) {
					peer.receive(dp);
				}
				
				String message = new String(dp.getData());
				System.out.println(message);
			}
			catch (IOException e) {
				return;
			}
		}
	}
}
