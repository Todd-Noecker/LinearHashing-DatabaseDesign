import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Comparator;
import java.util.Formatter;

public class Record implements Comparable<Object>{

	/*+----------------------------------------------------------------------
	 ||
	 ||  Class Record
	 ||
	 ||         Author:  Todd Noecker (Noted Methods were adapted from Dr. McCann's Example)
	 ||
	 ||        Purpose:  An object of this class holds the field values of one
	 ||                  record of data.  There are nine fields EID, Project Name,
	 ||					 State, Latitude, Longitude, avg GHI, Solar Capacity in MW-DC
	 ||					 and Solar Capacity in MW-AC. This class also(Not done yet) 
	 ||                  supports the binary write/read operations to convert the
	 ||                  Object from a .csv to .bin file and reads from a .bin file.
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
	 ||  Class Methods:  int    getEid()
	 ||                  String getName()
	 ||                  String getCod()
	 ||                  String getState()
	 ||                  double getGhi()
	 ||                  double getLat()
	 ||                  double getLon()
	 ||                  void   setLat()
	 ||                  void   setLon()
	 ||                  void   writeToBin()
	 ||                  void   readFromBin()
	 ||		             void   toString()
	 ||                  void   setState()
	 ||                  void   setGhi()
	 ||                  void   setName()
	 ||                  void   setCod()
	 ||                  void   setEid()
	 ||
	 ++-----------------------------------------------------------------------*/

	//The ordering here represents the ordering stored in the .bin file.
	private int eid; //The EID code for a specified plant. If a record has an invalid EID, it is rejected.
	private String name; //The name of the given solar plant
	private String cod; //The Commercial Operating Date(When they started generating power)
	private String state; //The state the plant is in.
	private double lat; //The Latitude
	private double lon; //The Longitude
	private double ghi; //Avg. Global Horizontal Irradiance(How much sun it gets)
	private double capAc; //The capacity measured in GW-AC
	private double capDc;// The capcacity measured in GW-DC

	public Record(int eid) {
		this.eid = eid;
		this.name = "";
		this.state = "ZZ"; //Chose nonvalid State FIPS code to indicate a missing field
		this.cod = "00/00/0000"; //Chose nonvalid COD to indicate a missing field
		this.lat = 0.0;
		this.lon = 0.0;
		this.ghi = 0.0;
		this.capAc = 0.0;
		this.capDc = 0.0;
	}

	// Setter methods for Class variables.
	// Note EID required to instantiate Class instance

	public void setEid(int eid) {
		this.eid = eid;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setCod(String cod) {
		this.cod = cod;
	}

	public void setState(String state) {
		this.state = state;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public void setGhi(double ghi) {
		this.ghi = ghi;
	}

	public void setCapAc(double capAc) {
		this.capAc = capAc;
	}

	public void setCapDc(double capDc) {
		this.capDc = capDc;
	}

	// Getter method for Class variables.

	public double getLat() {
		return lat;
	}

	public double getLon() {
		return lon;
	}

	public double getGhi() {
		return ghi;
	}

	public double getCapAc() {
		return capAc;
	}

	public double getCapDc() {
		return capDc;
	}

	public int getEid() {
		return eid;
	}

	public String getName() {
		return name;
	}

	public String getCod() {
		return cod;
	}

	public String getState() {
		return state;
	}

	
    /*---------------------------------------------------------------------
    |  Method writeToBin (stream, maxName, maxState, maxCOD)
    |
    |  Purpose:  Create and populate a binary file named 'fileName.bin'
    |            that contains the data from the supplied csvContent
    |            list in the same order.  The integers are stored
    |            as 4-byte ints.  The strings are stored as 22-byte
    |            character sequences, padded on the right with null
    |            characters as necessary to reach the desired length.
    |
    |  Pre-condition:  Stream must be on initial byte 0 for first write and
    |                  must write a consistent length for each Object entry.
    |
    |  Post-condition: The file is created in the current directory after the
    |                  last record is written. The final line of the file
    |                  contains the max field length for the 3 String fields.
    |                  The file has the same content as the csvContent list.
    |  
    |           Note:  Adapted from Dr. McCann's code.    
    |
    |  Parameters:
    |      fileName -- file name only of the binary file, no extension 
    |      csvContent -- An ArrayList of DataRecord objects, containing
    |                    the data from the given CSV file.
    |
    |  Returns: None.
    *-------------------------------------------------------------------*/
	public void writeToBin(RandomAccessFile stream, int maxName, int maxState, int maxCOD, boolean last) {

		if(last == true) {
			try {
				stream.writeInt(maxName);
				stream.writeInt(maxCOD);
				stream.writeInt(maxState);
			} catch (IOException e) {
				System.out.println("Error writing final int values. The bin will be bugged.\n");
				e.printStackTrace();
			}

			return;
		}
		
		//Pad Strings with spaces until they meet max field length.
		while (name.length() < maxName) {
			name = name + " ";
		}
		while (state.length() < maxState) {
			state = state + " ";
		}
		while (cod.length() < maxCOD) {
			cod = cod + " ";
		}
		
		StringBuffer projectName = new StringBuffer(name); 
		StringBuffer solarCod = new StringBuffer(cod);
		StringBuffer stateAbv = new StringBuffer(state);

		//Attempt to write Object to the binary file.
		try {
			stream.writeInt(eid);
			stream.writeBytes(projectName.toString());
			stream.writeBytes(solarCod.toString());
			stream.writeBytes(stateAbv.toString());
			stream.writeDouble(lat);
			stream.writeDouble(lon);
			stream.writeDouble(ghi);
			stream.writeDouble(capAc);
			stream.writeDouble(capDc);
		} catch (IOException e) {
			System.out.println("I/O ERROR: Couldn't write to the file;\n\t" + "perhaps the file system is full?");
			System.exit(-1);
		}
		

	}
	
    /*---------------------------------------------------------------------
    |  Method readEntry(RandomAccessFile stream, int maxName, int maxState, int maxCOD)
    |
    |  Purpose:  This method will read a single entry from the .bin file into
    |            the fields of a Record Object. The read sections are determined
    |            by a maxlength value determined by the .csv read process.
    |
    |  Pre-condition: The stream is on it's initial read of the bin file. THe bin file is on
    |                 byte 0 when accessed by the method. No seeks are done in method, to ensure
    |                 it will not interfer with other methods sequential reads.
    |
    |  Post-condition: Bin file access is left open. The bin will be one record further/deeper,
    |                  so bounds handling must be handled elsewhere.
    |  
    |           Note:  Adapted from Dr. McCann's code.    
    |
    |  Parameters: RandomAccessFile stream, The RAF stream of the .bin to be read from.
    |              int maxName, The maximum field length for the name field across the entire .csv/.bin
    |              int maxState, The maximum field length for the state across the entire .csv/.bin
    |              int maxCOD, The maximum field length for the COD across the entire .csv/.bin
    |
    |  Returns: a String representing the currently read object from the .bin.
    *-------------------------------------------------------------------*/
	public String readEntry(RandomAccessFile stream, int maxName, int maxState, int maxCOD) {
		byte[] ctyName = new byte[maxName]; // ASCII, not UNICODE
		byte[] CODName = new byte[maxState];
		byte[] stateName = new byte[maxCOD];

		try {
			this.eid = stream.readInt();
			stream.readFully(ctyName); // reads all the bytes we need...
			this.name = new String(ctyName); // ...& makes a String of them
			stream.readFully(CODName);
			this.cod = new String(CODName);
			stream.readFully(stateName);
			this.state = new String(stateName);
			this.lat = stream.readDouble();
			this.lon = stream.readDouble();
			this.ghi = stream.readDouble();
			this.capAc = stream.readDouble();
			this.capDc = stream.readDouble();
		} catch (IOException e) {
			System.out.println(
					"I/O ERROR: Couldn't read from the file;\n\t" + "The RAF stream might be out of position.");
			System.exit(-1);
		}
		return (this.toString());
	}
	

	/*---------------------------------------------------------------------
    |  Method toString()
    |
    |  Purpose:  A standard toString with the records printing according to Dr. McCann's
    |            specified print look. (Surround each EID Name and GHI by brackets)
    |            Three Values[EID][NAME with trailing spaces to max len][Capacity in MW-AC]
    |
    |  Pre-condition:  None
    |
    |  Post-condition: None
    |
    |  Parameters:     None
	|							
    |  Returns: a partial String representation of the Record Object.
    *-------------------------------------------------------------------*/
	public String toString() {
		String printMe = "";
		Formatter formatter = new Formatter();
		printMe = printMe + "[" + eid + "] [" + name + "] [" + formatter.format("%.2f", capAc) + "]\n";
		return printMe;
	}
	
	/*---------------------------------------------------------------------
    |  Method myString()
    |
    |  Purpose:  A standard toString with the full value print of the Record's contents.
    |            Primary use for internal testing. The full print ensures all writes/reads
    |            are correct.
    |
    |  Pre-condition:  None
    |
    |  Post-condition: None
    |
    |  Parameters:     None
	|							
    |  Returns: a full String representation of the Record Object.
    *-------------------------------------------------------------------*/
	public String myString() {
		String printMe = "";
		printMe = printMe + "[EID] " + eid + " [PROJECT NAME] " + name + " " + " [SOLAR COD] " + cod + " [STATE] "
				+ state + " [LAT] " + lat + " [LONG] " + lon + " [AVG GHI] " + ghi + " [SOLAR CAPACITY MW-DC] " + capDc
				+ " [SOLAR CAPACITY MW-AC] " + capAc + "\n";
		return printMe;
	}

	/*---------------------------------------------------------------------
    |  Method compareTo(Object o)
    |
    |  Purpose:  This is a basic and comparator implementation to utilize Collections.sort()
    |            This will create a list in ascending order using the int Eid value of the Record
    |            Object.
    |            
    |
    |  Pre-condition:  None
    |
    |  Post-condition: None
    |
    |  Parameters:     Object o- The passed object to be read as a Record and compared to via
    |                            EID value.
	|							
    |  Returns: an int which will result in the list being sorted in ascending order.
    *-------------------------------------------------------------------*/
	@Override
	public int compareTo(Object o) {
		Record x = (Record) o;
		if (this.getEid() < x.getEid()) {
			return -1;
		}
		return 1;
	}
}
