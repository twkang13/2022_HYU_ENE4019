import java.io.*;
import java.net.*;

public class ServerFileThread extends Thread {
	public String command;
	public String fileName;
	public String groupName;
	public Socket fileSocket;
	public DataOutputStream out;
	
	public ServerFileThread(String command, String fileName, String groupName, Socket fileSocket, DataOutputStream out) {
		this.command = command;
		this.fileName = fileName;
		this.groupName = groupName;
		this.fileSocket = fileSocket;
		this.out = out;
	}
	
	public void run() {
		try {
			/* UPLOAD FILE */
			if (command.equals("#PUT")){
				System.out.println("PUT " + fileName); // Server Log
				
				File file = new File(groupName + "_storage/", fileName);
				FileOutputStream fo = new FileOutputStream(file);
				
				InputStream is = fileSocket.getInputStream();
				
				int readCnt = 0;
				byte[] buffer = new byte[65536]; //64KB buffer
				
				while ((readCnt = is.read(buffer)) > 0) {
	                fo.write(buffer, 0, readCnt);
	            }
				
				System.out.println("PUT " + fileName + " COMPLETE"); // Server Log
				fo.close();
				is.close();
			}
			/* DOWNLOAD FILE */
			else {
				System.out.println("GET " + fileName); // Server Log
				
				File file = new File(groupName + "_storage/" + fileName); 
				if (!file.exists()) {
					out.writeUTF("----- " + fileName + " not exists! -----");
				}
				else {
					FileInputStream fis = new FileInputStream(file);
				
					OutputStream os = fileSocket.getOutputStream();
				
					int putCnt = 0;
					byte[] buffer = new byte[65536]; //64KB buffer
			
					while((putCnt = fis.read(buffer)) > 0) {
						os.write(buffer, 0, putCnt);
					}
				
					System.out.println("GET " + fileName + " COMPLETE"); //Server Log
					fis.close();
					os.close();
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
