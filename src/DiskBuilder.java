import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.Object;

public class DiskBuilder {
	private char buffer[];
	private Disk disk;
	private int buffIndex;
	private int recordsNumber;
	private int sectorCount;
	// variables from Indexed File
	private int recordSize; // in characters
	private int keySize; // in characters
	// fields describing data portion of file
	private int firstAllocated; // sector number where data begins
	private int sectorsAllocated; // sectors originally allocated for data
	// fields describing index portion of file
	private int indexStart; // sector number where index begins
	private int indexSectors; // number of sectors allocated for index
	private int indexRoot; // sector number of root of index
	private int indexLevels; // number of levels of index

	// must create two construtors
	public DiskBuilder() {
		buffer = new char[512];
		disk = new Disk(10000, 512);
		buffIndex = 0;
		recordsNumber = 0;
		firstAllocated = 1000;
		keySize = 27;
		recordSize = 60;
	}

	// add more stuff to this constructor
	public DiskBuilder(Disk disk) {
		buffer = new char[disk.getSectorSize()];
		this.disk = disk;
		buffIndex = 0;
		recordsNumber = 0;
		firstAllocated = 1000;
		keySize = 27;
		recordSize = 60;
	}

	public void builIndexSector() {
		char tempBuffer[] = new char[512];
		int levelStart, levelEnd;
		int loopCount = 0;
		levelStart = firstAllocated;// 1000
		levelEnd = sectorCount;// 3939
		int diskIndex = sectorCount;
		int buffIndex = 0;
		indexLevels = 0;
		while (levelStart + 1 != levelEnd) {
			for (int i = levelStart; i < levelEnd; i++) {
				resetBuffer(tempBuffer);
				disk.readSector(i, tempBuffer);
				// copy 15 and then save to drive
				for (int t = 0; t < keySize; t++) {
					buffer[buffIndex + t] = tempBuffer[t];
				}
				buffIndex += keySize;
				// saving index as char array size
				String word = Integer.toString(i);
				if (word.length() >= 6) {
					word.getChars(0, 27, buffer, buffIndex);
				} else if (word.length() < 6) {
					for (int l = 0; l < word.length(); l++) {
						buffer[buffIndex + l] = word.charAt(l);
					}// padding nulls\000
					for (int l = word.length(); l < 6; l++) {
						buffer[buffIndex + l] = '\000';
					}
				}

				buffIndex += 6;
				loopCount++;
				if (loopCount == 15) {
					// insertbuffer into memory
					disk.writeSector(diskIndex, buffer);
					resetBuffer(buffer);
					resetBuffer(tempBuffer);
					buffIndex = 0;
					loopCount = 0;
					diskIndex++;
				}
			}
			if (buffIndex != 0) {
				disk.writeSector(diskIndex, buffer);
				resetBuffer(buffer);
				buffIndex = 0;
				loopCount = 0;
				diskIndex++;
			}
			levelStart = levelEnd;
			levelEnd = diskIndex;
			indexRoot = levelStart;
			indexLevels++;
		}
	}

	public void builtDataSector() {
		sectorsAllocated = 0;
		sectorCount = firstAllocated;
		// The name of the file to open.
		String fileName = "mountaindata.txt";

		// This will reference one line at a time
		String line = null;
		// temprary holds space of name, location
		String name, location, elevation;
		try {
			// FileReader reads text files in the default encoding.
			FileReader fileReader = new FileReader(fileName);
			// Always wrap FileReader in BufferedReader.
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			char n = '\000';// make this \000 in the final
			String recordFields[];
			while ((line = bufferedReader.readLine()) != null) {
				recordFields = line.split("#");
				name = recordFields[0];
				location = recordFields[1];
				elevation = recordFields[2];
				// name
				if (name.length() >= 27) {
					name.getChars(0, 27, buffer, buffIndex);
				} else if (name.length() < 27) {
					name.getChars(0, name.length(), buffer, buffIndex);
					for (int x = name.length(); x < 27; x++) {
						buffer[buffIndex + x] = n;
					}
				}
				buffIndex += 27;
				// country
				if (location.length() >= 27) {
					location.getChars(0, 27, buffer, buffIndex);
				} else if (location.length() < 27) {
					location.getChars(0, location.length(), buffer, buffIndex);
					for (int x = location.length(); x < 27; x++) {
						buffer[buffIndex + x] = n;
					}
				}
				buffIndex += 27;
				// elevation
				if (elevation.length() >= 6) {
					elevation.getChars(0, 6, buffer, buffIndex);
				} else if (elevation.length() < 6) {
					elevation.getChars(0, elevation.length(), buffer, buffIndex);
					// padding in with \000
					for (int x = elevation.length(); x < 6; x++) {
						buffer[buffIndex + x] = n;
					}
				}
				buffIndex += 6;
				recordsNumber++;
				/* if has five insertion then insert into disk and empty buffer */
				if (recordsNumber % 5 == 0) {
					disk.writeSector(sectorCount, buffer);
					resetBuffer(buffer);
					sectorCount++;
					sectorsAllocated++;
					buffIndex = 0;// re-write the buffer
				}
			}
			// in case the bufffer was not empty
			if (recordsNumber % 5 != 0) {
				disk.writeSector(sectorCount, buffer);
				resetBuffer(buffer);

				sectorCount++;
				sectorsAllocated++;
				buffIndex = 0;// re-write the buffer
			}
			indexStart = sectorCount;

			// Always close files.
			bufferedReader.close();
		} catch (FileNotFoundException ex) {
			System.out.println("Unable to open file '" + fileName + "'");
		} catch (IOException ex) {
			System.out.println("Error reading file '" + fileName + "'");
		}

	}

	public void printBuffer(char[] buffer) {
		for (int i = 0; i < 512; i++) {
			System.out.print(buffer[i]);
		}
		System.out.println();
	}

	public void resetBuffer(char[] buffer) {
		for (int i = 0; i < 512; i++) {
			buffer[i] = '\000';
		}
	}

	public int getRoot() {
		return indexRoot;
	}

	public int getIndexLevels() {
		return indexLevels;
	}

	public int getSectorsAllocated() {
		return sectorsAllocated;
	}

	public int getIndexStart() {
		return indexStart;
	}

	public int getIndexSectors() {
		indexSectors = (indexStart - 1) - firstAllocated;
		return indexSectors;
	}
	public int getFirstAllocated(){
		return firstAllocated;
	}
	public int getKeySize(){
		return keySize;
	}
	public int getRecordSize(){
		return recordSize;
	}

}