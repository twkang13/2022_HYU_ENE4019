import java.nio.channels.*;
import java.net.*;

public class Server {
	public static int portNum1, portNum2;
	
	public static void main(String[] args) {
		portNum1 = Integer.parseInt(args[0]);
		portNum2 = Integer.parseInt(args[1]);
		
		try {
			Selector selector = Selector.open();
			Selector fileSelector = Selector.open();
		
			/* Open Chat Server */
			ServerSocketChannel chatServerChannel = ServerSocketChannel.open();
			chatServerChannel.configureBlocking(false);
		
			ServerSocket chatSocket = chatServerChannel.socket();
			chatSocket.bind(new InetSocketAddress(portNum1));
		
			chatServerChannel.register(selector, SelectionKey.OP_ACCEPT);
			
			/* Open File Transfer Server */
			ServerSocketChannel fileServerChannel = ServerSocketChannel.open();
			fileServerChannel.configureBlocking(false);
		
			ServerSocket fileSocket = fileServerChannel.socket();
			fileSocket.bind(new InetSocketAddress(portNum2));
		
			fileServerChannel.register(fileSelector, SelectionKey.OP_ACCEPT);
			
			/* Staring Single Thread */
			ServerThread st = new ServerThread(selector, fileSelector);
			st.start();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
