import java.net.*;
import java.util.*;

public class Group {
	public String groupName;
	public ArrayList<Client> clientList = new ArrayList<>();
	public ArrayList<Socket> socketList = new ArrayList<>();
	
	public Group(String groupName) {
		this.groupName = groupName;
	}
	
	public void addClient(Client client, Socket socket) {
		clientList.add(client);
		socketList.add(socket);
		System.out.println(client.userName + " JOINED TO " + groupName); // Server Log
	}
	
	public void removeClient(Client client, Socket socket) {
		clientList.remove(client);
		socketList.remove(socket);
		System.out.println(client.userName + " HAS LEFT " + groupName); // Server Log
	}
}
