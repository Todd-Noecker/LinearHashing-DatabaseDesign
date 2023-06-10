import java.util.Scanner;

/*+----------------------------------------------------------------------
||
||  Class CSC460MAINPA22
||
||            Author: Todd Noecker
||
||        Assignment: PA2 Part B
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
||        Objectives:  This program is designed to open a .idx file created in Program 2A and an associated .bin file 
||                     created in Program 1. It will use the .idx file as an index on the .bin file to allow for fast
||                     Linear Hash Searches. These files are not modified and only accessed from this program, The program will
||                     allow users to enter EID values which are then searched for in .idx file and if found the values are accessed then
||                     printed from the .bin file.
||                     
||
||      Requirements:  This program will implement Dr.McCann's Linear Hashing Lite Algorithm, and carry out searches on the EID passed during the
||                     input loop to determine if the value exists within the DB record. 
||
||           Issues:  No remaining issues.
||
++-----------------------------------------------------------------------*/
public class Prog22 {

	public static void main(String[] args) {

			if(args.length < 2 || args.length > 3) {
				System.out.println("Error the program needs exactly two arguements. These should be lhl.idx and 2021-utility-scale-solar-plants");
				System.exit(-1);
			}
			String arg1 = args[0];
			String arg2 = args[1];
			
			System.out.println("Reading from " + arg2 + " and " + arg1 + "\n");

			IndexBin inBin = new IndexBin(arg2, arg1);
			checkEIDs(inBin);

	}
	
	/*---------------------------------------------------------------------
	|  Method checkEIDs(binRead)
	|
	|  Purpose:  This method will take the passed EID value and initiate a binary search on the
	|            selected int value. If the record is found it is returned to be printed, if
	|            the record is not found, a default "Record Not Found" message is printed.
	|           
	|
	|  Pre-condition: An opened ParseBin Object containing the .bin RAF. 
	|
	|  Post-condition: Bin file access is left open. Search has or has returned a string
	|                  with either a missing record message or the record data.
	|  
	|
	|  Parameters: IndexBin binRead- the associated bin to be used to access the files within.
	|
	|  Returns: None
	*-------------------------------------------------------------------*/
	private static void checkEIDs(IndexBin binRead) {

		Scanner input = new Scanner(System.in);
		int EIDsearched = 0;
		System.out.print("\n\nSearching the generated .idx Index file...\n");
		// Outer read loop for EIDs
		while (EIDsearched != -1) {
			// Inner loop to handle bad inputs
			do {
				System.out.print("\nPlease enter an EID value between 1-99999\n");
				while (!input.hasNextInt()) {
					String wrong = input.next();
					System.out.println(wrong + " is not a valid EID.\n");
				}
				EIDsearched = input.nextInt();
			} while (EIDsearched < -1);

			// The search has been ended.
			if (EIDsearched == -1) {
				System.out.println("Have a great day.\nThanks for grading!\n");
				System.exit(0);
			}
			if (EIDsearched >= 1 && EIDsearched <= 99999) {
				//This is essentially the exact same loop from project 1 but now,
				//the checkEID method will call a linear hashing function to find
				//The associated EID.
				binRead.checkEID(EIDsearched);
			} else {
				System.out.println("Searched int is " + EIDsearched + " is not in a valid range 1-99999.\n");
			}

		}
	}
}
