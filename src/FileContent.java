import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.regex.Pattern;

public class FileContent
{
	static private int totalReads = 0;
	static private int totalWrites = 0;
	static private int totalDeletes = 0;
	
	private ArrayList<Integer> keys;
	private ArrayList<String> childrenPaths;
	private String filePath;
	private boolean saved = false;
	
	
	FileContent(ArrayList<Integer> keys, ArrayList<String> childrenPaths, String filePath)
	{
		this.keys = keys;
		this.childrenPaths = childrenPaths;
		this.filePath = filePath;
	}
	
	FileContent(String filePath) throws IOException
	{
		this.filePath = filePath;
		this.keys = new ArrayList<>();
		this.childrenPaths = new ArrayList<>();
		read(filePath);
		saved = true;
	}
	
	void write(String filepath) throws IOException
	{
		if(saved)
			return;
		
		FileWriter fileWriter = new FileWriter(filepath, false);
		if(keys != null && childrenPaths != null)
		{
			for(int i = 0; i < keys.size(); i++)
			{
				fileWriter.write(keys.get(i) + "\n");
			}
			
			for(int i = 0; i < childrenPaths.size(); i++)
			{
				fileWriter.write(childrenPaths.get(i) + "\n");
			}
		}
		totalWrites++;
		fileWriter.flush();
		fileWriter.close();
		saved = true;
	}
	
	void write() throws IOException
	{
		write(filePath);
	}
	
	void read(String filepath) throws IOException
	{
		Scanner scanner = new Scanner(new File(filepath));
		while(scanner.hasNext(Pattern.compile("-?[0-9]+")))
		{
			keys.add(Integer.parseInt(scanner.nextLine()));
		}
		while(scanner.hasNext("((?:[^/]*/)*)(.*)"))
		{
			childrenPaths.add(scanner.nextLine());
		}
		scanner.close();
		totalReads++;
	}
	
	private void delete(String filePath)
	{
		File file = new File(filePath);
		if(file.delete())
		{
			System.out.println("File deleted successfully");
			totalDeletes++;
		}
		else
			System.out.println("File not deleted");
		saved = false;
	}
	
	void delete()
	{
		delete(filePath);
	}
	
	ArrayList<Integer> getKeys()
	{
		return keys;
	}
	
	ArrayList<String> getChildrenPaths()
	{
		return childrenPaths;
	}
	
	static int getTotalReads()
	{
		return totalReads;
	}
	
	static int getTotalWrites()
	{
		return totalWrites;
	}
	
	void setFilePath(String filePath)
	{
		this.filePath = filePath;
		saved = false;
	}
	
	String getFilePath()
	{
		return filePath;
	}
	
	void insertKey(int key)
	{
		keys.add(key);
		Collections.sort(keys);
		saved = false;
	}
	
	boolean removeKey(int key)
	{
		if(keys.remove(new Integer(key)))
		{
			saved = false;
			return true;
		}
		else
			return false;
	}
	
	int removeLowestKey()
	{
		saved = false;
		return keys.remove(0);
	}
	
	int removeHighestKey()
	{
		saved = false;
		return keys.remove(keys.size() - 1);
	}
	
	int removeIndexKey(int index)
	{
		saved = false;
		return keys.remove(index);
	}
	
	
	public static void main(String[] args) throws IOException
	{
		FileContent fileContent = new FileContent(new ArrayList<Integer>(), new ArrayList<String>(), "a");
		
		fileContent.insertKey(0);
		fileContent.write();
		fileContent.insertKey(1);
		System.out.println(fileContent.keys.size());
		fileContent.write();
		
		
	}
	
}
