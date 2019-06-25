import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class WaitForLoginOrSignup extends Thread{
	Socket socket;
	private ObjectInputStream obIn;
	private ObjectOutputStream obOut;
	
	public WaitForLoginOrSignup(Socket socket) {
		this.socket = socket;
		setupStreams();
	}
	
	private void setupStreams(){
        try {
            obOut = new ObjectOutputStream(socket.getOutputStream());
            obOut.flush();
            obIn = new ObjectInputStream(socket.getInputStream());
        } catch (IOException ex) {}
    }
	
	@Override
	public void run(){
		boolean running=true;
		
		while(running){
			try {
				ArrayList<String> req = (ArrayList<String>) obIn.readObject();
				
				switch(req.get(0)){
				case "a":				//For Login  index 1 username, index 2 password
		            System.out.println("Login Request from username "+req.get(1));
		            
		            if(LoginUtils.isUser(req.get(1), req.get(2))){
		            	String full_name = LoginUtils.getFullName(req.get(1));
		            	ConnectionThread ct = new ConnectionThread(socket, full_name, req.get(1), obIn, obOut);
		            	ServerBL.addPlayer(ct);
		            	ct.start();
		            	System.out.println(req.get(1) + " connected.");
		            	running = false;
		            }else{
		            	obOut.writeUTF("Invalid username or password");
		            	System.out.println("Invalid username or password ("+ req.get(1) +", "+ req.get(2) +")");
		            }
					break;
				case "b":				//For SignUp index 1 full_name, index 2 username, index 3 password
					System.out.println("New signup Request");
		            
		            if(LoginUtils.isUser(req.get(2))){
		            	obOut.writeUTF("Username "+ req.get(2) + " is already a user.");
		            	System.out.println(req.get(2) + " is already a user.");
		            }else{
		            	LoginUtils.registerUser(req.get(1),req.get(2),req.get(3));
		            	obOut.writeUTF("Successfully Registered");
		            	System.out.println("Successfully Registered."+"Username :"+ req.get(2) + ", Fullname :"+req.get(1));
		            	ConnectionThread ct = new ConnectionThread(socket, req.get(1), req.get(2), obIn, obOut);
		            	ServerBL.addPlayer(ct);
		            	ct.start();
		            	running = false;
		            }
					break;
				default:
					closeCon();
					running = false;
				}
			} catch (IOException e) {
				running= false;
				closeCon();
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				running= false;
				closeCon();
				e.printStackTrace();
			}
			
		}
		
	}

	private void closeCon() {
		if(socket!=null){
			try {
				socket.close();
				obIn.close();
				obOut.close();
			} catch (IOException e) {e.printStackTrace();}
		}
	}
}
