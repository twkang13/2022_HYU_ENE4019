import java.io.*;
import java.net.*;
import java.util.*;

public class ServerThread extends Thread {
	public Socket socket;
	public ArrayList<Group> groupList;
	
	public DataOutputStream out;
	public DataInputStream in;
	
	public String command;
	
	public Group group;
	public String groupName;
	public boolean inGroup;

	public ServerSocket fileWelcomeSocket;
	public Socket fileSocket;
	public String fileName;
	
	public File storage;
	
	public Client client;
	public String userName;
	
	public ServerThread(ArrayList<Group> groupList, Socket socket, ServerSocket fileWelcomeSocket) {
		this.groupList = groupList;
		this.socket = socket;
		this.fileWelcomeSocket = fileWelcomeSocket;
	}
	
	public synchronized void run() {
		try {
			while(true) {
				in = new DataInputStream(socket.getInputStream());
				command = in.readUTF();
							
				out = new DataOutputStream(socket.getOutputStream());
			
				/* CREATE */
				if (command.equals("#CREATE")) {
					createGroup();
				}
				/* JOIN */
				else if (command.equals("#JOIN")) {
					joinGroup();
				}
				/* PUT or GET */
				else if (command.equals("#PUT") || command.equals("#GET")){
					fileSocket = fileWelcomeSocket.accept();
					fileName = in.readUTF();
					
					ServerFileThread ft = new ServerFileThread(command, fileName, groupName, fileSocket, out);
					ft.run();
					
					if (command.equals("#PUT")) {
						out.writeUTF("----- " + fileName + " Uploaded -----");
						sendMessage("----- " + fileName + " Uploaded -----");
					}
					
					fileSocket.close();
				}
				/* STATUS */
				else if (command.equals("#STATUS")){
					showStatus();
				}
				/* EXIT or QUIT */
				else if (command.equals("#EXIT") || command.equals("#QUIT")) {
					if (inGroup) {
						leaveGroup(group);
					}
					
					if (command.equals("#QUIT")) {
						socket.close();
					}
				}
				/* SEND */
				else if (command.equals("#SEND")) {
					String message = in.readUTF();
					sendMessage(message);
				}
			}
		}
		catch (Exception e){
			return;
		}
	}
	
	public void createGroup() throws Exception {
		groupName = in.readUTF();
		userName = in.readUTF();
		
		for (int i = 0; i < groupList.size(); i++) {
			if (groupList.get(i).groupName.equals(groupName)) {
				out.writeUTF("#FALSE");
				out.writeUTF("----- A group with name '" + groupName + "' already exists -----");
				return;	
			}
		}
	
		client = new Client(groupName, userName, socket);
		group = new Group(groupName);
	
		client.joinGroup(group);
		group.addClient(client, socket);
	
		groupList.add(group);
		inGroup = true;
		
		storage = new File(groupName + "_storage/");
		storage.mkdir();
		
		out.writeUTF("#TRUE");
		out.writeUTF("----- " + userName + " made a group '" + groupName + "'! -----");
		sendMessage("----- " + userName + " made a group '" + groupName + "'! -----");
	}
	
	public void joinGroup() throws Exception {
		groupName = in.readUTF();
		userName = in.readUTF();
		
		client = new Client(groupName, userName, socket);
		
		for (int i = 0; i < groupList.size(); i++) {
			if (groupList.get(i).groupName.equals(groupName)) {
				group = groupList.get(i);
				
				for (int j = 0; j < group.clientList.size(); j++) {
					if (userName.equals(group.clientList.get(j).userName)) { 
						out.writeUTF("#FALSE");
						out.writeUTF("----- There is a user with the same name in the group -----"); 
						return;
					}
				}
				
				System.out.println("GET"); // Server Log
				
				client.joinGroup(group);
				group.addClient(client, socket);
				
				storage = new File(groupName + "_storage/");
				inGroup = true;
				
				out.writeUTF("#TRUE");
				out.writeUTF("----- Welcome to " + groupName + ", " + userName + "! -----");
				sendMessage("----- Welcome to " + groupName + ", " + userName + "! -----");
				return;	
			}
		}
		
		if (!inGroup) {
			out.writeUTF("#FALSE");
			out.writeUTF("----- A group with name '" + groupName + "' does not exist -----"); 
			return;
		}
	}
	
	public void showStatus() throws Exception {
		String head = "----- " + groupName + " -----";
		out.writeUTF(head);
		
		for (int i = 0; i < group.clientList.size(); i++) {
			out.writeUTF(group.clientList.get(i).userName);
		}
		
		String tail = "";
		for (int i = 0; i < head.length(); i++) {
			tail += "-";
		}
		out.writeUTF(tail);
	} 
	
	public void leaveGroup(Group group) throws Exception {
		out.writeUTF("----- " + userName + " left " + groupName + " -----"); 
		sendMessage("----- " + userName + " left " + groupName + " -----");
		
		client.leaveGroup(group);
		group.removeClient(client, socket);
		
		if (group.clientList.size() == 0) {
			Iterator<Group> iter = groupList.iterator();
			while(iter.hasNext()) {
				Group tmpGroup = iter.next();
				if (tmpGroup.groupName.equals(groupName)) {
					iter.remove();
					break;
				}
			}
			
			File[] stList = storage.listFiles();
			if (stList != null) {
				for (int i = 0; i < stList.length; i++) {
					stList[i].delete();
				}
			}
			storage.delete();
			System.out.println(groupName + " HAS BEEN REMOVED FROM GROUPLIST."); // Server Log
		}
		
		group = null;
		client = null;
		
		inGroup = false;
	}
	
	public void sendMessage(String message) throws Exception {
		if (inGroup) {
			DataOutputStream sendOut = null;
			System.out.println("SEND '" + message + "' TO " + groupName); // Server Log
			
			for (int i = 0; i < group.clientList.size(); i++) { 
				if (!group.clientList.get(i).userName.equals(userName)) {
					sendOut = new DataOutputStream(group.socketList.get(i).getOutputStream());
					sendOut.writeUTF(message);
				}
			}
		}
	}
}