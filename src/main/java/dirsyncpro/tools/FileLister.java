package dirsyncpro.tools;


import java.io.File;
import java.util.ArrayList;


public class FileLister {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if (args.length < 1){
			System.out.println("Please enter a directory as parameter!");
			System.exit(1);
		}
		File dir = new File(args[0]);
		if (!dir.exists()){
			System.out.println("Directory does not exist!");
			System.exit(1);
		}
		System.out.println("Please wait...");
		ArrayList<File> files = list(dir);
		for (int i = 0; i < files.size() ; i++){
			System.out.println(files.get(i).getAbsolutePath());
		}
		System.out.println("Found " + files.size() + " files.");
	}

	static ArrayList<File> list(File dir){
		ArrayList<File> filesAL = new ArrayList<File>();
		File[] files = dir.listFiles();
		for (File f : files){
			filesAL.add(f);
			if (f.isDirectory()){
				filesAL.addAll(list(f));
			}
		}
		return filesAL;
	}
}
