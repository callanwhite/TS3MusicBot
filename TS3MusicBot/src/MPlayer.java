import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/*Class to represent an MPlayer instance and 
 * perform input/output operations through
 * manipulation of in/out/err streams
 */
public class MPlayer {

	private ProcessBuilder pb = new ProcessBuilder("mplayer","-slave","-idle","-quiet");
	private Process p;
	private PrintStream ps;
	private BufferedReader mout;


	public MPlayer() throws IOException {
		p = pb.start();
		ps = new PrintStream(p.getOutputStream());
		
		PipedInputStream rf = new PipedInputStream(256*1024);
		PipedOutputStream wt = new PipedOutputStream(rf);
		mout = new BufferedReader(new InputStreamReader(rf));
		
		new LineRedirector(p.getInputStream(), wt).start();
		new LineRedirector(p.getErrorStream(), wt).start();
	}
	
	/*Attempt to read the response to a command
	 * sent to mplayer. Potentially blocking, should
	 * probably be wrapped in an executor.
	 */
	private String GetCommandAnswer() {
		try {
			String s;
			while ((s = mout.readLine()) != null) {
				if (s.contains("ANS")) {
					return s;
				}
			}
		} catch (IOException e) {
			System.err.println("Error retrieving response");
		}
		return null;
	}
	
	private String GetProperty(String s) {
		DoMPlayerCommand("get_property "+s);
		String ret = GetCommandAnswer();
		if (ret.startsWith("ANS_"+s)) { 
			return ret.substring(("ANS_"+s+"=").length());
		}
		return s+" not available";
	}
	
	public String GetPercentPos() {
		return GetProperty("percent_pos");
	}
	
	public String GetFilename() {
		return GetProperty("filename");
	}
	
	public String GetFileLength() {
		return GetProperty("length");
	}
	
	public String GetTimePos() {
		return GetProperty("time_pos");
	}
	
	public void Pause() {
		DoMPlayerCommand("pause");
	}
	
	public void ForceFile(String s) {
		DoMPlayerCommand("loadfile \"yt/"+s+"\" 0");
	}
	public void QueueFile(String s) {
		DoMPlayerCommand("loadfile \"yt/"+s+"\" 1");
	}
	
	public void Skip() {
		DoMPlayerCommand("pt_step 1");
	}
	
	private void DoMPlayerCommand(String s) {
		ps.print(s);
		ps.print("\n");
		ps.flush();
	}
	
	private class LineRedirector extends Thread {
		private InputStream in;
		private OutputStream out;
		public LineRedirector(InputStream in, OutputStream out) {
			this.in = in;
			this.out = out;
		}
		public void run() {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				PrintStream ps = new PrintStream(out);
				String line;
				while ((line = reader.readLine()) != null) {
					ps.println(line);
				}
			} catch (IOException e) {
				System.out.println("LineRedirector fucked up");
			}
		}
	}
}
