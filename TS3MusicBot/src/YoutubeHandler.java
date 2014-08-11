import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Queue;


public class YoutubeHandler {
	private Queue<String> ready;
	private Queue<String> request;
	
	public YoutubeHandler() {
		ready = new LinkedList<String>();
		request = new LinkedList<String>();
	}
	
	public void Update() {
		if (request.peek() != null) {
			new DownloadThread(request.poll()).start(); //fly, my pretty
		}
	}
	
	public void AddRequest(String s) {
		request.offer(s);
	}
	public String GetReadyRequest(){ 
		return ready.poll();
	}
	
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