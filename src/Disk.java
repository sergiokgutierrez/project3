public class Disk
{
   private int sectorCount;   // sectors on the disk
   private int sectorSize;    // characters in a sector
   private char[][] store;    // all disk data is stored here
   public Disk()    // for default sectorCount and sectorSize
   {
	   sectorCount = 10000;
	   sectorSize = 512;
	   store = new char[sectorCount][sectorSize];
   }
   public Disk(int sectorCount, int sectorSize)
   { 
	   this.sectorCount = sectorCount;
	   this.sectorSize = sectorSize;
	   store = new char[sectorCount][sectorSize];   
   }
   public void readSector(int sectorNumber, char[] buffer)   // sector to buffer
   {
	   for (int x=0; x<sectorSize; x++){
		   buffer[x] = store[sectorNumber][x];
	   }
   }                                                       
   public void writeSector(int sectorNumber, char[] buffer)  // buffer to sector
   {
	   for (int x=0; x<sectorSize;x++){
		   store[sectorNumber][x]=buffer[x];
	   }
   } 
   public int getSectorCount()
   {
      return sectorCount;
   }
   public int getSectorSize()
   {
      return sectorSize;
   }
}