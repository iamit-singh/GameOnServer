import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class ServerBL {
	private static ServerSocket ss;
	private static ArrayList<String> games;
	private static HashSet<ConnectionThread> player;
	private static HashMap<String, HashSet<ConnectionThread>> avlbl;
	private static HashMap<String, ArrayList<HashSet<ConnectionThread>>> ongoing;
	
	
	static void addGame(String str){
		if(games==null){
			games = new ArrayList<String>();
		}
		games.add(new String(str));
	}
	
	static void addToAvailable(String str, ConnectionThread ct){
		if(avlbl==null){
			avlbl= new HashMap<String, HashSet<ConnectionThread>>();
		}
		
		if(avlbl.get(str) == null){
			HashSet<ConnectionThread> hs = new HashSet<ConnectionThread>();
			avlbl.put(str, hs);
		}
		
		avlbl.get(str).add(ct);
	}
	
	static void removeFromAvailable(String str, ConnectionThread ct){
		if(avlbl==null || avlbl.get(str)==null)
			return;
		avlbl.get(str).remove(ct);
	}
	
	static ArrayList<String> getAvailablePlayers(String game){
		if(avlbl==null || avlbl.get(game)==null)
			return null;
		Iterator<ConnectionThread> it =  avlbl.get(game).iterator();
		ArrayList<String> res = new ArrayList<String>();
		
		res.add("a");
		while(it.hasNext()){
			res.add(it.next().getUsername());
		}
		
		return res;
	}
	
	static boolean sendRequestToJoin(String game, String username1, String username2){
		boolean tof = false;
		if(avlbl==null || avlbl.get(game)==null)
			return false;
		Iterator<ConnectionThread> it =  avlbl.get(game).iterator();
		
		while(it.hasNext()){
			ConnectionThread ct = it.next(); 
			if(ct.getUsername().equals(username1)){
				ct.sendJoinRequest(game,username2);
				tof = true;
				break;
			}
		}
		
		return tof;
	}
	
	static void addToOngoing(String str, ConnectionThread ct1, ConnectionThread ct2){
		if(ongoing == null){
			ongoing = new HashMap<String, ArrayList<HashSet<ConnectionThread>>>();
		}
		
		if(ongoing.get(str) == null){
			ArrayList<HashSet<ConnectionThread>> aList = new ArrayList<HashSet<ConnectionThread>>();
			ongoing.put(str, aList);
		}
		
		HashSet<ConnectionThread> hs = new HashSet<ConnectionThread>();
		hs.add(ct1);
		hs.add(ct2);
		
		ongoing.get(str).add(hs);
	}
	
	public static void addPlayer(ConnectionThread ct){
		if(player==null){
			player = new HashSet<ConnectionThread>();
		}
		player.add(ct);
	}
	
	
	public static ArrayList<String> getGames(){
		return games;
	}
	
	public static void main(String ...args){
		
		addGame("GTA");
		addGame("COUNTER STRIKE");
		addGame("BATMAN");
		addGame("SOCCER");
		addGame("WWE");
		
		try {
			ss = new ServerSocket(2500);
			
			while(true){
				System.out.println("Your IP address : "+InetAddress.getLocalHost().getHostAddress().toString());
				System.out.println("Waiting for connection..");
				Socket socket = ss.accept();
				
				WaitForLoginOrSignup wls=new WaitForLoginOrSignup(socket);
	            wls.setDaemon(true);
	            wls.start();
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public static void requestResponse(String game, String username1, String username2, String response) {
		
		if(avlbl==null || avlbl.get(game)==null)
			return;
		Iterator<ConnectionThread> it =  avlbl.get(game).iterator();
		ConnectionThread ct1 = null;
		ConnectionThread ct2 = null;
		while(it.hasNext()){
			ConnectionThread ct = it.next(); 
			if(ct.getUsername().equals(username1)){
				ct1 = it.next();
			}else if(ct.getUsername().equals(username2)){
				ct2 = it.next();
			}
		}
		
		if(ct1!=null && ct2!=null){
		
			if(response.equalsIgnoreCase("yes")){
				removeFromAvailable(game, ct1);
				removeFromAvailable(game, ct2);
				addToOngoing(game, ct1, ct2);
				ct2.sendJoinRequestResponse("yes");
				ct1.sendSuccessfullyConnected("yes");
			}else if(response.equalsIgnoreCase("no")){
				ct2.sendJoinRequestResponse("no");
			}
			
		}else if(ct1==null){
			ct2.sendJoinRequestResponse("out"); // the receiver of request is out of the available list
		}else{
			if(response.equalsIgnoreCase("yes")){
				ct1.sendSuccessfullyConnected("no");
			} 
		}
		
	}

	public static void exitFromGame(String game, ConnectionThread ct) { // incomplete
		if(ongoing==null || ongoing.get(game)==null)
			return;
		
		ArrayList<HashSet<ConnectionThread>> alist = ongoing.get(game);
		
		for(int i=0;i<alist.size();i++){
			if(alist.get(i).contains(ct)){
				
				
				alist.remove(i);
			}
		}
		
	}
	
	
}
