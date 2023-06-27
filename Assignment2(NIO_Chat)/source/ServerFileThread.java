import java.io.*;
import java.util.*;
import java.nio.*;
import java.nio.channels.*;

public class ServerFileThread extends Thread {
	public Selector fileSelector;
	public String command, fileName, groupName;
	public ServerThread st;
	public SocketChannel clientSocket;
	
	public ServerFileThread(Selector fileSelector) {
		this.fileSelector = fileSelector;
	}
	
	public ServerFileThread(Selector fileSelector, String command, String fileName, String groupName, ServerThread st, SocketChannel clientSocket) {
		this.fileSelector = fileSelector;
		this.command = command;
		this.fileName = fileName;
		this.groupName = groupName;
		this.st = st;
		this.clientSocket = clientSocket;
	}

	public void run() {
		try {
			Set<SelectionKey> fileSelectionKeys = fileSelector.selectedKeys();
			Iterator<SelectionKey> iter = fileSelectionKeys.iterator();
			
			while(iter.hasNext()) {
				SelectionKey key = iter.next();
				
				/* Accept */
				if (key.isAcceptable()) {
					accept(key);
				}
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
	
	public void accept(SelectionKey key) throws Exception {
		ServerSocketChannel fileServer = (ServerSocketChannel)key.channel();
		
		SocketChannel fileSocketChannel = fileServer.accept();
		fileSocketChannel.configureBlocking(false);
		
		fileSocketChannel.register(fileSelector, SelectionKey.OP_READ);
		
		System.out.println("FILE SERVER CONNECTED"); // Server Log
	}
	
	public void read(SelectionKey key) throws Exception {
		SocketChannel fileSocket = (SocketChannel)key.channel();
		
		/* UPLOAD FILE */
		if (command.equals("#PUT")) {
			System.out.println("PUT " + fileName); // Server Log
		
			File file = new File(groupName + "_storage/", fileName);
			FileOutputStream fo = new FileOutputStream(file);
			
			FileChannel inChannel = fo.getChannel();
			
			ByteBuffer buffer = ByteBuffer.allocate(65536); //64KB buffer
			
			int readCnt = 0;
			while((readCnt = inChannel.read(buffer)) > 0) {
				fileSocket.read(buffer);
				buffer.flip();
				System.out.print('#');
			}
			System.out.println();
			
			System.out.println("PUT " + fileName + " COMPLETE"); // Server Log
			fo.close();
		}
		/* DOWNLOAD FILE */
		else {
			System.out.println("GET " + fileName); // Server Log
			
			File file = new File(groupName + "_storage/" + fileName); 
			if (!file.exists()) {
				st.write("----- " + fileName + " not exists! -----", clientSocket);
			}
			else {
				FileInputStream fis = new FileInputStream(file);
				
				
				System.out.println("GET " + fileName + " COMPLETE"); //Server Log
			}
		}
	}
}
