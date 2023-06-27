import java.io.*;
import java.net.*;
import java.util.*;
import java.security.MessageDigest;

public class Peer extends Thread {
	public static String groupName = null;
	public static String userName = null;
	
	public static String ip = null;
	public static InetAddress group = null;
	
	public static boolean inGroup = false;
	
	public static MulticastSocket peer = null;
	
	public static void main(String[] args) {
		if (args == null) {
			System.out.println("Error : Wrong argument");
			return;
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		int portNum = Integer.parseInt(args[0]);
		
		try {
			peer = new MulticastSocket(portNum);
			
			ReadingThread rt = null;
			
			while(true) {
				String str = br.readLine();
				
				if (str.length() == 0) {
					System.out.println("----- Please enter the contents -----");
					continue;
				}
				
				/* command 입력 */
				if (str.charAt(0) == '#') {
					String[] command = str.split(" ");
					
					if (command[0].equals("#JOIN")) {
						groupName = command[1];
						userName = command[2];
						
						/* 이미 다른 채팅방에 참여중이면 해당 채팅방을 떠나고 새로운 채팅방에 참여 */
						if (inGroup) {
							rt = null;
							peer.leaveGroup(group);
						}
						
						MessageDigest sha = MessageDigest.getInstance("SHA-256");
						sha.update(groupName.getBytes());
						byte[] digest = sha.digest();
						
						int x = Byte.toUnsignedInt(digest[29]);
						int y = Byte.toUnsignedInt(digest[30]);
						int z = Byte.toUnsignedInt(digest[31]);
						
						group = InetAddress.getByName("225." + x + "." + y + "." + z);
						peer.joinGroup(group);
						
						inGroup = true;
						
						String welcomeMessage = "----- Welcome to " + groupName + ", " + userName + "! -----";
						SendingThread st = new SendingThread(peer, group, welcomeMessage);
						st.start();
						
						rt = new ReadingThread(peer);
						rt.start();
					}
					else if (command[0].equals("#EXIT")) {
						/* 참여중인 채팅방이 없을때 exit는 불가능 */
						if (!inGroup) {
							System.out.println("----- You are not in any chat room. Try again -----");
							continue;
						}
						
						String farewellMessage = "----- " + userName + " left " + groupName + " -----";
						SendingThread st = new SendingThread(peer, group, farewellMessage);
						st.start();
						
						rt = null;
						
						peer.leaveGroup(group);
						inGroup = false;
					}
					/* 프로그램 종료 */
					else if (command[0].equals("#QUIT")) {
						if (inGroup) {
							rt = null;
							peer.leaveGroup(group);
						}
						
						peer.close();
						System.out.println("----- Quit program -----");
						
						return;
					}
					else {
						System.out.println("----- Not allowed command. Try again -----");
					}
				}
				/* 채팅 입력 */
				else if (inGroup){
					String message = userName + " : " + str;
					SendingThread st = new SendingThread(peer, group, message);
					
					st.start();
				}
				/* Group에 들어가있지 않은 경우 */
				else {
					System.out.println("----- You are not in any chat room. Try again -----");
					continue;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
