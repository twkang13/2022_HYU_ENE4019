import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.*;

public class ServerThread extends Thread {
	public Selector selector;
	public Selector fileSelector;
	
	public ArrayList<Client> clientList = new ArrayList<>();
	public ArrayList<Group> groupList = new ArrayList<>();
	
	public ByteBuffer outputBuffer;
	public Charset charset;
	
	public ServerThread(Selector selector, Selector fileSelector) {
		this.selector = selector;
		this.fileSelector = fileSelector;
	}
	
	public void run() {
		outputBuffer = ByteBuffer.allocate(100);
		
		charset = Charset.forName("UTF-8");
		
		while(true) {
			try {
				selector.select();
				fileSelector.select();
				
				Set<SelectionKey> selectionKeys = selector.selectedKeys();
				Iterator<SelectionKey> iter = selectionKeys.iterator();
				
				while(iter.hasNext()) {
					SelectionKey key = iter.next();
					
					/* Accept */
					if (key.isAcceptable()) {
						accept(key);
					}
					/* Read */
					else if (key.isReadable()) {
						read(key);
					}
					
					iter.remove();
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void accept(SelectionKey key) throws IOException {
		ServerSocketChannel server = (ServerSocketChannel)key.channel();
		
		SocketChannel socketChannel = server.accept();
		socketChannel.configureBlocking(false);
		
		Client newClient = new Client(socketChannel);
		clientList.add(newClient);
		
		SelectionKey selectionKey = socketChannel.register(selector, SelectionKey.OP_READ);
		selectionKey.attach(newClient);
		
		System.out.println("CONNECTED"); // Server Log
	}
	
	public String getString(ByteBuffer inputBuffer) {
		return charset.decode(inputBuffer).toString();
	}
	
	public void write(String str, SocketChannel socket) {
		try {
			charset = Charset.forName("UTF-8");
			ByteBuffer outputBuffer = charset.encode(str);
			
			socket.write(outputBuffer);
			outputBuffer.clear();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void read(SelectionKey key) {
		Client client = (Client)key.attachment();
		
		ByteBuffer inputBuffer = ByteBuffer.allocate(100);
		
		String command, groupName, userName;
		String fileName, message;
		
		SocketChannel clientSocket = (SocketChannel)key.channel();
		
		try {
			clientSocket.read(inputBuffer);
			inputBuffer.flip();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		String[] str = getString(inputBuffer).split(" ");
		
		command = str[0];
		
		/* CREATE */
		if (command.equals("#CREATE")) {
			groupName = str[1];
			userName = str[2];
			
			createGroup(groupName, userName, client, clientSocket);
		}
		/* JOIN */
		else if (command.equals("#JOIN")) {
			groupName = str[1];
			userName = str[2];
			
			joinGroup(groupName, userName, client, clientSocket);
		}
		/* PUT or GET */
		else if (command.equals("#PUT") || command.equals("#GET")) {
			fileName = str[1];
			
			try {
				ServerFileThread ft = new ServerFileThread(fileSelector, command, fileName, client.groupName, this, clientSocket);
				ft.start(); // Fail
			
				if (command.equals("#PUT")) {
					write("----- " + fileName + " Uploaded -----", clientSocket);
					sendMessage("----- " + fileName + " Uploaded -----", client);
				}
				
				fileSelector.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		/* STATUS */
		else if (command.equals("#STATUS")) {
			showStatus(client, clientSocket);
		}
		/* EXIT or QUIT */
		else if (command.equals("#EXIT") || command.equals("#QUIT")) {
			if (client.inGroup) {
				leaveGroup(client, clientSocket);
			}
			
			if (command.equals("#QUIT")) {
				key.cancel();
				clientList.remove(clientSocket);
				System.out.println("DISCONNECTED"); // Server Log
			}
		}
		/* SEND */
		else if (command.equals("#SEND")) {
			message = client.userName + " : " + str[1];
			sendMessage(message, client);
		}
		
	}
	
	public void createGroup(String groupName, String userName, Client client, SocketChannel clientSocket) {
		for (int i = 0; i < groupList.size(); i++) {
			if (groupList.get(i).groupName.equals(groupName)) {
				write("#FALSE", clientSocket);
				write("----- A group with name '" + groupName + "' already exists -----", clientSocket);
				return;	
			}
		}
		
		Group group = new Group(groupName);
		
		client.userName = userName;
		client.groupName = groupName;
		client.group = group;
		
		client.joinGroup(group);
		group.addClient(client, clientSocket);
		groupList.add(group);
		
		client.inGroup = true;
		
		File storage = new File(groupName + "_storage/");
		storage.mkdir();
		
		write("#TRUE", clientSocket);
		write("----- " + userName + " made a group '" + groupName + "'! -----", clientSocket);
	}
	
	public void joinGroup(String groupName, String userName, Client client, SocketChannel clientSocket) {
		client.userName = userName;
		
		for (int i = 0; i < groupList.size(); i++) {
			if (groupList.get(i).groupName.equals(groupName)) {
				Group group = groupList.get(i);
				
				for (int j = 0; j < group.clientList.size(); j++) {
					if (userName.equals(group.clientList.get(j).userName)) { 
						write("#FALSE", clientSocket);
						write("----- There is a user with the same name in the group -----", clientSocket);
						return;
					}
				}
				
				System.out.println("GET"); // Server Log
				
				client.userName = userName;
				client.groupName = groupName;
				client.group = group;
				
				client.joinGroup(group);
				group.addClient(client, clientSocket);
				
				File storage = new File(groupName + "_storage/");
				storage.mkdir();
				
				client.inGroup = true;
				
				write("#TRUE", clientSocket);
				write("----- Welcome to " + groupName + ", " + userName + "! -----", clientSocket);
				sendMessage("----- Welcome to " + groupName + ", " + userName + "! -----", client);
				return;
			}
		}
		
		if (!client.inGroup) {
			write("#FALSE", clientSocket);
			write("----- A group with name '" + groupName + "' does not exist -----", clientSocket); 
			return;
		}
	}
	
	public void showStatus(Client client, SocketChannel clientSocket) {
		String head = "----- " + client.groupName + " -----";
		String status = head + "\n";
		
		for (int i = 0; i < client.group.clientList.size(); i++) {
			status += (client.group.clientList.get(i).userName + "\n");
		}
		
		for (int i = 0; i < head.length(); i++) {
			status += "-";
		}
		write(status, clientSocket);
	}
	
	public void leaveGroup(Client client, SocketChannel clientSocket) {
		write("----- " + client.userName + " left " + client.groupName + " -----", clientSocket); 
		sendMessage("----- " + client.userName + " left " + client.groupName + " -----", client);
		
		Group xGroup = client.group;
		
		client.group.removeClient(client, clientSocket);
		client.leaveGroup(client.group);
		
		if (xGroup.clientList.size() == 0) {
			Iterator<Group> iter = groupList.iterator();
			while(iter.hasNext()) {
				Group tmpGroup = iter.next();
				if (tmpGroup.groupName.equals(client.groupName)) {
					iter.remove();
					break;
				}
			}
			
			File storage = new File(client.groupName + "_storage/");
			
			File[] stList = storage.listFiles();
			if (stList != null) {
				for (int i = 0; i < stList.length; i++) {
					stList[i].delete();
				}
			}
			storage.delete();
			System.out.println(client.groupName + " HAS BEEN REMOVED FROM GROUPLIST."); // Server Log
			
			client.group = null;
			client.inGroup = false;
			client = null;

		}
	}
	
	public void sendMessage(String message, Client client) {
		if (client.inGroup) {
			System.out.println("SEND '" + message + "' TO " + client.groupName); // Server Log
			
			for (int i = 0; i < client.group.clientList.size(); i++) { 
				if (!client.group.clientList.get(i).userName.equals(client.userName)) {
					write(message, client.group.socketChannelList.get(i));
				}
			}
		}
	}
}
