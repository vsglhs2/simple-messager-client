import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client {
	static private Socket clientSocket;
	static private BufferedReader in;
	static private PrintWriter out;
	static private Scanner reader;
	
	public static void disconnect() throws IOException {
		in.close();
		out.close();
		clientSocket.close();
	}
	
	public static void connect(String ip, int port) throws IOException, UnknownHostException {
		clientSocket = new Socket(ip, port);
		in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		out = new PrintWriter(clientSocket.getOutputStream(), true);
	}
	
	public static void messageProcess(ServerMessage message) throws RuntimeException {
		String argsAdditional = "";
		if (message.args != null) {
			argsAdditional = ", args: ";
			for (String arg : message.args) argsAdditional += arg + " ";
		}
		switch (message.header) {
		case "System":
			if (message.subHeader.equals("Context")) {
				System.out.println("Entered to " + message.headerType + " context");			
			} else if (message.headerType.equals("Disconnect")) {
				System.out.println("Disconnecting from server...");	
				throw new RuntimeException();
			}
		break;
		case "Error":
			System.out.println(message.subHeader + " error: " + message.headerType + argsAdditional);
		break;
		case "MMessage":
			if (message.headerType.equals("MessageGet")) System.out.println(message.args.get(0));
			else if (message.headerType.equals("Help") || 
					message.headerType.equals("UserList") || 
					message.headerType.equals("ChatList") || 
					message.headerType.equals("History")) {
				System.out.println(message.subHeader + " " + message.headerType);
				for (String arg : message.args) System.out.println(arg);
			}
			else if (!message.headerType.equals("MessageSend")) System.out.println(message.subHeader + " " + message.headerType + argsAdditional);
		break;
		}
	}
	
	static public void main(String[] args) {
		reader = new Scanner(System.in);	
		String ip = "127.0.0.1";
		int port = 775;
		if (args.length == 2) {
			ip = args[0];
			port = Integer.valueOf(args[1]);
		} else {
			System.out.print("Enter server ip adress: ");
			ip = reader.nextLine();
			System.out.print("Enter server port: ");
			port = reader.nextInt();
		}
		
		while (true) {
			try {
				connect(ip, port);
				System.out.println("Client connected to server on " + ip + ":" + port);			
				break;
			} 
			catch (IOException e) {
				System.out.println("Can't connect to such server\ntrue - try to connect to server at another ip and port, false - close the client");
				boolean tryToConnect = reader.nextBoolean();
				
				if (!tryToConnect) {
					System.out.println("Client was closed");
					reader.close();
					System.exit(0);
				}
				
				System.out.print("Enter server ip adress: ");
				ip = reader.next();
				System.out.print("Enter server port: ");
				port = reader.nextInt();
			}		
		}
		
		Thread terminalThread = new Thread() {		
			public void run() {
				String clientInput = "";
				while (true) {
					if (Thread.currentThread().isInterrupted()) {
						break;
					}		
					
					if (reader.hasNextLine()) {
						clientInput = reader.nextLine();
						if (!clientInput.isEmpty()) out.println(clientInput);
					}
				}
				
				try {
					reader.close();
					disconnect();
				} catch (IOException e) {}	
				
				System.out.println("InputStream closed");
			}
		};
		terminalThread.start();
		
		Thread inputThread = new Thread() {
			public void run() {
				String response = "";
				try {
					ServerMessage message = null;
					while (true) {
						if (in.ready()) {
							response = in.readLine();
							if (message == null && ServerMessage.isHeader(response)) {
								message = new ServerMessage();
							}
							
							if (message != null) {
								message.put(response);
							}
						} 
						
						if (message != null && message.isFulfilled()) {
							messageProcess(message);
							message = null;
						}
	
						Thread.sleep(100);
					}	
				} catch (IOException | InterruptedException | RuntimeException e) {}
				
				try {
					if (terminalThread.isAlive()) {
						System.out.println("Closing the inputStream");
						System.out.println("Enter to continue");
						terminalThread.interrupt();
					}						
					
					reader.close();
					disconnect();
					
					System.out.println("Disconnected");
				} catch (IOException e) {}
				

			}
		};
		inputThread.start();
		
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
		    public void run()
		    {
		    	out.println("exit");
		    	
		    	if (inputThread.isAlive()) inputThread.interrupt();
		    	if (terminalThread.isAlive()) terminalThread.interrupt();
		    	
		    	try {
		    		reader.close();
					disconnect();
				} catch (IOException e) {}
		    }
		});
	}
}

// java -jar C:\Users\User\eclipse-workspace2\Client\bin\client.jar
// java -jar C:\Users\User\eclipse-workspace2\Client\bin\server.jar