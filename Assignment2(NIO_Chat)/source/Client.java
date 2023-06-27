import java.io.*;
import java.net.*;
import java.nio.channels.*;

public class Client {
	public static String serverIP;
	public static int portNum1, portNum2;
	
	public SocketChannel clientChannel, fileChannel;
	
	public Group group;
	public boolean inGroup = false;
	
	public String groupName, userName;
	public String fileName;
	
	public File uploadFolder, downloadFolder;
	
	public Client() {};
	
	public Client(SocketChannel clientChannel) {
		this.clientChannel = clientChannel;
	}
	
	public void joinGroup(Group group) {
		this.group = group;
	}
	
	public void leaveGroup(Group group) {
		this.group = null;
	}
	
	public static void main(String[] args) {
		try {
			serverIP = args[0].toString();
			portNum1 = Integer.parseInt(args[1]);
			portNum2 = Integer.parseInt(args[2]);
			
			Client client = new Client();
			client.runClient();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void runClient() {
		try {
			clientChannel = SocketChannel.open(new InetSocketAddress(serverIP, portNum1));
			fileChannel = SocketChannel.open(new InetSocketAddress(serverIP, portNum2));
			
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			ClientReceive cr = new ClientReceive(clientChannel);
			cr.start();
		
			uploadFolder = new File("client_upload/");
			uploadFolder.mkdir();
		
			downloadFolder = new File("client_download/");
			downloadFolder.mkdir();
		
			while(true) {
				String str = br.readLine();
			
				if (str.length() == 0) {
					System.out.println("----- Please enter contents -----");
					continue;
				}
				
				if (str.charAt(0) == '#') {
					String[] command = str.split(" ");
				
					/* CREATE */
					if (command[0].equals("#CREATE")) {
						if (command.length != 3) {
							System.out.println("----- Not allowed command. Try again -----");
							continue;
						}
						
						if (inGroup) {
							System.out.println("----- You must leave current group to create another group -----");
							continue;
						}
						
						groupName = command[1];
						userName = command[2];
						
						ClientSend cs = new ClientSend("#CREATE", str, this, clientChannel);
						cs.start();
						
						Thread.sleep(100);
						inGroup = cr.getInGroup();
						
						if (inGroup) {
							this.userName = userName;
							this.groupName = groupName;
						}
					}
					/* JOIN */
					else if (command[0].equals("#JOIN")) {
						if (command.length != 3) {
							System.out.println("----- Not allowed command. Try again -----");
							continue;
						}
						
						if (inGroup) {
							System.out.println("----- You must leave current group to create another group -----");
							continue;
						}
						
						groupName = command[1];
						userName = command[2];
						
						ClientSend cs = new ClientSend("#JOIN", str, this, clientChannel);
						cs.start();
						
						Thread.sleep(100);
						inGroup = cr.getInGroup();
						
						if (inGroup) {
							this.userName = userName;
							this.groupName = groupName;
						}
					}
					/* PUT */
					else if (command[0].equals("#PUT")) {
						if (command.length != 2) {
							System.out.println("----- Not allowed command. Try again -----");
							continue;
						}
						
						if (inGroup) {
							fileName = command[1];
							
							ClientSend cs = new ClientSend("#PUT", fileName, str, serverIP, clientChannel, fileChannel);
							cs.start();
						}
						else {
							System.out.println("----- You are not in any chat room. Try again -----");
						}
					}
					/* GET */
					else if (command[0].equals("#GET")) {
						if (command.length != 2) {
							System.out.println("----- Not allowed command. Try again -----");
							continue;
						}
						
						if (inGroup) {
							fileName = command[1];
							
							ClientSend cs = new ClientSend("#GET", fileName, str, serverIP, clientChannel, fileChannel);
							cs.start();
						}
						else {
							System.out.println("----- You are not in any chat room. Try again -----");
						}
					}
					/* STATUS */
					else if (command[0].equals("#STATUS")) {
						if (command.length != 1) {
							System.out.println("----- Not allowed command. Try again -----");
							continue;
						}
						
						if (inGroup) {
							ClientSend cs = new ClientSend("#STATUS", clientChannel);
							cs.start();
						}
						else {
							System.out.println("----- You are not in any chat room. Try again -----");
						}
					}
					/* EXIT */
					else if (command[0].equals("#EXIT")) {
						if (command.length != 1) {
							System.out.println("----- Not allowed command. Try again -----");
							continue;
						}
						
						if (inGroup) {
							ClientSend cs = new ClientSend("#EXIT", clientChannel);
							cs.start();
							
							inGroup = false;
						}
						else {
							System.out.println("----- You are not in any chat room. Try again -----");
						}
					}
					/* QUIT */
					else if (command[0].equals("#QUIT")) {
						if (command.length != 1) {
							System.out.println("----- Not allowed command. Try again -----");
							continue;
						}
						
						ClientSend cs = new ClientSend("#QUIT", clientChannel);
						cs.start();
						
						Thread.sleep(100);
						cr = null;
						
						File[] ufList = uploadFolder.listFiles();
						if (ufList != null) {
							for (int i = 0; i < ufList.length; i++) {
								ufList[i].delete();
							}
						}
						uploadFolder.delete();
						
						File[] dfList = downloadFolder.listFiles();
						if (dfList != null) {
							for (int i = 0; i < dfList.length; i++) {
								dfList[i].delete();
							}
						}
						downloadFolder.delete();
						
						System.out.println("----- Quit Program -----");
						System.exit(0);
					}
					else {
						System.out.println("----- Not allowed command. Try again -----");
					}
				}
				/* SEND MESSAGE */
				else if (this.inGroup){
					ClientSend cs = new ClientSend("#SEND", str, this, clientChannel);
					cs.start();
				}
				else {
					System.out.println("----- You are not in any chat room. Try again -----");	
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
