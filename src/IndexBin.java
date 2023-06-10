import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

/*+----------------------------------------------------------------------
||
||  Class IndexBin
||
||         Author:  Todd Noecker (Noted Methods were adapted from Dr. McCann's Example)
||
||        Purpose:  This class is used to access and create .idx files from the associated
||                  .bin structure of 
||
||  Inherits From:  None.
||
||     Interfaces:  None.
||
|+-----------------------------------------------------------------------
||
||      Constants:  None
||
|+-----------------------------------------------------------------------
||
||   Constructors:  This method contains 2 constructors. The first is used in construction and takes only
||                  the .bin file path. The second takes two arguments The filename for the index "lhl.idx"
||                  and the path to the .bin file.
||
||  Class Methods:  private void openIndex();
||                  private void aquireMaxVals()
||                  private void validateFiles()
||                  private void aquireMaxVals()
||                  private void createIndex()
||                  private void openIndex()
||                  private void readCounts()
||                  private void writeToIDX()
||                  private void checkEID()
||                  private void doubleBuckets()
||                  private void updatePosition()
||
++-----------------------------------------------------------------------*/
public class IndexBin {
	
	//Constants
	private static String IDXPATH = "lhl.idx"; //The given path to the .idx file.
	private static int BUCKETSIZE = 20; //The size of one bucket measured in entries
	private static int ENTRYSIZE = 8; //The size of one Entry measured in bytes.
	private static int BLOCKSIZE = BUCKETSIZE * ENTRYSIZE + 4; //The total size for 1 Block
	//used to traverse through the file.
	
	private String binPath; //The given path to the associated .bin file for reading.	
	private Record current; //An object used generally to read in the current Record from the .bin
	private RandomAccessFile fileBin; //the RAF currently accessing the .bin file.
	private RandomAccessFile indexBin; //the RAF currently accessing lhl.idx file.
	private EntryBlock currBlock; //The currently used EntryBlock object.
	
	//The final line of the .bin will contain 3 int values representing the following 3 ints in order.
	//The numRecords and recordLen are based on those values and the fileBin.length()/
	private int maxName; //The maximum found name String length in the .bin file.
	private int maxCOD; //The maximum found COD String length in the .bin file.
	private int maxState; //The maximum found state String length in the .bin file.
	private int hVal; //This is used to represent the current depth of the .idx bucket structure. This value is primarily used for
	                  //hashing, but generally relates to the number of entries in the .bin file.
	private long numRecords; //The calculated number of records in the file. Based on the associated max size of Strings in the file.
	private int recordLen; //The standard record length in the file. maxName+maxCOD+maxState+44(The remaining 5 doubles(8) + 1 int(4))
	
	
	public IndexBin(String path) {
		this.binPath = path;
		this.hVal = -1;
		validateFile();
		aquireMaxVals();
		createIndex();
		readCounts();
	}
	
	//Overloaded constructor to handle Program 2b requirements.
	public IndexBin(String path, String extension) {
		this.binPath = path;
		this.hVal = 0;
		validateFile();
		aquireMaxVals();
		openIndex(extension);
		readCounts();

	}

	/*---------------------------------------------------------------------
    |  Method openIndex (extension)
    |
    |  Purpose:  This method is used to just open the .idx file passed from Prog22
    |            Using the conditions of the .idx file bucket size, a Hval is determined
    |            which will be used to access the buckets that exist within the .idx file.
    |
    |  Pre-condition:  RAF file must be ready for reading and seeking.
    |
    |  Post-condition: None
    |
    |  Parameters: extension The passed file extension for the purposes of this project should always be
    |              "lhl.idx"
	|
    |  Returns: None.
    *-------------------------------------------------------------------*/
	private void openIndex(String extension) {
		
		try {
			indexBin = new RandomAccessFile(extension, "r");
			int toHVal = 0;
			toHVal = (int) (indexBin.length()/BLOCKSIZE);
			for(int i =0; i < toHVal; i++) {
				if(toHVal == Math.pow(2, i+1)) {
					hVal = i;
					return;
				}
			}

		} catch (IOException e) {
			System.out.println("I/O ERROR: Something went wrong with the " + "opening of the RAF .idx file.");
			System.exit(-1);
		}
		
	}

	/*---------------------------------------------------------------------
    |  Method validateFile ()
    |
    |  Purpose:  This method is called by the Constructor to determine if the passed
    |            String indicating file location is valid, .csv is added to the file name.
    |            If the filename is valid and the .csv is found, the file is opened for 
    |            processing.
    |
    |  Pre-condition:  File location provided must be without .csv extension, file
    |                  must exist.
    |
    |  Post-condition: The file will be generated for the class, this fill will then be
    |                  used as a stream for the .bin read method.
    |
    |  Parameters: None
	|
    |  Returns: None.
    *-------------------------------------------------------------------*/
	private void validateFile() {
		current = new Record(00000);
		if(binPath.contains("/")) {
			String[] lastPart = binPath.split("/");
			int i = lastPart.length;
			binPath = lastPart[i - 1];
		}
		try {
            fileBin = new RandomAccessFile(binPath +".bin","r");
        } catch (IOException e) {
            System.out.println("I/O ERROR: Something went wrong with the "
                             + "opening of the RAF .bin file.");
            System.exit(-1);
        }
	}
	
	
	/*---------------------------------------------------------------------
    |  Method aquireMaxVals()
    |
    |  Purpose:  This method is called to extract the final 3 records from the .bin file
    |            these contain the 3 String lengths fore the associated maxName, maxState,
    |            and maxCOD values. These values are used to determine the length of the actual
    |            Record Object for printing/displaying/writing.
    |
    |  Pre-condition:  RAF file must be open and in any position.
    |
    |  Post-condition: None
    |
    |  Parameters: None
	|
    |  Returns: None.
    *-------------------------------------------------------------------*/
	private void aquireMaxVals() {
		
		//In an effort to avoid constants, these are the byte values of the final line of the .bin file
		//3*(int=4bytes) = 12 bytes
		int threeIntByteVal = 12;
		//5*(double=8bytes) + 1*int = 44 bytes
		int numberColsByteVal = 44;
		
		try {

			fileBin.seek(fileBin.length() - threeIntByteVal);
			this.maxName = fileBin.readInt();
			this.maxState = fileBin.readInt();
			this.maxCOD = fileBin.readInt();
			this.recordLen = maxName + maxCOD + maxState + numberColsByteVal;
	        numRecords = fileBin.length() / recordLen; 
			fileBin.seek(0);
		} catch (IOException e) {
			System.out.println("Unable to read the maximum String size values. Maybe you got your record byte lengths wrong.\n");
			e.printStackTrace();
		}
	}
	
	
	
	 /*---------------------------------------------------------------------
    |  Method createIndex()
    |
    |  Purpose:  This method will take a .bin file and create .idx file with a modified 
    |            version of Linear Hashing suggested by Dr. McCann. This method will take each
    |            entry from the .bin file and attempt to store it in .idx based on a hash value.
    |            
    |			 The hashing processing is as follows: Begin with 2 20 bucket bins for Record EID
    |            storage. Using the formula to derive keys k = EID mod(2^(H+1), with an H of zero
    |            which increases every time a full bucket needs to be added to. (E.G. after 21 even 
    |            or odd records there will be 4 bins with 80 buckets total.)
    |           
    |
    |  Pre-condition: The .bin file must be constructed with 9 fields matching the .csv structure
    |                 from 2021-utility-scale-solar-plants.
    |
    |  Post-condition: An unmodified .bin and the creation of a file lhl.idx which will have a full index of the .bin
    |                  file.
    |  
    |
    |  Parameters: None
    |
    |  Returns: None
    *-------------------------------------------------------------------*/
		private void createIndex() {
			int indexCount = 0;

			try {
				indexBin = new RandomAccessFile(IDXPATH, "rw");
			} catch (IOException e) {
				System.out.println("I/O ERROR: Something went wrong with the " + "opening of the RAF .idx file.");
				System.exit(-1);
			}

			currBlock = new EntryBlock(indexBin);
			writeBlank(indexBin);
		

		// Main read loop from the passed .bin file.
		try {
			for (int i = 0; i < (fileBin.length() / recordLen); i++) {
				current.readEntry(fileBin, maxName, maxState, maxCOD);
				writeToIDX(current.getEid(), indexCount);
				indexCount++;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	/*---------------------------------------------------------------------
	|  Method readCounts()
	|
	|  Purpose:  This method acts as the requested stats method by Dr. McCann. The purpose of the method is to derive
	|            and display the Minimum bucket count(occupancy), Maximum bucket count(occupancy), Mean occupancy of all buckets,
	|            and the total count of buckets. This method will work through each of the stored capcity ints every 160 bytes one
	|            exists eg 160, 324, ... So that minimal access can determine these values.
	|           
	|
	|  Pre-condition: The .bin file can be in any state, however the stats will not be representative of the actual counts
	|                 unless it is run after complete write/expansion of the .idx file.
	|
	|  Post-condition: An unmodified .bin and the creation of a file lhl.idx which will have a full index of the .bin
	|                  file.
	|  
	|
	|  Parameters: None
	|
	|  Returns: None
	*-------------------------------------------------------------------*/
	private void readCounts() {
		ArrayList<Integer> counts = new ArrayList<Integer>();
		int blockMax = (int) (Math.pow(2, hVal + 1));
		int indexMax = blockMax - 1;//The position of the end of the .idx buckets.
		long currentPos = 0; //Current position
		int currCount = 0; //The current count in use.
		//A value to be replaced and ultimately printed. Initial value is not in a valid range for this DB.
		int lowCount = 21;//The low count
		int lowBucket = -1;//the bucket containing the lowest count
		int highBucket = 21212;//the bucket containing the highest count
		int highCount = 0;//the count of the bucket with the most entries.
		try {
			currentPos = indexBin.getFilePointer();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//This section is reading off each of the associated bucketCounts to determine
		// The various values for the final print (max count,min count, avg, total buckets)
		//These values are stored and used to compute a mean.
		for (int i = 0; i <= indexMax; i++) {
			int seekVal = (i * 160) + (160 + ((i) * 4));
			try {
				indexBin.seek(seekVal);
				currCount = indexBin.readInt();
				counts.add(currCount);
				if (currCount < lowCount) {
					lowCount = currCount;
					lowBucket = i;
				}
				if (currCount > highCount) {
					highCount = currCount;
					highBucket = i;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//System.out.println("Bucket" + i + " current occupancy is " + currCount);
		}
		//Computing the mean sum value for all buckets.
		int sum = 0;
		for (int i = 0; i < counts.size(); i++) {
			sum = sum + counts.get(i);
		}
		double avg = sum / counts.size();

		//Display all requested info for part 2a
		System.out.println("There are " + blockMax + " buckets in the index.");
		System.out.println("The Lowest occupany bucket is " + lowBucket + " with just " + lowCount + " records.");
		System.out.println("The Highest occupany bucket is " + highBucket + " with " + highCount + " records.");
		System.out.println("The mean occupied capacity is " + avg + "\n\n");

		//Returns to the current position. This let me use this method anywhere for testing.
		try {
			indexBin.seek(currentPos);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	 /*---------------------------------------------------------------------
    |  Method writeToIDX(EID, indexCount)
    |
    |  Purpose:  This method acts as the requested stats method by Dr. McCann. The purpose of the method is to derive
    |            and display the Minimum bucket count(occupancy), Maximum bucket count(occupancy), Mean occupancy of all buckets,
    |            and the total count of buckets. This method will work through each of the stored capcity ints every 160 bytes one
    |            exists eg 160, 324, ... So that minimal access can determine these values.
    |           
    |
    |  Pre-condition: The .bin file can be in any state, however the stats will not be representative of the actual counts
    |                 unless it is run after complete write/expansion of the .idx file.
    |
    |  Post-condition: An unmodified .bin and the creation of a file lhl.idx which will have a full index of the .bin
    |                  file.
    |  
    |
    |  Parameters: EID - the passed int EID value to be used to compute a hash value.
    |              indexCount- The sequential order of Records from the .bin file is represented here
    |                          this value is used to ensure O(1) access to the bin file after the associated
    |                          EID is read from the .idx file.
    |
    |  Returns: None
    *-------------------------------------------------------------------*/
	private void writeToIDX(int EID, int indexCount) {

		int hash = getHash(EID);
		// System.out.println("Current bucketCount for bin " + hash + " is " +
		// getBinBucketCount(hash));

		if (getBinBucketCount(hash) < 20) {
			currBlock.writeEntry(indexBin, hash, current.getEid(), indexCount);

		} else {
			doubleBuckets();
			currBlock.writeEntry(indexBin, hash, current.getEid(), indexCount);

		}
	}

	 /*---------------------------------------------------------------------
    |  Method getHash(EID)
    |
    |  Purpose:  This method simply returns a hash value based on the formula provided by Dr. McCann
    |            this hash method will allow us to access the correct bucket for the passed EID.
    |           
    |
    |  Pre-condition: The .bin file must be opened as a RAF.
    |
    |  Post-condition: None
    |  
    |
    |  Parameters: EID - the passed int EID value to be used to compute a hash value..
    |
    |  Returns: an int representing the bucket of interest.
    *-------------------------------------------------------------------*/
	private int getHash(int EID) {
		int hash = (int) (EID % (Math.pow(2, (hVal + 1))));
		return hash;
	}

	 /*---------------------------------------------------------------------
    |  Method getHash(EID, oldH)
    |
    |  Purpose:  This method simply returns a hash value based on the formula provided by Dr. McCann
    |            this hash method will allow us to access the correct bucket for the passed EID. This method
    |            is overloaded s.t. it can also accept a different hVal then the one currently in use.
    |           
    |
    |  Pre-condition: The .bin file must be opened as a RAF.
    |
    |  Post-condition: None
    |  
    |
    |  Parameters: EID - the passed int EID value to be used to compute a hash value..
    |
    |  Returns: an int representing the bucket of interest.
    *-------------------------------------------------------------------*/
	private int getHash(int EID, int oldH) {
		int hash = (int) (EID % (Math.pow(2, (oldH + 1))));
		return hash;
	}

	 /*---------------------------------------------------------------------
    |  Method getBinBucketCount(hash)
    |
    |  Purpose:  This method is used to retrieve a current bucketCount for the bin associated with the hash value.
    |            This method is used more basically to ensure that the bucket we are trying to write to is not 
    |            already full. 
    |           
    |
    |  Pre-condition: The .bin file can be in any state, however the stats will not be representative of the actual counts
    |                 unless it is run after complete write/expansion of the .idx file.
    |
    |  Post-condition: An unmodified .bin and the creation of a file lhl.idx which will have a full index of the .bin
    |                  file.
    |  
    |
    |  Parameters: hash- The bucket of interest.
    |
    |  Returns: The current number of stored Records in the .idx file for the associated bucket.
    *-------------------------------------------------------------------*/
	private int getBinBucketCount(int hash) {
		int currentCount = 0;
		long currPos = 0;
		try {
			currPos = indexBin.getFilePointer();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			int seekVal = (hash * 160) + (160 + ((hash) * 4));
			indexBin.seek(seekVal);
			currentCount = indexBin.readInt();
			indexBin.seek(currPos);
		} catch (Exception e) {
			e.toString();
		}
		return currentCount;

	}
	 

	 /*---------------------------------------------------------------------
    |  Method writeToIDX(EID, indexCount)
    |
    |  Purpose:  This method acts as the requested stats method by Dr. McCann. The purpose of the method is to derive
    |            and display the Minimum bucket count(occupancy), Maximum bucket count(occupancy), Mean occupancy of all buckets,
    |            and the total count of buckets. This method will work through each of the stored capcity ints every 160 bytes one
    |            exists eg 160, 324, ... So that minimal access can determine these values.
    |           
    |
    |  Pre-condition: The .bin file can be in any state, however the stats will not be representative of the actual counts
    |                 unless it is run after complete write/expansion of the .idx file.
    |
    |  Post-condition: An unmodified .bin and the creation of a file lhl.idx which will have a full index of the .bin
    |                  file.
    |  
    |
    |  Parameters: EID - the passed int EID value to be used to compute a hash value.
    |              indexCount- The sequential order of Records from the .bin file is represented here
    |                          this value is used to ensure O(1) access to the bin file after the associated
    |                          EID is read from the .idx file.
    |
    |  Returns: None
    *-------------------------------------------------------------------*/
	public void writeBlank(RandomAccessFile stream) {

		// Sp
		if (hVal == 0) {
			for (int i = 0; i < 82; i++) {
				try {
					stream.writeInt(0);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {

			int blockMax = (int) ((Math.pow(2, hVal + 1)));
			int blockPosition = (blockMax / 2);
			try {
				stream.seek(blockPosition * BLOCKSIZE);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			while (blockPosition != blockMax) {

				for (int i = 0; i < 41; i++) {
					try {
						stream.writeInt(0);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				blockPosition++;
			}
		}
	}

	/*---------------------------------------------------------------------
    |  Method doubleBuckets()
    |
    |  Purpose:  This method is called if a full bucket is found. The overall size of the .bin is increased(2x),
    |            the H value increases by 1 and all current records are rehashed to the new structure. The doubling
    |            is mathematically guaranteed to place the new value in one of two buckets(Determined by h values.)
    |            which allows us to only access any two EntryBlocks(2x20 record chunks at any one time.). 
    |            When doubling is completed for the current block a write is issues to rewrite the block to the file.
    |            
    |			The hashing processing is as follows: Begin with 2 20 bucket bins for Record EID
    |           storage. Using the formula to derive keys k = EID mod(2^(H+1), with an H of zero
    |           which increases every time a full bucket needs to be added to. (E.G. after 21 even records
    |           there will be 4 bins with 80 buckets total.)
    |           
    |
    |  Pre-condition: The .bin file must be constructed with 9 fields matching the .csv strucuture
    |                 from 2021-utility-scale-solar-plants.
    |
    |  Post-condition: An unmodified .bin and the creation of a file lhl.idx which will have a full index of the .bin
    |                  file.
    |  
    |
    |  Parameters: None
    |
    |  Returns: None
    *-------------------------------------------------------------------*/
	private void doubleBuckets() {
		hVal++;
		writeBlank(indexBin);
		updatePositions(indexBin);
	}
	


	/*---------------------------------------------------------------------
    |  Method updatePositions(stream)
    |
    |  Purpose:  This method is called to physically increase the storage on the HD by doubling the
    |            capacity of the index. The hashing function used ensures that one of two buckets will
    |            determine the position of an entry into a bucket. All current entries are updated to
    |            new positions within the index bucket structure. These values are stored with an ordering
    |            based on the .bin file from project 1(that is as a Record Class). The reference in the index
    |            file will point to the position the bytes exist for that record in the .bin DB file. Maintaining
    |            this structure will ultimately allow us to search quickly using only the bucket position. THis 
    |            implementation is based on Dr. McCann's Linear Hashing lite algorithm. 
    |           
    |
    |  Pre-condition: The bin file must be freshly expanded(from writeBlank(). The entire expansion process is handled from
    |                 this method.
    |
    |  Post-condition: The .bin file for lhl.idx will be doubled in capacity and have all currently hashed values updated to
    |                  new positions.
    |  
    |
    |  Parameters: stream- The RAF file stream containing the lhl.idx RAF.
    |
    |  Returns: None
    *-------------------------------------------------------------------*/
	private void updatePositions(RandomAccessFile stream) {

		EntryBlock existing = new EntryBlock(stream);
		EntryBlock adding = new EntryBlock(stream);
		// This gives the total number of buckets
		int blockMax = (int) (Math.pow(2, hVal + 1));
		//This value acts as the end of the old
		int indexMax = blockMax - 1;
		int blockPosition = (indexMax / 2);

		//This section reads through each of the old half of the .idx file
		//The values contained within are rehashed to either the current location
		//or a new location.
		while (blockPosition >= 0) {
			existing = existing.readBlock(stream, blockPosition);
			adding = adding.readBlock(stream, indexMax);
			for (int i = 0; i < existing.getRecCount(); i++) {
				Entry entry = existing.getList().get(i);
				int hash = getHash(entry.getEID());
				if (hash != blockPosition) {
					existing.getList().remove(entry);
					existing.setRecCount(existing.getRecCount() - 1);
					adding.getList().add(entry);
					adding.setRecCount(adding.getRecCount() + 1);
				}
			}
			existing.writeBlock(stream, blockPosition);
			adding.writeBlock(stream, indexMax);
			blockPosition--;
			indexMax--;
		}
	}

	/*---------------------------------------------------------------------
    |  Method checkEID()
    |
    |  Purpose:  This method combines all current functionality for the .bin DB file and .idx file
    |            to allow searching via EID. This is a hash search which will find the correct bucket
    |            (determined by the hash value). Note to grader: I broke the r
    |           
    |
    |  Pre-condition: The bin file must be freshly expanded(from writeBlank(). The entire expansion process is handled from
    |                 this method.
    |
    |  Post-condition: The .bin file for lhl.idx will be doubled in capacity and have all currently hashed values updated to
    |                  new positions.
    |  
    |
    |  Parameters: stream The RAF file stream containing the lhl.idx RAF.
    |
    |  Returns: None
    *-------------------------------------------------------------------*/
	public void checkEID(int EID) {

		int hash = getHash(EID, hVal);
		EntryBlock toCheck = new EntryBlock(indexBin);
		toCheck.readBlock(indexBin, hash);
		for (Entry each : toCheck.getList()) {
			if (each.getEID() == EID) {
				System.out.println("The EID was found!\n");
				//System.out.println("THe EID is located in bucket " + hash);
				Record current = new Record(0);
				try {
					fileBin.seek(each.getKey() * recordLen);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("Tried to seek and failed.\n");
				}
				current.readEntry(fileBin, maxName, maxState, maxCOD);
				System.out.println(current.toString());
				return;
			}
		}

		System.out.println("No record associated with the EID= " + EID + " was found.\n");
		}

}
