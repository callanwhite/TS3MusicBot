import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;

import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import com.github.theholywaffle.teamspeak3.api.ClientProperty;
import com.github.theholywaffle.teamspeak3.api.TextMessageTargetMode;
import com.github.theholywaffle.teamspeak3.api.event.ChannelCreateEvent;
import com.github.theholywaffle.teamspeak3.api.event.ChannelDeletedEvent;
import com.github.theholywaffle.teamspeak3.api.event.ChannelDescriptionEditedEvent;
import com.github.theholywaffle.teamspeak3.api.event.ChannelEditedEvent;
import com.github.theholywaffle.teamspeak3.api.event.ChannelMovedEvent;
import com.github.theholywaffle.teamspeak3.api.event.ChannelPasswordChangedEvent;
import com.github.theholywaffle.teamspeak3.api.event.ClientJoinEvent;
import com.github.theholywaffle.teamspeak3.api.event.ClientLeaveEvent;
import com.github.theholywaffle.teamspeak3.api.event.ClientMovedEvent;
import com.github.theholywaffle.teamspeak3.api.event.ServerEditedEvent;
import com.github.theholywaffle.teamspeak3.api.event.TS3Event;
import com.github.theholywaffle.teamspeak3.api.event.TS3EventType;
import com.github.theholywaffle.teamspeak3.api.event.TS3Listener;
import com.github.theholywaffle.teamspeak3.api.event.TextMessageEvent;


public class QueryHandler implements TS3Listener {

	private TS3Api api;
	private TS3Config config;
	private TS3Query query;
	
	private int clientId;
	
	private String YT_PREFIX = "https://www.youtube.com/watch?v=";
	private String PAUSE = "pause";
	private String SKIP = "skip";
	
	private Queue<String> request;
	private Queue<String> ready;
	private boolean request_pause;
	private boolean request_skip;
	private boolean kill_me;
	
	private HashMap<ClientProperty, String> clientProperties;
	
	public QueryHandler(String hostname, String serveradmin, String password, String clientname) {
		config = new TS3Config();
			config.setHost(hostname);
			config.setDebugLevel(Level.OFF);
			config.setLoginCredentials(serveradmin, password);
			
		query = new TS3Query(config);
			query.connect();
			
		api = query.getApi();
			api.selectVirtualServerById(1);
			api.setNickname("MCBot");
			api.addTS3Listeners(this);
			/*Register events we're interested in*/
			api.registerEvent(TS3EventType.CHANNEL, 1);
			api.registerEvent(TS3EventType.TEXT_CHANNEL);
			
		/*Identify the clientId for channel moves*/
		clientId = api.getClientByName(clientname).get(0).getId();
		
		request = new LinkedList<String>();
		ready = new LinkedList<String>();
		
		clientProperties = new HashMap<ClientProperty, String>();
	}
	
	public void Update() {
		if (request.peek() != null) {
			new DownloadThread(request.poll()).start();
		}
	}
	
	public String GetReadyRequest() {
		return ready.poll();
	}
	
	public void UpdatePlayingDescription(String s) {
		clientProperties.put(ClientProperty.CLIENT_DESCRIPTION, s);
		api.editClient(clientId, clientProperties);
	}
	
	public boolean IsPauseRequested() {
		if (request_pause) {
			request_pause = false;
			return true;
		}
		return false;
	}
	
	public boolean IsSkipRequested() {
		if (request_skip) {
			request_skip = false;
			return true;
		}
		return false;
	}
	
	public boolean KillMe() {
		return kill_me;
	}
	
	public String NextPlaylistItem() {
		return ready.poll();
	}
	
	@Override
	public void onClientLeave(ClientLeaveEvent e) {
		/*If the client leaves, then we really want to be 
		 * bailing out of this program.
		 */
		if (e.getClientId() == clientId) {
			kill_me = true;
		}
	}

	@Override
	public void onClientMoved(ClientMovedEvent e) {
		/*Listen for the client changing channel, then
		 * follow it and reregister for events
		 */
		if (e.getClientId() == clientId) {
			api.moveClient(e.getClientTargetId());
			api.unregisterAllEvents();
			api.registerEvent(TS3EventType.CHANNEL, e.getClientTargetId());
			api.registerEvent(TS3EventType.TEXT_CHANNEL);
		}
	}

	@Override
	public void onTextMessage(TextMessageEvent e) {
		/*Do we have a command to process, and is it within the channel?*/
		if (e.getTargetMode() == TextMessageTargetMode.CHANNEL && e.getMessage().startsWith("!")) {
			String command = e.getMessage().substring(1);
			/*We have a youtube command*/
			if (command.startsWith(YT_PREFIX)) {
				String youtube_id = command.substring(YT_PREFIX.length());
				api.sendChannelMessage("Youtube Request: "+youtube_id);
				request.offer(youtube_id);
			}
			else if (command.equals(PAUSE)) {
				request_pause = true;
			}
			else if (command.equals(SKIP)) {
				request_skip = true;
			}
		}
	}
	
	/*These don't get used (yet)*/
	@Override
	public void onChannelCreate(ChannelCreateEvent e) {}
	@Override
	public void onChannelDeleted(ChannelDeletedEvent e) {}
	@Override
	public void onChannelDescriptionChanged(ChannelDescriptionEditedEvent e) {}
	@Override
	public void onChannelEdit(ChannelEditedEvent e) {}
	@Override
	public void onChannelMoved(ChannelMovedEvent e) {}
	@Override
	public void onChannelPasswordChanged(ChannelPasswordChangedEvent e) {}
	@Override
	public void onClientJoin(ClientJoinEvent e) {}
	@Override
	public void onServerEdit(ServerEditedEvent e) {}
	
	private class DownloadThread extends Thread {
		private String ident;
		public DownloadThread(String s) {
			this.ident = s;
		}
		public void run() {
			try {
				Process p = new ProcessBuilder("./getsong.sh", ident).directory(new File("yt")).start();
				p.waitFor();
				if (p.exitValue() != 0) {
					System.out.println("Non-Zero exit value for: "+ident);
					throw new IOException();
				}
				
				InputStream is = p.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String s = br.readLine();
				if ((s!=null) && !s.isEmpty()) {
					System.out.println("Successfully downloaded "+s+"EOL");
					ready.offer(s+".m4a");
				}
				else {
					throw new IOException();
				}
			} catch (IOException | InterruptedException e) {
				System.out.println("Error retrieving: "+ident);
			}
		}
	}
}
