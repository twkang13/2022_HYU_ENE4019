import java.io.*;
import java.net.*;

public class SendingThread extends Thread {
	public InetAddress IPAddress = null;
	public int portNum;
	
	public DatagramPacket dp = null;
	public MulticastSocket peer = null;
	
	public String message = null;
	
	public SendingThread(MulticastSocket peer, InetAddress group, String message) {
		try {
			this.peer = peer;
			this.IPAddress = group;
			this.portNum = peer.getLocalPort();
			this.message = message;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		try {
			byte[] data = new byte[512];
			DatagramPacket dp = new DatagramPacket(data, data.length);
		
			dp.setAddress(IPAddress);
			dp.setPort(portNum);
		
			byte[] byteMessage = message.getBytes();
			dp.setData(byteMessage);
		
			synchronized(this) {
				peer.send(dp);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
