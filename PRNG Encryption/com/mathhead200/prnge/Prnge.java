package com.mathhead200.prnge;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;


/**
 * PRNGE (Pseudo Random Number Generated Encryption) <br>
 * (Pronounced like "ringe" because the 'p' is silent in "Pseudo")
 * @author Christopher D'Angelo <br>
 * 	www.mathhead200.com
 * @version 1.0.0 (1/20/2012)
 */
public class Prnge
{
	private final String key;
	private final int bloatFactor;
	private final int chunkSize;
	private Random rand;
	private Random junk = new Random();

	private final void init() {
		long s = 0;
		char[] cs = key.toCharArray();
		for( char c : cs )
			s = 127L * s + c;
		rand = new Random(s);
	}

	public Prnge(String key, int bloatFactor, int chunkSize) {
		this.key = key;
		this.bloatFactor = bloatFactor;
		this.chunkSize = chunkSize;
		init();
	}

	public Prnge(String key) {
		this(key, 1, 1024);
	}

	public void reset() {
		init();
	}

	public void encrypt(InputStream in, OutputStream out) throws IOException {
		byte[] data = new byte[chunkSize];
		byte[] cypher = new byte[chunkSize];
		byte[] pseudoData = new byte[chunkSize];
		for( int n = in.read(data); n > 0; n = in.read(data) ) {
			assert n <= chunkSize;
			//Generate cypher
			rand.nextBytes(cypher);
			//Encrypt (real) data
			for( int i = 0; i < n; i++ )
				data[i] ^= cypher[i];
			//Output (fake) pre-data
			int r = rand.nextInt(bloatFactor);
			for( int i = 0; i < r; i++ ) {
				junk.nextBytes(pseudoData);
				out.write(pseudoData);
			}
			//Output (real) data
			out.write(data, 0, n);
			//Output (fake) post-data
			r = rand.nextInt(bloatFactor);
			for( int i = 0; i < r; i++ ) {
				junk.nextBytes(pseudoData);
				out.write(pseudoData);
			}
		}
	}

	public void decrypt(InputStream in, OutputStream out) throws IOException {
		byte[] data = new byte[chunkSize];
		byte[] cypher = new byte[chunkSize];
		while( in.available() > 0 ) {
			//Get cypher
			rand.nextBytes(cypher);
			//Get (fake) pre & post data sizes
			long pre = (long)rand.nextInt(bloatFactor) * (long)chunkSize,
				 post = (long)rand.nextInt(bloatFactor) * (long)chunkSize;
			//Skip (fake) pre-data
			if( in.skip(pre) != pre )
				throw new DecryptionException();
			//Read (real) data
			int n = in.read(data);
			assert n <= chunkSize;
			if( n < chunkSize ) {//reached eof
				if( n < 0 )
					n = 0;
				n -= (int)post;
				if( n < 0 || post > Integer.MAX_VALUE )
					throw new DecryptionException();
			} else {
			//Skip post-data
				long s = in.skip(post);
				if( s != post ) { //some (fake) post data in (real) data
					if( s < 0L )
						s = 0L;
					long d = post - s;
					n -= (int)d;
					if( n < 0 || d > Integer.MAX_VALUE )
						throw new DecryptionException();
				}
			}
			//Decrypt (real) data
			for( int i = 0; i < n; i++ )
				data[i] ^= cypher[i];
			//Output (real) data
			out.write(data, 0, n);
		}
	}
}
