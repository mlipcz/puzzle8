package pl.modulo.puzzle8;

import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class Puzzle8 implements Runnable {

	final static int THREADS = 4;
	static CountDownLatch doneSignal = new CountDownLatch(THREADS);
	static List<Long> basicBlocks = new ArrayList<>();		
	static List<List<Long>> rotatedBlocks = new ArrayList<>();
	static List<List<Long>> movedBlocks = new ArrayList<>();
	static PrintStream out;
	int w[];
	int id;
	
	public static void main(String[] args) throws Exception {
		init();
		for (int i = 0; i < THREADS; i++)
			new Thread(new Puzzle8(i)).start();
		out.println("start  "+System.currentTimeMillis());
		doneSignal.await();
		out.println("end    "+System.currentTimeMillis());
		out.close();
	}
	
	public Puzzle8() {
	}
	
	public Puzzle8(int i) {
		w = new int[basicBlocks.size()];
		id = i;
	}
	
	public void run()
	{
		solve(0, 0L);		
	}
	
	private void solve(int pos, long board)
	{
		if (board == -1L){
			printSolution();
			return;
		}
		
		List<Long> list = movedBlocks.get(pos);
		int size = list.size();
		if (pos == 0) {
			for (int i = id; i < size; i+=THREADS) {
				long block = list.get(i);
				if ((board & block) == 0) {
					w[pos] = i;
					solve(pos+1, board | block);
				}
			}
			doneSignal.countDown();
		}
		else
			for (int i = 0; i < size; ++i) {
				long block = list.get(i);
				if ((board & block) == 0) {
					w[pos] = i;
					solve(pos+1, board | block);
				}
			}

	}
	
	static void init() throws FileNotFoundException {
		out = new PrintStream(new File("PuzzleSolution.txt"));
		out.println("init   "+System.currentTimeMillis());
		readBlocks();
		// Do not rotate first asymmetric block to filter out flipped and rotated solutions
		rotatedBlocks.add(Collections.singletonList(basicBlocks.get(0)));
		for (long block : basicBlocks.subList(1, basicBlocks.size()))
			rotatedBlocks.add(rotate(block));
		for (List<Long> rotated : rotatedBlocks)
			movedBlocks.add(moveMany(rotated));
		BigInteger comb = BigInteger.ONE;
		// Shuffle to get first solutions quicker
		for (List<Long> list : movedBlocks) {
			Collections.shuffle(list);
			comb = comb.multiply(BigInteger.valueOf(list.size()));
		}
		out.println("Combinations: "+comb.toString());
	}
	
	private void printSolution() {
		char[] boardChars = new char[64];
		long a = 1L;
		for (int i = 0; i < 64; ++i) {
			for (int j = 0; j < movedBlocks.size(); ++j)
				if ((movedBlocks.get(j).get(w[j]) & a) != 0) {
					boardChars[i] = (char)(65+j);
					break;
				}
			a <<= 1;
		}
		synchronized(out) {
			out.println("found "+Arrays.toString(w));
			for (int i = 0; i < 64; i+=8) {
				for (int j = i; j < i+8; ++j)
					out.print(boardChars[j]);
				out.println("");
			}
			out.flush();
		}
	}

	private static List<Long> moveMany(List<Long> lb) {
		List<Long> lc = new ArrayList<>();
		for (long x : lb)
			lc.addAll(move(x));
		return lc;
	}

	protected static List<Long> move(long block) {
		List<Long> lc = new ArrayList<>();
		long y = block;
		while (true)
		{
			long z = y;
			while (true) {
				lc.add(z);
				// left edge detection
				if ((z & 0x8080808080808080L) != 0)
					break;
				z <<= 1;
			}
			// top edge detection
			if ((y & 0xff00000000000000L) != 0)
				break;
			y <<= 8;
		}	
		return lc;
	}

	protected static List<Long> rotate(long block) {
		List<Long> result = new ArrayList<>();
		long start = block;
		do {
			result.add(block);
			block = rotate90(block);
		} while (block != start);
		block = flip(block);
		for (long oldBlock : result)
			if (oldBlock == block) 
				return result;
		start = block;
		do {
			result.add(block);
			block = rotate90(block);
		} while (block != start);			
		return result;
	}

	protected static long flip(long x) {
		x = ((x >> 24) & 0xffL) +
			((x >> 8) & 0xff00L) +
			((x & 0xff00L) << 8) +
			((x & 0xffL) << 24);
		while ((x & 0xff) == 0 && x != 0)
			x >>= 8;
		return x;
	}

	protected static long rotate90(long block) {
		long edge = leftEdge(block);
		long rotated = 0;
		while (edge != 0) {
			long newLine = 0;
			long q = block;
			while (q != 0) {
				newLine = (newLine << 1) + ((edge & q) == 0 ? 0 : 1);
				q >>= 8;
			}
			rotated = (rotated << 8) | newLine;
			edge >>= 1;
		}
			
		return rotated;
	}

	protected static long leftEdge(long block) {
		long edge = 0x80L;
		while (edge > 0) {
			long f = block;
			while (f != 0) {
				if ((f & 0xff & edge) != 0)
					return edge;
				f >>= 8;
			}
			edge >>= 1;
		}
		return -1L;
	}
	
	protected static void readBlocks() {
		InputStream stream = Puzzle8.class.getResourceAsStream("blocks.txt");
		java.util.Scanner scanner = new java.util.Scanner(stream);
		long j = 0;
		while (scanner.hasNextLine()) {
			String s = scanner.nextLine();
			if (s.length() == 0) {
				basicBlocks.add(j);
				j = 0;
			} else {
				j = (j << 8) + line2byte(s);
			}
		}
		scanner.close();
		if (j != 0)
			basicBlocks.add(j);
	}

	private static long line2byte(String s) {
		long j = 0;
		for (char c : s.toCharArray()) {
			j = (j << 1) + (c == '#' ? 1 : 0);
		}
		return j;
	}
	
}
