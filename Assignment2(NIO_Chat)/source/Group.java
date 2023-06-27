import java.util.*;
import java.nio.channels.*;

public class Group {
	public String groupName;
	public ArrayList<Client> clientList = new ArrayList<>();
	public ArrayList<SocketChannel> socketChannelList = new ArrayList<>();
	
	public Group(String groupName) {
		this.groupName = groupName;
	}
	
	public void addClient(Client client, SocketChannel socketChannel) {
		clientList.add(client);
		socketChannelList.add(socketChannel);
		System.out.println(client.userName + " JOINED TO " + groupName); // Server Log
	}
	
	public void removeClient(Client client, SocketChannel socketChannel) {
		clientList.remove(client);
		socketChannelList.remove(socketChannel);
		System.out.println(client.userName + " HAS LEFT " + groupName); // Server Log
	}
}
