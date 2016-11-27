public class IndexedFile
{
   private Disk disk;             // disk on which the file will be written
   private char[] buffer;         // disk buffer
//   private int recordSize;        // in characters
   private int keySize;           // in characters
   // fields describing data portion of file
   private int recordsPerSector;  // sectorSize/recordSize
   private int firstAllocated;    // sector number where data begins
   private int sectorsAllocated;  // sectors originally allocated for data
   private int overflowStart;     // sector number where overflow begins
   private int overflowSectors;   // count of overflow sectors in use
   // fields describing index portion of file
   private int indexStart;        // sector number where index begins
   private int indexSectors;      // number of sectors allocated for index
   private int indexRoot;         // sector number of root of index
   private int indexLevels;       // number of levels of index
   private int recordSize;
   private int sectorSize;
   private int elevationSize;//size of the elevation name in chars(NOT THE ACTUALL ELEVATION VALUE)
   
   public IndexedFile(Disk disk, int recordSize, int keySize,
                      int firstAllocated, int indexStart,
                      int indexSectors, int indexRoot, int indexLevels)
   {
	  
	   this.disk = disk;
	   sectorSize = disk.getSectorSize();
	   this.recordSize = recordSize;
	   this.keySize = keySize;
       this.firstAllocated =firstAllocated;
       this.indexStart = indexStart;
       this.indexSectors = indexSectors;
       this.indexRoot = indexRoot;
       this.indexLevels = indexLevels; 
       recordsPerSector = sectorSize/recordSize;
       buffer = new char[sectorSize];
       overflowStart = indexRoot +1;//overflowsector is right after the root
       overflowSectors = 0;//starts at sector and grows at the more insertion
       elevationSize = recordSize-keySize-keySize;//elevation size is left over by key and name
   }
   public boolean insertRecord(char[] record)
   {
	   int buffIndex = 0 ;
	   int difference = 1;//set as 1 to enter the loop
	   char keyToSearch []= new char[keySize];//creates char arr so it can save the the array temp
	   getKey(keyToSearch,record,0);//insert key from the record into keyToSearch
	   int sectorNumber = getSector(keyToSearch);//get sector returns the sector number where the letter suppose to go at	   
	   resetBuffer(buffer);
	   disk.readSector(sectorNumber, buffer);
	   char dataSectorKey []= new char[keySize];//creates char arr of 27
	   while(difference != 0 && buffIndex< (disk.getSectorSize() - recordSize) && buffer[buffIndex] != '\000'){
		   getKey(dataSectorKey,buffer, buffIndex);//gets entire sector from the data sector and puts it  to the buffer
		   difference = keyComparator(dataSectorKey, keyToSearch);
		   buffIndex+=recordSize;
	   }
	   if(difference == 0){
		   System.out.println("insertion is already in the disk, no duplicates allow");
		   return false;
		   }
	   buffIndex = 0;
	   //look for space
	   while(buffer[buffIndex] != '\000' && buffIndex < (sectorSize-recordSize)){
		   buffIndex+=recordSize;
	   }
	   if(buffer[buffIndex]=='\000' && buffIndex < (sectorSize-recordSize)){//iF we find free space we insert here
		   for(int i = 0; i<record.length;i++){
			   buffer[i+buffIndex]=record[i];
		   }
		   disk.writeSector(sectorNumber, buffer);//buffer repleace the entire sector
		   disk.readSector(sectorNumber, buffer);//for testing purpuse delete
		   return true;	   
		 //WE GO TO THE OVERFLOW SECTOR IN THE DISK
	   } else if(overflowStart + overflowSectors > disk.getSectorCount()) {//if there is no space in the entire disk
		   System.out.println("sorry the overflow has reach it's capacity of "+ disk.getSectorCount());
		   System.out.println("the disk is full");
		   return false;
	   } else{//go to the first spot open in the overflow sector
		   disk.readSector(overflowSectors + overflowStart, buffer);
		   buffIndex = 0;
		   while(buffer[buffIndex]!='\000' && buffIndex <sectorSize-recordSize){
			   buffIndex+=recordSize;
		   }
		   if(buffer[buffIndex]=='\000' && buffIndex <sectorSize-recordSize){//If we find free space in this overflow spot we insert here 
			  for(int i = 0; i<record.length;i++){
				   buffer[i+buffIndex]=record[i];
			   }
			   disk.writeSector(overflowSectors + overflowStart, buffer);//buffer repleace the entire sector
			   return true;	   
			 //we go to the overflow
		   }else if(overflowStart + overflowSectors < disk.getSectorCount()){//if section is not full insert a new record in a brand new sector
			   overflowSectors++;
			   resetBuffer(buffer);
			   for(int i = 0; i<record.length;i++){
				   buffer[i]=record[i];
			   }
			   disk.writeSector(overflowSectors + overflowStart, buffer);//buffer repleace the entire sector
			   return true;		   
		   }
	   }
	   return false;
   }   
   public boolean findRecord(char[] record)
   {
	   int buffIndex =0 ;
	   int difference = 1;//set as 1 to enter the loop
	   char keyToSearch []= new char[keySize];//creates char arr of 27
	   getKey(keyToSearch,record,0);//insert key from the record into keyToSearch
	   int sectorNumber = getSector(keyToSearch);//get sector returns the sector number where the letter suppost to be at
	   resetBuffer(buffer);
	   disk.readSector(sectorNumber, buffer);

	   char dataSectorKey []= new char[keySize];//creates char arr of 27
	   while(difference != 0 && buffIndex< (disk.getSectorSize() - recordSize) && buffer[buffIndex] != '\000'){
		   getKey(dataSectorKey,buffer, buffIndex);//gets entire sector from the data sector and puts it  to the buffer
		   difference = keyComparator(dataSectorKey, keyToSearch);
		   buffIndex+=recordSize;
	   }
	   if(difference == 0){
		   buffIndex -= recordSize;
		   printRecord(buffer, buffIndex);
		   resetBuffer(record);
		   for(int x =0; x<record.length;x++){
			   record[x] = buffer[buffIndex+x];
		   }
		   return true;
	   }else{
		   for(int o = overflowStart; o<= overflowStart + overflowSectors; o++){
			   resetBuffer(buffer);
			   disk.readSector(o, buffer);
			   buffIndex = 0;
			   while(difference != 0 && buffIndex< (disk.getSectorSize() - recordSize) && buffer[buffIndex] != '\000'){
				   getKey(dataSectorKey,buffer, buffIndex);//gets entire sector from the data sector and puts it  to the buffer
				   difference = keyComparator(dataSectorKey, keyToSearch);
				   buffIndex+=recordSize;
			   }
			   if(difference == 0){
				   buffIndex -= recordSize;
				   printRecord(buffer, buffIndex);
				   resetBuffer(record);
				   for(int x =0; x<record.length;x++){
					   record[x] = buffer[buffIndex+x];
				   }
				   return true;
			   }
		   }
	   }
	   System.out.println("sorry the data was not found");
	   return false;
   } 
   
   
   
   
   private int getSector(char[] key)   // returns sector number indicated by key
   {

	   int indexNumber = 0, prevIndexNumber;
	   int difference;
	   int buffIndex = 0;
	   char indexKey []= new char[keySize];//creates char arr of 27
	   int level = indexRoot;//start at index root
	   //loop around indexlevels
	   for(int s = 0; s<indexLevels; s++){
		   disk.readSector(level,buffer );//reads the root
		   //traversing trough the buffer
		   do{
			   getKey(indexKey,buffer,buffIndex);//insert key from Buffer into indexKey 
			    prevIndexNumber = indexNumber;
			   indexNumber = getIndexNumber(buffer, buffIndex);//getindexNumber
			   difference = keyComparator(indexKey, key);//checks difference of keys
			  buffIndex += (keySize+elevationSize);
		   }while(difference > 0 && buffIndex<(sectorSize-keySize-elevationSize) && buffer[buffIndex] != '\000');//as long as there is enough space to read one more keys and the sector (6) and next value is not null
			if(difference > 0 ){
				level = indexNumber;
			}if (difference <0){
				level = prevIndexNumber;
			}if (difference ==0){
				level = indexNumber;
//				System.out.println("is zero it will go to curent level: "+level);
			}
			buffIndex = 0;
			prevIndexNumber = 0;
			
	   }
	   
	   //end of loop
	   return level;
   }   
   // get key gets 27 char from buffer as long as they are no nulls
	void getKey(char[] key,char[] buffer, int buffindex){
		for(int x=0; x < keySize;x++){
//			if(buffer[x]!='\000'){
				key[x] = buffer[x + buffindex];
//			}
		}
	}
	int keyComparator(char[] indexKey, char [] keyToSearch){
		int val1, val2, value=0;
		int smallestSize = (getSizeofKey(indexKey) <= getSizeofKey(keyToSearch)) ? getSizeofKey(indexKey) : getSizeofKey(keyToSearch);
		for(int x=0; x < smallestSize;x++){
			val1 = charValue( indexKey [x]);
			val2 = charValue( keyToSearch [x]);
			value = val2 - val1;
			if(value != 0){
				return value;
			}
		}
		if(getSizeofKey(indexKey) == getSizeofKey(keyToSearch)){
			return 0;
		}else if(getSizeofKey(indexKey) < getSizeofKey(keyToSearch)){//if key key we are serching is bigger
			return 1;
		}else if(getSizeofKey(indexKey) > getSizeofKey(keyToSearch)){
			return -1;
		}
		return value;
		
	}
	
	int charValue(char ch){
		int val = Character.valueOf(ch);
		if(val >=97 && val<=122){
			val -= 32;
		}
		return val;
	}
	int getSizeofKey(char key[]){
		int size=0;
		for(int x=0;x<key.length;x++){
			if(key[x] != '\000'){
				size++;
			}
		}
		return size;
	}
   //this prints any char array
   public void print(char [] buffer){
		for(int i=0; i<buffer.length; i++){
			System.out.print(buffer[i]);
		}
		System.out.println();
	}
   public void printRecord(char [] record, int buffIndex){
	   System.out.print("Name: ");
		for(int i=buffIndex; i<buffIndex+keySize; i++){
			System.out.print(buffer[i]);
		}
		System.out.println();
		System.out.print("Location: ");
		for(int i=buffIndex+keySize; i<buffIndex+keySize+keySize; i++){
			System.out.print(buffer[i]);
		}
		System.out.println();
		System.out.print("Elevation: ");
		for(int i=buffIndex+keySize+keySize; i<buffIndex+keySize+keySize+elevationSize; i++){
			System.out.print(buffer[i]);
		}
		System.out.println();
	}
   //clears the buffer to all '\000' 
   public static void resetBuffer(char [] buffer){
		for(int i=0; i<buffer.length; i++){
			buffer[i]='\000';
		}
	}
   /****TAKES THE FIRST 4 ASCII VALUES TO BUILT AN INT***/
	public int getIndexNumber(char [] buff,int buffIndex){
		int sectionN = 0;
		int c;
		//thousands
		c = (buff[buffIndex+keySize]) - 48;
		sectionN += c*1000;
		//hundreds
		c = (buff[buffIndex+keySize+1]) - 48;
//		System.out.print(buff[buffIndex+28]);
		sectionN += c*100;
		//tens
		c = (buff[buffIndex+keySize+2]) - 48;
//		System.out.print(buff[buffIndex+29]);
		sectionN += c*10;
		//singles
		c = (buff[buffIndex+keySize+3]) - 48;
//		System.out.print(buff[buffIndex+30]);
		sectionN += c*1;
		return sectionN;
	}
}