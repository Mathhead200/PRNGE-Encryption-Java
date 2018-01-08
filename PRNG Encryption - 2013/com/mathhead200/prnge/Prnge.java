package com.mathhead200.prnge;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Random;


/**
 * PRNGE (Pseudo Random Number Generated Encryption) <br>
 * (Pronounced like "ringe" because the 'p' is silent in "Pseudo")
 * @author Christopher D'Angelo <br>
 * 	www.mathhead200.com
 * @version 2.0.0 (8/15/2013)
 */
public class Prnge
{
	private static final double DEFAULT_BLOAT_FACTOR = 1.3;
	private static final int DEFAULT_CHUNK_SIZE = 1024;

	private final byte[] seed;
	private final double bloatFactor;
	private final int chunkSize;
	private SecureRandom rand;
	private Random junk = new SecureRandom();


	public Prnge(int seedSize, double bloatFactor, int chunkSize) {
		try {
			rand = SecureRandom.getInstance("SHA1PRNG", "SUN");
		} catch (NoSuchAlgorithmException e) {
			throw new Error(e);
		} catch (NoSuchProviderException e) {
			throw new Error(e);
		}
		seed = rand.generateSeed(seedSize);
		rand.setSeed(seed);
		this.bloatFactor = bloatFactor;
		this.chunkSize = chunkSize;
	}

	public Prnge(byte[] seed, double bloatFactor, int chunkSize) {
		try {
			rand = SecureRandom.getInstance("SHA1PRNG", "SUN");
		} catch (NoSuchAlgorithmException e) {
			throw new Error(e);
		} catch (NoSuchProviderException e) {
			throw new Error(e);
		}
		this.seed = seed;
		rand.setSeed(this.seed);
		this.bloatFactor = bloatFactor;
		this.chunkSize = chunkSize;
	}

	public Prnge(String key, double bloatFactor, int chunkSize) {
		try {
			rand = SecureRandom.getInstance("SHA1PRNG", "SUN");
		} catch (NoSuchAlgorithmException e) {
			throw new Error(e);
		} catch (NoSuchProviderException e) {
			throw new Error(e);
		}
		seed = new byte[key.length()];
		for( int i = 0; i < key.length(); i++ )
			seed[i] = (byte)key.charAt(i); //assume ASCII char
		rand.setSeed(seed);
		this.bloatFactor = bloatFactor;
		this.chunkSize = chunkSize;
	}

	public Prnge(int seedSize) {
		this( seedSize, DEFAULT_BLOAT_FACTOR, DEFAULT_CHUNK_SIZE );
	}

	public Prnge(byte[] seed) {
		this( seed, DEFAULT_BLOAT_FACTOR, DEFAULT_CHUNK_SIZE );
	}

	public Prnge(String key) {
		this( key, DEFAULT_BLOAT_FACTOR, DEFAULT_CHUNK_SIZE );
	}


	public byte[] getSeed() {
		return seed.clone();
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
			int r = (int)(bloatFactor * rand.nextDouble());
			for( int i = 0; i < r; i++ ) {
				junk.nextBytes(pseudoData);
				out.write(pseudoData);
			}
			//Output (real) data
			out.write(data, 0, n);
			//Output (fake) post-data
			r = (int)(bloatFactor * rand.nextDouble());
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
			long pre = (int)(bloatFactor * rand.nextDouble()) * (long)chunkSize,
				 post = (int)(bloatFactor * rand.nextDouble()) * (long)chunkSize;
			//Skip (fake) pre-data
			if( in.skip(pre) != pre )
				throw new DecryptionException();
			//Read (real) data
			int n = in.read(data);
			assert n <= chunkSize;
			if( n < chunkSize ) {//reached eof
				if( n < 0 )
					n = 0;
				if( post > n )
					throw new DecryptionException();
				n -= (int)post;
			} else {
			//Skip (fake) post-data
				long s = in.skip(post);
				if( s != post ) { //some (fake) post-data in (real) data
					if( s < 0L )
						s = 0L;
					long d = post - s;
					if( d > n )
						throw new DecryptionException();
					n -= (int)d;
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
