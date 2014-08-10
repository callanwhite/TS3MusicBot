import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;

import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
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
	
	private Queue<String> playlist;
	private boolean request_pause;
	private boolean request_skip;
	
	public QueryHandler(String hostname, String serveradmin, String password, String clientname) {
		config = new TS3Config();
			config.setHost(hostname);
			config.setDebugLevel(Level.ALL);
			config.setLoginCredentials(serveradmin, password);
			
		query = new TS3Query(config);
			query.connect();
			
		api = query.getApi();
			api.selectVirtualServerById(1);
			api.setNickname("MCBot");
			api.addTS3Listeners(this);
		
		clientId = api.getClientByName(clientname).get(0).getId();
		
		playlist = new LinkedList<String>();
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
	
	public String NextPlaylistItem() {
		return playlist.poll();
	}

	@Override
	public void onChannelCreate(ChannelCreateEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onChannelDeleted(ChannelDeletedEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onChannelDescriptionChanged(ChannelDescriptionEditedEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onChannelEdit(ChannelEditedEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onChannelMoved(ChannelMovedEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onChannelPasswordChanged(ChannelPasswordChangedEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClientJoin(ClientJoinEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClientLeave(ClientLeaveEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClientMoved(ClientMovedEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onServerEdit(ServerEditedEvent e) {
		// TODO Auto-generated method stub
		
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
				playlist.offer(youtube_id);
			}
			else if (command.equals(PAUSE)) {
				request_pause = true;
			}
			else if (command.equals(SKIP)) {
				request_skip = true;
			}
		}
	}
}