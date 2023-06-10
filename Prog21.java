import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Prog21 {

/*+----------------------------------------------------------------------
||
||  Class CSC460MAINPA21
||
||            Author: Todd Noecker
||
||        Assignment: PA2 Part A
||
||            Course: CSC460 Database Design
||
||        Instructor: Dr. Lester McCann
||
||               TAs: Priya, Aayush
||
||  Language and IDE: Java v16.0 written in Eclipse v2021		
||
||          Due Date: 9/21/22
||
||        Objectives:  This program is designed to create an index for a .bin file which will allow fast access speeds to the .bin
||                     through the use of a Linear Hashing algorithm. In this case, we are using a modified algorithm with reduced
||                     complexity. The goal is to show that secondary storage can be used to store very large records and access them
||                     quickly using a hash function if we store our data properly. Further, we look at how an index file can give us
||                     a fast means of accessing data stored elsewhere.
||
||      Requirements:  This program will implement Dr.McCann's Linear Hashing Lite Algorithm, to create a .idx file which
||                     will allow fast access to the associated .bin file. The file must be written to non-volatile storage,
||                     and not be held entirely in memory while processing. Part A must show # of buckets indexed. # bucket with
||                     fewest entries, bucket with most entries, and the mean of all the occupancies across all buckets.
||
||           Issues:   There should be no remaining issues.
||        
||
++-----------------------------------------------------------------------*/
public static void main(String[] args) {
	if (args.length == 0) {
		System.out.println(
				"Error one filepath arguement must be provided\nUse(/home/cs460/fall22/2021-utility-scale-solar-plants) exclude the .csv extension.");
		System.exit(-1);
	}
	// 2021-utility-scale-solar-plants

	System.out.println("Reading file " + args[0]);
	IndexBin binRead = new IndexBin(args[0]);

}
}
