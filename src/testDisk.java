
public class testDisk {
	public static void main(String []args)
	{
		Disk disk = new Disk (15,15);//sector count by sector size
		System.out.println(disk.getSectorCount());
		System.out.println(disk.getSectorSize());
		char[] carr1={'h','e','l','l','o'};
		char[] carr2={'m','y',' ','n','a'};
		char[] carr3={'i','s',' ','e','l'};
		disk.writeSector(0, carr1);
		disk.writeSector(1, carr2);
		disk.writeSector(2, carr3);
		disk.readSector(1, carr1);
		 for (int x=0; x<carr1.length; x++){
			   System.out.print(carr1[x]);
		   }
		
	}

}
