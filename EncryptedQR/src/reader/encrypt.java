package reader;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Random;

public class encrypt {

	public static int round = 10;
	public static int totalBits;
	public static int segment;
	
	public static String[] XorKey;
	public static String[] messages;
	public static BigInteger[] result;
	public static BigInteger[] result_int;
	public static byte[] result_b;
	public static byte[] original;

	public static String[][] d_seed;
	public static String d_key;
	public static int initialX = 5;
	public static int initialY = 6;
	public static int modulo = 63;//4051;
	public static String[] d_keys;
		
	public static void process(String mess) {
		
		totalBits = mess.length();
		
		try {
			d_seed(8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		d_key = new String();
		d_key(initialX, initialY, ((totalBits / 2) * round));
		inSegment(mess);
		d_encrypt(messages, d_keys);
	}

	private static void d_encrypt(String[] message, String[] key){		 
		result_int = new BigInteger[messages.length];
		int count = 0;
		while (count < round) {
			// XOR
			for (int i = 1; i < messages.length; i += 2) {
				BigInteger xa = new BigInteger(messages[i], 2);
				BigInteger xb = new BigInteger(key[(i - 1) / 2 + (count * segment / 2)], 2);

				result_int[i] = xa.xor(xb);
				BigInteger xc = new BigInteger(messages[i - 1], 2);
				result_int[i - 1] = (xc.xor(result_int[i]));

				BigInteger temp = result_int[i];
				result_int[i] = result_int[i - 1];
				result_int[i - 1] = temp;
			}
			// Shuffle
			for (int i = 1; i < messages.length; i += 2) {
				BigInteger num = new BigInteger(key[(i - 1) / 2 + (count * segment / 2)], 2);
				int s1 = Math.abs(num.intValue()) % segment;
				BigInteger temp1 = result_int[i - 1];
				result_int[i - 1] = result_int[s1];
				result_int[s1] = temp1;

				int s2 = (int) (Math.abs((Math.pow(2, segment) - num.intValue())) % segment);
				BigInteger temp2 = result_int[i];
				result_int[i] = result_int[s2];
				result_int[s2] = temp2;
			}
			count++;
		}
		
		//re segment encoding result to 8 bits;	
		String bin = new String();
		
		for (int i = 0; i < result_int.length; i++) {
			bin+=String.format("%" + totalBits/segment + "s", result_int[i].toString(2)).replace(' ', '0');
		}

		result = new BigInteger[totalBits / 8];
		result_b = new byte[totalBits/8];
		
		for (int i = 0, j = 0, l = bin.length(); i < l; i += 8, j++) {
			result[j] = new BigInteger(bin.substring(i, Math.min(l, i + 8)), 2);
			result_b[j] = result[j].byteValue();
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

	public static String[][] d_seed(int size) throws IOException {
		d_seed = new String[size][size];
		BufferedWriter writer = new BufferedWriter(new FileWriter("new_seed.txt"));
		
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				d_seed[i][j] = keyGenerate(1);				 
			    writer.append(d_seed[i][j]);			    
			}
		}
		writer.close();
		return d_seed;
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

	public static String keyGenerate(int length) {
		Random random = new Random();
		boolean res;
		String randomBinary = "";
		for (int i = 0; i < length; i++) {
			res = random.nextBoolean();
			if (res)
				randomBinary = randomBinary.concat("1");
			else
				randomBinary = randomBinary.concat("0");
		}
		return randomBinary;
	}
}
