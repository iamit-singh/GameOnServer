import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ConnectionThread extends Thread {
	private final Socket socket;
	private String full_name;
	private String username;
	private ObjectInputStream obIn;
	private ObjectOutputStream obOut;
	
	ConnectionThread(Socket socket, String full_name, String username, ObjectInputStream obIn, ObjectOutputStream obOut){
		this.socket = socket;
		this.full_name = full_name;
		this.username = username;
		this.obIn = obIn;
		this.obOut = obOut;
	}
	
	public String getFull_name() {
		return full_name;
	}

	public String getUsername() {
		return username;
	}
	
	public void closeCon(){
		if(socket!=null){
			try{
				socket.close();
				obIn.close();
				obOut.close();
			}catch(Exception e){e.printStackTrace();}
		}
	}
	
	@Override
	public void run(){
		boolean running = true;
		try {
			obOut.writeUnshared(full_name);
			obOut.writeUnshared(ServerBL.getGames());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		while(running){
			try {
				ArrayList<String> req = (ArrayList<String>)obIn.readObject();
				switch(req.get(0)){
				case "a":      // Join the game and get list of opponents for the particular game
					ServerBL.addToAvailable(req.get(1), this);  // index1 = Name of the game
					obOut.writeUnshared(ServerBL.getAvailablePlayers(req.get(1)));
					break;
				case "b":      // Remove player from available list of the game
					ServerBL.removeFromAvailable(req.get(1), this);  // index1 = Name of the game
					break;
				case "c":      // send request to opponent
					if(ServerBL.sendRequestToJoin(req.get(1), req.get(2), username)){	// index1 game name, index2 request receiver
						isRequestSent("yes");   //player is still online
					}else{
						isRequestSent("no");    //player is now offline
					} 
					break;
				case "d":      // accept or reject request
					ServerBL.requestResponse(req.get(1), username, req.get(2), req.get(3)) ; // 1 gameName, 2 request sender username, 3 yes/no response
					break;
				case "e":      // exit from a game
					ServerBL.exitFromGame(req.get(1), this);
					break;
				}
			}catch (IOException e){
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		
	}

	private void isRequestSent(String string) {
		try {
			ArrayList<String> alist = new ArrayList<String>();
			alist.add("request sent");
			alist.add(string);
			obOut.writeUnshared(alist);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendJoinRequest(String game, String username) {
		try {
			ArrayList<String> alist = new ArrayList<String>();
			alist.add("request");
			alist.add(game);
			alist.add(username);
			obOut.writeUnshared(alist);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendJoinRequestResponse(String response) {
		try {
			ArrayList<String> alist = new ArrayList<String>();
			alist.add("response");
			alist.add(response);
			obOut.writeUnshared(alist);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendSuccessfullyConnected(String response) {
		try {
			ArrayList<String> alist = new ArrayList<String>();
			alist.add("connection success");
			alist.add(response);
			obOut.writeUnshared(alist);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
