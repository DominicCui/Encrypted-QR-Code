package reader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;

public class decrypt {
	
	public static int round = 10;
	public static int totalBits;
	public static int segment;

	public static String message;
	public static String[] messages;
	
	public static BigInteger[] result_i;
	public static String[] d_keys;
	public static String d_key;
	public static String seed;
	public static String[][] d_seed;
	
	public static int initialX = 5;
	public static int initialY = 6;
	public static int modulo = 63;//4051;

	public static byte[] original;
	public static BigInteger[] original_int;
	
	public static void process(byte[] encode) {
		
		try {
			readfile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		d_seed(8);

		message = new String();
		for (int i = 0; i < encode.length; i++) {
			message += String.format("%8s", Integer.toBinaryString(encode[i] & 0xff)).replace(' ', '0');
		}

		totalBits = message.length();
		segment = chooseSeg();
		
		inSegment(message);
		d_key(initialX, initialY, ((totalBits / 2) * round));
		d_decrypt(encode,d_keys);	
	}
	
	public static void d_decrypt(byte[] modify, String[] key) {

		//get encoding bytes and re segment to crypto segments		
		String b = new String();
		for (int i = 0; i < modify.length; i++) {
			b += String.format("%8s", Integer.toBinaryString(modify[i] & 0xff)).replace(' ', '0');
		}
		inSegment(b);

		original_int = new BigInteger[messages.length];
		for (int i = 0; i < messages.length; i++) {
			original_int[i] = new BigInteger(messages[i], 2);
		}

		int count = round-1;
		// Shuffle
		for (int i = original_int.length - 1; i > 0; i -= 2) {
			BigInteger num = new BigInteger(key[(i - 1) / 2 + (count * segment / 2)], 2);
			int s2 = (int) (Math.abs((Math.pow(2, segment) - num.intValue())) % segment);
			BigInteger temp2 = original_int[i];
			original_int[i] = original_int[s2];
			original_int[s2] = temp2;

			int s1 = Math.abs(num.intValue()) % segment;
			BigInteger temp1 = original_int[i - 1];
			original_int[i - 1] = original_int[s1];
			original_int[s1] = temp1;
		}
		 
		// XOR
		for (int i = original_int.length - 1; i >= 0; i -= 2) {
			BigInteger temp = original_int[i];
			original_int[i] = original_int[i - 1];
			original_int[i - 1] = temp;
			BigInteger xb = new BigInteger(key[(i - 1) / 2 + (count * segment / 2)], 2);

			original_int[i - 1] = original_int[i].xor(original_int[i - 1]);
			original_int[i] = original_int[i].xor(xb);
		}
		
		//re segment to 8bits
		String bin = new String();

		for (int i = 0; i < original_int.length; i++) {
			bin+=String.format("%" + totalBits/segment + "s", original_int[i].toString(2)).replace(' ', '0');
		}

		//original sequence;
		original = new byte[totalBits / 8];
		for (int i = 0, j = 0, l = bin.length(); i < l; i += 8, j++) {
			original[j] = new BigInteger(bin.substring(i, Math.min(l, i + 8)), 2).byteValue();
		}
	}
	
	private static String[] d_key(int row, int col, int length) {
		int count = 1;
		d_key = new String();
		d_key = d_seed[row][col];
		int start = row+col;
		
		while(count < length) {
			int r = (int) (Math.pow(start, 2)%modulo);
			int d =  r%8;

			switch(d) {
			case 0:
				if(col==7)
					col=-1;
				d_key += d_seed[row][col+1];
				col +=1;
				break;
			case 1:
				if(row==0)
					row=8;
				if(col==7)
					col=-1;
				d_key += d_seed[row-1][col+1];
				row-=1;
				col+=1;
				break;
			case 2:
				if(row==0)
					row=8;
				d_key += d_seed[row-1][col];
				row-=1;
				break;
			case 3:
				if(row==0)
					row=8;
				if(col==0)
					col=8;
				d_key += d_seed[row-1][col-1];
				row-=1;
				col-=1;
				break;
			case 4:
				if(col==0)
					col=8;
				d_key += d_seed[row][col-1];
				col-=1;
				break;
			case 5:
				if(row==7)
					row=-1;
				if(col==0)
					col=8;
				d_key += d_seed[row+1][col-1];
				row+=1;
				col-=1;
				break;
			case 6:
				if(row==7)
					row=-1;
				d_key += d_seed[row+1][col];
				row+=1;
				break;
			case 7:
				if(row==7)
					row=-1;
				if(col==7)
					col=-1;
				d_key += d_seed[row+1][col+1];
				row+=1;
				col+=1;
				break;
			}
			count++;
			start++;
		}

		//divid into segment
		segment = chooseSeg();
		int size = totalBits / segment;
		int chunks = d_key.length() / size + ((d_key.length() % size > 0) ? 1 : 0);
		d_keys = new String[chunks];
		
		for (int i = 0, j = 0, l = d_key.length(); i < l; i += size, j++) {
			d_keys[j] = d_key.substring(i, Math.min(l, i + size));
		}
		return d_keys;	
	}

	public static String[] inSegment(String message) {
		int size = totalBits / segment;
		int chunks = message.length() / size + ((message.length() % size > 0) ? 1 : 0);
		messages = new String[chunks];
		
		for (int i = 0, j = 0, l = message.length(); i < l; i += size, j++) {
			messages[j] = message.substring(i, Math.min(l, i + size));
		}
		return messages;
	}
	
	public static int chooseSeg() {
		int seg = (int) Math.floor(Math.sqrt(totalBits));

		while (totalBits % seg != 0)
			seg--;

		if (seg % 2 == 0)
			return seg;
		else
			return seg = totalBits / seg;
	}
	
	public static String[][] d_seed(int size){
		d_seed = new String[size][size];
		
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				d_seed[i][j] = Character.toString(seed.charAt(i*size+j));
			}
		}
		return d_seed;
	}
	private static void readfile() throws IOException {
		BufferedReader reader2 = new BufferedReader(new FileReader("new_seed.txt"));
		seed = reader2.readLine();
		reader2.close();
	}
}
