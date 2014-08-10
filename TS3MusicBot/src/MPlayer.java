import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

/*Class to represent an MPlayer instance and 
 * perform input/output operations on said instance
 */
public class MPlayer {

	private ProcessBuilder pb = new ProcessBuilder("mplayer","-slave","-idle","-quiet");
	private Process p;
	private PrintStream ps;
	
	private InputStream is;
	private InputStreamReader isr;
	private BufferedReader br;
	
	public MPlayer() throws IOException {
		p = pb.start();
		ps = new PrintStream(p.getOutputStream());
		
		is = p.getInputStream();
		isr = new InputStreamReader(is);
		br = new BufferedReader(isr);
	}
	
	public void Pause() {
		DoMPlayerCommand("pause");
	}
	
	public void LoadFile(String s) {
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
}
