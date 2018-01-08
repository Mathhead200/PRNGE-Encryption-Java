package com.mathhead200.prnge;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;


/**
 * @author Christopher D'Angelo <br>
 * 	www.mathhead200.com
 * @version 1.0.0
 */
public class PrngeTUI
{
	public static final BufferedReader STDIN = new BufferedReader( new InputStreamReader(System.in) );

	public static void main(String[] args) {
		boolean enc = true;
		int bloatFactor = 3;
		int chunkSize = 1;
		try {
			enc = args[0].matches("\\b(en?|enc(rypt)?)\\b");
			if( !(enc || args[0].matches("\\b(de?|dec(rypt)?)\\b")) )
				throw new IllegalArgumentException( "What is \"" + args[0] + "\"?" );
			bloatFactor = Integer.parseInt( args[1] );
			chunkSize = Integer.parseInt( args[2] );
		} catch(ArrayIndexOutOfBoundsException e) {
		} catch(IllegalArgumentException e) {
			System.err.print("Unrecognized argument: ");
			System.err.println(e.getMessage());
			System.exit(1);
		}

		try {
			System.out.print("Input Filename: ");
			InputStream fin = new FileInputStream( STDIN.readLine() );

			System.out.print("Key: ");
			Prnge prnge = new Prnge( STDIN.readLine(), bloatFactor, chunkSize );

			System.out.print("Output Filename: ");
			OutputStream fout = new FileOutputStream( STDIN.readLine() );

			if( enc ) {
				System.out.println("Encrypting...");
				prnge.encrypt(fin, fout);
			} else {
				System.out.println("Decrypting...");
				prnge.decrypt(fin, fout);
			}
			System.out.println("Done.");
		} catch(IOException e) {
			System.err.print("IO Error: ");
			System.err.println(e.getMessage());
			System.exit(2);
		}
	}
}
