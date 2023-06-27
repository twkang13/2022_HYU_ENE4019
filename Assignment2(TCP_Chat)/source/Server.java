import java.net.*;
import java.util.*;

public class Server {
	public static int portNum1;
	public static int portNum2;
	
	public ServerSocket welcomeSocket;
	public Socket socket;
	
	public ServerSocket fileWelcomeSocket;
	public Socket fileSocket;
	
	public ArrayList<Group> groupList = new ArrayList<>();
	
	public static void main(String[] args) {
		portNum1 = Integer.parseInt(args[0]);
		portNum2 = Integer.parseInt(args[1]);
		
		Server server = new Server();
		server.runServer();
	}
	
	public void runServer() {
		ServerThread[] st = new ServerThread[100];
		int threadCnt = 0;
		
		try {
			welcomeSocket = new ServerSocket(portNum1);
			fileWelcomeSocket = new ServerSocket(portNum2);
			
			while(true) {
				socket = welcomeSocket.accept();
				
				st[threadCnt] = new ServerThread(groupList, socket, fileWelcomeSocket);
				st[threadCnt].start();
				++threadCnt;
				
				if (threadCnt >= 100) {
					threadCnt = 0;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}

