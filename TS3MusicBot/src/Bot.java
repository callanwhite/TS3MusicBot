import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;


public class Bot {
	
	private Process PROCESS_TS_CLIENT;
	private MPlayer mplayer;
	private QueryHandler qh;
	private YoutubeHandler yh;
	
	public Bot(String hostname, String sq_username, String sq_password, String clientname, String password) throws IOException, InterruptedException {
		PROCESS_TS_CLIENT = new ProcessBuilder("sh","-c","xvfb-run -n 0 -f xauthfile -s \"-screen 0 640x480x16\" ./ts3client_runscript.sh \"ts3server://"+hostname+"?password="+password+"&nickname="+clientname+"\"").directory(new File("client")).start();
		Thread.sleep(3000); //wait for the client connect
		mplayer = new MPlayer();
		qh = new QueryHandler(hostname, sq_username, sq_password, clientname);
		yh = new YoutubeHandler();
	}
	
	public void Update() {

		String s = qh.NextPlaylistItem();
		if (s != null) { yh.AddRequest(s);}
		yh.Update();
		
		s = yh.GetReadyRequest();
		if (s!=null) { mplayer.QueueFile(s);}
		
		if (qh.IsPauseRequested()) { mplayer.Pause();}
		if (qh.IsSkipRequested()) { mplayer.Skip();}
		if (qh.KillMe()) { 
			System.out.println("The bot has left the server. Killing program");
			System.exit(1);
		}
		//qh.UpdatePlayingDescription("Now Playing: "+mplayer.GetFilename()+ "("+mplayer.GetPercentPos()+"%");
		
	}
	
	/*Forcibly kill the ts client after retrieving
	 * the pid via a reflective call
	 */
	public void KillProcesses() {
		int pid = GetPid(PROCESS_TS_CLIENT);
		System.out.println("TS_CLIENT_PID: "+pid);
		try {
			Process p = new ProcessBuilder("bash","-c","kill -9 "+pid).start();
			p.waitFor();
			System.out.println("Killed Processes");
		} catch (IOException | InterruptedException e) {
			System.out.println("Error killing process with PID: "+pid);
		}
	}
	
	private int GetPid(Process p) {
		Field f;
		try {
			f = p.getClass().getDeclaredField("pid");
			f.setAccessible(true);
			int pid = (Integer) f.get(p);
			return pid;
		} catch (Exception e) {
			System.out.println("Error retrieving PID");
			return 0;
		}
	}
	
	/*
	 * Program entry point and main loop
	 */
	public static void main(String[] args) {
		try {
			final Bot b = new Bot(args[0], args[1], args[2], args[3], args[4]);
			
			Runnable r = new Runnable() {
				public void run() {
					b.KillProcesses();
				}
			};
			Runtime.getRuntime().addShutdownHook(new Thread(r));
			
			while (true) {
				b.Update();
				Thread.sleep(1000);
			}
			
		} catch (InterruptedException | IOException e) {
			System.out.println("Fatal error. Exiting program.");
			System.exit(1);
		}
	}
}
