import java.net.*;
import java.io.*;
import java.util.*;

class HttpServer{

	public static void main(String args[])
	{

		try
		{	//if the correct nubber of arguments weren enterd
			if(args.length != 1)
			{
				//print the correct usage
	    		System.out.println("Usage: HttpServer <path to folder to serve files from>");
				System.exit(0);
			}

			//"/home/sk382/204website/"
			//the location to look for the files in
			String startFolder = args[0];

			System.out.println("web server starting");
			//create the server socket
			ServerSocket server = new ServerSocket(8080);
			//loop infinitley
			while(true)
			{
				//waits fo r a request
				Socket client = server.accept();

				//create a new inet address object
				InetAddress IP = client.getLocalAddress();
				System.out.println("Here comes a request from: " + client.getLocalAddress());

				//create a new servre session thred for the client
				HttpServerSession session1 = new HttpServerSession(client, startFolder);
				session1.start();
			}
		}
		catch(IOException e)
		{
			System.out.println(e.getMessage());
		}
	}
}

class HttpServerSession extends Thread{

	private Socket client;
	String startFolder;

	//constructor
    public HttpServerSession(Socket clientIn, String startFolderIn) {
		client = clientIn;
		startFolder = startFolderIn;
    }
    //replacement println method needed to comply with the http spec
	private void println2(BufferedOutputStream bos, String s)
	throws IOException
	{
		String news = s + "\r\n";
		byte[] array = news.getBytes();
		for(int i=0; i<array.length; i++)
		{
			bos.write(array[i]);
		}
		return;
	}
    //the main method for the server session
    public void run()
    {
    	System.out.println("running the session");

    	try
    	{
    		//checks that the client is still connected
    		if(client.isConnected())
    		{
				//create a new reader to read the request from the client
		 		BufferedReader reader = new BufferedReader( new InputStreamReader(client.getInputStream()));

		 		String request = reader.readLine();
		 		System.out.println(request);

		 		//split up the request int its seperate parts
		 		String parts[] = request.split(" ");

		 		//check that the header is not corrupted
		 		if(parts.length == 3)
		 		{
	 				//check that the request type is correct
		 			if(parts[0].compareTo("GET") == 0)
		 			{
		 				//extract the file name form the request
		 				String filename = parts[1].substring(1);
		 				System.out.println(filename);

		 				//loops untill the end of the header
						while(true)
						{
							String line = reader.readLine();
							if(line == null) {
								System.out.println("error: bad header");
								System.exit(0);
							}
							if(line.compareTo("") == 0)
							break;
						}

						//create an output stream to write the file to the client
						BufferedOutputStream bos = new BufferedOutputStream(client.getOutputStream());

						System.out.println("printing");

						try
						{
							//create the file object to write to the client
							File file = new File(startFolder + filename);
							FileInputStream fis = new FileInputStream(file);

							//write the header to the client

							String output = "HTTP/1.1 200 OK";
							println2(bos, output);

							output = "";
							println2(bos, output);

							byte[] array = new byte[1024];
							int bytes = 0;

							//write the file to the client
							try
							{
								while((bytes = fis.read(array)) != -1)
								{
									bos.write(array, 0, bytes);
									bos.flush();

									//simulates a slow connection--------------------------------------->
									//Thread.sleep(1000);
								}
							}
							catch(Exception e)
							{
								//System.err.println("Sleep Exception: " + e);
							}
						}
						//send the file not found error
						catch (IOException e)
						{
							String output = "HTTP/1.1 404 OK";
							println2(bos, output);
						}

						bos.close();
						client.close();
		 			}
		 		}
			}
    	}

		catch(IOException e)
		{
			System.out.println(e.getMessage());
		}
    }
}
