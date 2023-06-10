import java.io.IOException;
import java.io.RandomAccessFile;

public class Entry {
	
	/*+----------------------------------------------------------------------
	 ||
	 ||  Class Entry
	 ||
	 ||         Author:  Todd Noecker (Noted Methods were adapted from Dr. McCann's Example)
	 ||
	 ||        Purpose:  An object of this class holds the field values of one
	 ||                  Entry representing a Record. This Class contains 2 fields
	 ||                  These are a key/value pairing key/EID. These are used in an index structure
	 ||                  to indicate exactly(the byte) where the associated Record can be found.
	 ||					 
	 ||
	 ||  Inherits From:  None.
	 ||
	 ||     Interfaces:  None.
	 ||
	 |+-----------------------------------------------------------------------
	 ||
	 ||      Constants:  No constants(all Max length Strings are treated as variables
	 ||                  and determined individually by method.)
	 ||
	 |+-----------------------------------------------------------------------
	 ||
	 ||   Constructors:  The constructor for this method takes the two values and sets them
	 ||                  The Class is very basic
	 ||
	 ||  Class Methods:  public int getEID()
	 ||                  public int getKey()
	 ||                  public void setKey()
	 ||                  public void setEID()
	 ||                  public void writeToIDX()
	 ||                  public void readFromIDX()
	 ||
	 ++-----------------------------------------------------------------------*/
	
	private int EID; //An associated EID for the Record it references.
	private int key; //The key which indicates a Records position in the .bin file.
	
	public Entry(int EID, int key) {
		this.setEID(EID);
		this.setKey(key);
	}

	//Setters and Getters for this class.
	public int getEID() {
		return EID;
	}

	public int getKey() {
		return key;
	}

	public void setKey(int key) {
		this.key = key;
	}
	

	public void setEID(int eID) {
		EID = eID;
	}
	
	/*---------------------------------------------------------------------
	|  Method writeToIDX (stream)
	|
	|  Purpose:  This method will take the current value in the Entry and simply write the bytes
	|            to the file stream.
	|
	|  Pre-condition:  The stream must be in a specific position set by another method.
	|                  This method just writes to the next location!
	|
	|  Post-condition: 
	|  
	|           Note:  Adapted from Dr. McCann's code.    
	|
	|  Parameters:
	|      stream - the associated filestream of the .idx file.
	|
	|  Returns: None.
	*-------------------------------------------------------------------*/
	public void writeToIDX(RandomAccessFile stream) {



		//Attempt to write Object to the binary file.
		//Going to need to handle it's current position in the file.
		try {
			stream.writeInt(this.key);
			stream.writeInt(this.EID);

		} catch (IOException e) {
			System.out.println("I/O ERROR: Couldn't write to the file;\n\t" + "perhaps the file system is full?");
			System.exit(-1);
		}
		

	}
	
	/*---------------------------------------------------------------------
	|  Method readFromIDX (stream)
	|
	|  Purpose:  This method will take the current value in the Entry and simply reads the bytes
	|            from the file stream.
	|
	|  Pre-condition:  The stream must be in a specific position set by another method.
	|                  This method just reads the next entry! Any Entry!
	|
	|  Post-condition: 
	|  
	|           Note:  Adapted from Dr. McCann's code.    
	|
	|  Parameters:
	|      stream - the associated filestream of the .idx file.
	|
	|  Returns: None.
	*-------------------------------------------------------------------*/
	public String readFromIDX(RandomAccessFile stream) {	

		try {
			this.key = stream.readInt();
			this.EID = stream.readInt();

		} catch (IOException e) {
			System.out.println(
					"I/O ERROR: Couldn't read from the file;\n\t" + "The RAF stream might be out of position.");
			System.exit(-1);
		}
		return (this.toString());
	}
}
