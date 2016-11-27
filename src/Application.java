import java.util.Scanner;

public class Application {
	public static void main(String[] args) {
		Disk disk = new Disk();
		DiskBuilder diskBuilt = new DiskBuilder(disk);
		diskBuilt.builtDataSector();
		diskBuilt.builIndexSector();// to delete probably
		char buffer[] = new char[disk.getSectorSize()];//temporary buffer
		//fill all temp variables with non-hardcoded info from getters
		int recordSize = diskBuilt.getRecordSize();
		int keySize = diskBuilt.getKeySize();
		int firstAllocated = diskBuilt.getFirstAllocated();
		int indexStart = diskBuilt.getIndexStart();
		int indexSectors = diskBuilt.getIndexSectors();
		int indexRoot = diskBuilt.getRoot();
		int indexLevels = diskBuilt.getIndexLevels();
		IndexedFile indexedFile = new IndexedFile(disk, recordSize, keySize,
				firstAllocated, indexStart, indexSectors, indexRoot,
				indexLevels);
		// MAIN MENU
		Scanner keyboard = new Scanner(System.in);
		keyboard.useDelimiter("\\n");
		int option = 0;
		do {
			System.out.println("*****************************");
			System.out.println("*****************************");
			System.out.println("**  Insert new record...1  **");
			System.out.println("**  Find record.........2  **");
			System.out.println("**  Quit................3  **");
			System.out.println("*****************************");
			System.out.println("******************************");
			option = keyboard.nextInt();
			switch (option) {
			case 1:
				char recordToInsert[] = new char[60];// recordSize
				System.out.println("name: ");
				String name = keyboard.next();
				if (name.length() < keySize) {
					name.getChars(0, name.length(), recordToInsert, 0);
				} else
					name.getChars(0, keySize, recordToInsert, 0);
				System.out.println("location: ");
				String location = keyboard.next();
				if (location.length() < keySize) {
					location.getChars(0, location.length(), recordToInsert,
							keySize);
				} else
					location.getChars(0, keySize, recordToInsert, keySize);
				System.out.println("elevation: ");
				String elevation = keyboard.next();
				if (elevation.length() < 6) {
					elevation.getChars(0, elevation.length(), recordToInsert,
							keySize + keySize);
				} else
					elevation.getChars(0, 6, recordToInsert, keySize + keySize);
				if (indexedFile.insertRecord(recordToInsert)) {
					System.out.println("susccesfull :)");
				} else
					System.out.println("unsusceffsull :(");

				break;
			case 2:
				keyboard.useDelimiter("\\n");
				System.out.println("please enter the key: ");
				String toSearch = keyboard.next();
				char[] keyToSearch = new char[keySize];
				if (toSearch.length() < keySize) {//String is smaller than keySize
					toSearch.getChars(0, toSearch.length(), keyToSearch, 0);
				} else//String is bigger than keysize
					toSearch.getChars(0, keySize, keyToSearch, 0);				
				if(indexedFile.findRecord(keyToSearch)){
					System.out.println("key was found sucesfully");
				}else
					System.out.println("sorry key was not found");
				break;
			case 3:
				System.out.println("good bye...");
				break;
			case 4:// for testing porpuses must delete
				System.out.println("enter sector area");
				int newSectorToGet = keyboard.nextInt();

				resetBuffer(buffer);
				disk.readSector(newSectorToGet, buffer);
				print(buffer);

			default:
				System.out.println("you have enter wrong options please try again");
			}
		} while (option != 3);
	}

	public static void print(char[] buffer) {
		for (int i = 0; i < buffer.length; i++) {
			System.out.print(buffer[i]);
		}
		System.out.println();
	}

	// clears the buffer to all '\000'
	public static void resetBuffer(char[] buffer) {
		for (int i = 0; i < buffer.length; i++) {
			buffer[i] = '\000';
		}
	}

	public static void print(char[] buffer, int buffIndex) {
		for (int i = 0; i < buffer.length; i++) {
			System.out.print(buffer[i + buffIndex]);
		}
		System.out.println();
	}
}
