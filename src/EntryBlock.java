import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;


/*+----------------------------------------------------------------------
 ||
 ||  Class EntryBlock
 ||
 ||         Author:  Todd Noecker 
 ||
 ||        Purpose:  An object of this class will hold 20 record entries and one additional int
 ||                  for its current capacity. The Entryblock has a size of 164 bytes. To minimize
 ||                  I/O the Class features methods to read and write entire chunks(buckets) in the
 ||                  .bin index file.
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
 ||   Constructors:  The constructor for this method initializes all variables to
 ||                  the suggested values from the spec. The constructor can only be
 ||                  Initialized with a valid EID(only 0-9 with no additional chars).
 ||                  All values are initialized to default values
 ||
 ||  Class Methods:  public int getRecCount()
 ||                  public ArrayList<Entry> getList()
 ||                  public void setRecCount()
 ||                  public void getRecCount()
 ||                  
 ||
 ++-----------------------------------------------------------------------*/

public class EntryBlock {
	
	//These are used to avoid hard coded values in my project. These represent
	//The byte sizes of various parts of the lhl.idx file.
	private static int BUCKETSIZE = 20;
	private static int ENTRYSIZE = 8;
	private static int BLOCKSIZE = (BUCKETSIZE * ENTRYSIZE); 
	

	ArrayList<Entry> currentBlock = null;
	private Entry current;
	private int recordCount;
	
	public EntryBlock(RandomAccessFile stream) {
		currentBlock = new ArrayList<Entry>();
		recordCount = 0;
		current = new Entry(0,0);
	}
	
	
	//Setters and Getters for the class.
	public int getRecCount() {
		return this.recordCount;
	}
	
	public ArrayList<Entry> getList(){
		return currentBlock;
	}
	
	public void setRecCount(int count) {
		this.recordCount = count;
	}
	
	
	  /*---------------------------------------------------------------------
    |  Method readBlock (stream, position)
    |
    |  Purpose:  This method will read an entire EntryBlock from the RAF to the
    |            system memory as an EntryBlock Object. The method will read through 164
    |            bytes representing the 41 ints that comprise a EntryBlock.
    |
    |  Pre-condition:  Stream must be on initial byte 0 for first write and
    |                  must write a consistent length for each Object entry.
    |
    |  Post-condition: The stream has been read 164 bytes further/deeper.
    |
    |  Parameters:
    |      stream- The indicated file stream, in this case containing the lhl.idx RAF
    |      position - this indicated the current "index" of the file stream.
    |
    |  Returns: an EntryBlock object populated with values from the associated position.
    *-------------------------------------------------------------------*/
	public EntryBlock readBlock(RandomAccessFile stream, int position) {
		int currentCount = 0;
		currentBlock =  new ArrayList<Entry>();
		
		try {
			int seekVal = (position *160) + (160 + ((position)*4));
			stream.seek(seekVal);
			currentCount = stream.readInt();
			stream.seek((position*160) + (position*4));
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Had an issue trying to seek. Perhaps you went too far into the file.\n");
		}
		this.recordCount = 0;
		for(int i  = 0; i < currentCount; i ++) {
			current = new Entry(0, 0);
			current.readFromIDX(stream);
			currentBlock.add(current);
			this.recordCount++;
		}
		
		return this;
	}
	
	  /*---------------------------------------------------------------------
    |  Method writeBlock (stream, position)
    |
    |  Purpose:  This method is used to write the entire EntryBlock object into
    |            a .bin file on disk. The structure of the EntryBlock exists s.t.
    |            164 byte chunks will be written, and then a final int is written
    |            to indicate the size of the current block.
    |
    |  Pre-condition:  The position of the stream is adjusted on entry to the method and
    |                  does not impact functionality.
    |
    |  Post-condition: The file is created in the current directory after the
    |                  last record is written. The final line of the file
    |                  contains the max field length for the 3 String fields.
    |                  The file has the same content as the csvContent list.
    |  
    |           Note:  Adapted from Dr. McCann's code.    
    |
    |  Parameters:
    |      stream- The indicated file stream, in this case containing the lhl.idx RAF
    |      position - this indicated the current "index" of the file stream.
    |
    |  Returns: an EntryBlock object populated with values from the associated position.
    *-------------------------------------------------------------------*/
	public void writeBlock(RandomAccessFile stream, int position) {
		//There exists a EntryBlock every 164 bytes.
		int seekVal = (position *164);
		try {
			stream.seek(seekVal);
			int count = this.getRecCount();
			int blanks = 20 - count;
			//Write all actual entries that exist in the Block.
			for(int i = 0; i < count; i++) {
				this.getList().get(i).writeToIDX(stream);
			}
			//Write all additional spaces with 0's
			for(int i = 0; i < blanks; i++) {
				Entry adder = new Entry(0,0);
				adder.writeToIDX(stream);
			}
			//The stream should be on byte position*160 meaning we are at the correct location to write
			//The count.
			stream.writeInt(count);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Had an issue trying to seek. Perhaps you went too far into the file.\n");
		}
	}

	
	
	/*---------------------------------------------------------------------
	|  Method writeEntry (stream, maxName, maxState, maxCOD)
	|
	|  Purpose:  This method is used to write a single entry to the .bin file. This method helps
	|            to minimize I/O by only reading the associated capacity int from the EntryBlock.
	|            This value is used to determine the next usable space inside a block inside the
	|            .idx file. 
	|
	|  Pre-condition:  The stream will be in its initial write of the values. 
	|
	|  Post-condition: The file is created in the current directory after the
	|                  last record is written. The final line of the file
	|                  contains the max field length for the 3 String fields.
	|                  The file has the same content as the csvContent list.
	|  
	|           Note:  Adapted from Dr. McCann's code.    
	|
	|  Parameters:
	|      stream- The indicated file stream, in this case containing the lhl.idx RAF
	|      position - this indicated the current "index" of the filestream
	|
	|  Returns: an EntryBlock object populated with values from the associated position.
	*-------------------------------------------------------------------*/
	public void writeEntry(RandomAccessFile stream, int position, int EID, int key) {
		int currentCount = 0;
		current.setEID(EID);
		current.setKey(key);
		try {
			//Determine the current position of the size indicator for the current bucket.
			int seekVal = position*BLOCKSIZE + (BLOCKSIZE+(position*4));
			stream.seek(seekVal);
			currentCount = stream.readInt();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Had an issue trying to seek. Perhaps you went too far into the file.\n");
		}
		
		try {
			//Go to the current free record position. (ENTRYSIZE*currentCount) gives us the specific 
			//location to write the entry.
			int seekVal = (position*BLOCKSIZE + ((position*4)) + (ENTRYSIZE*currentCount));
			stream.seek(seekVal);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.out.println("Had an issue trying to seek. Perhaps you went too far into the file.\n");
		}
		
		try {
			//Write the actual object to .idx file.
			stream.writeInt(current.getKey());
			stream.writeInt(current.getEID());
			currentCount++;
			//Write the new current count to the correct position for this bucket.
			int seekVal = position*BLOCKSIZE + (BLOCKSIZE+(position*4));
			stream.seek(seekVal);
			stream.writeInt(currentCount);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Had an issue trying to seek. Perhaps you went too far into the file.\n");
		}
	}


}
