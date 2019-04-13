import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Testing
{
	
	
	public static void main(String[] args) throws IOException
	{
		Scanner scanner = new Scanner(System.in);
		
		int rank = 100;
		boolean random = true;
		boolean randomSeed = false;
		long seed = 0;
		int elementCount = 1000;
		
		Random rng;
		
		if(randomSeed)
			rng = new Random();
		else
			rng = new Random(seed);
		
		BTree tree = new BTree(rank);
		BTree.printlogs = false;
		
		
		long start = System.nanoTime();
		for(int i=0; i<elementCount; i++)
		{
			if(random)
				tree.insert(rng.nextInt());
			else
				tree.insert(i);
			//scanner.next();
		}
		long stop = System.nanoTime();
		
		
		
		System.out.println("Time : " + (stop - start));
		System.out.println("Number of write operations: " + FileContent.getTotalWrites());
		System.out.println("Number of read operations: " + FileContent.getTotalReads());
		
		
		log(rank, random, randomSeed, seed, elementCount, tree, start, stop);
		
//		for(int i = 100; i < 200; i++)
//			tree.insert(i);
//		for(int i = 100; i < 200; i++)
//			if(tree.delete(i))
//				System.out.println(i + " removed");
//			else
//				System.out.println(i + " not found");
		
		
//		for(int i = 1; i > 1000; i--)
//		{
//			if (tree.delete(i))
//				System.out.println(i + " removed");
//			else
//				System.out.println(i + " not found");
//		}
		
		tree = new BTree(5);
		tree.insert(1);
		tree.delete(1);
		tree.delete(1);
		return;


//		for(int i=0; i<13; i++)
//			System.out.println("Contains " + i + " = " + tree.contains(i));
	}
	
	private static void log(int rank, boolean random, boolean randomSeed, long seed, int elementCount, BTree tree, long start, long stop) throws IOException
	{
		FileWriter fileWriter = new FileWriter("resultLog.txt", true);
		StringBuilder stringBuilder = new StringBuilder();
		
		stringBuilder.append("Elements count: ");
		stringBuilder.append(elementCount);
		stringBuilder.append(" | Rank: ");
		stringBuilder.append(rank);
		stringBuilder.append(" | Input type: ");
		if(random)
		{
			stringBuilder.append("Random (seed: ");
			if(randomSeed)
				stringBuilder.append("random");
			else
				stringBuilder.append(seed);
			stringBuilder.append(") | ");
		}
		else
			stringBuilder.append("Ascending sequence | ");
		
		stringBuilder.append("Number of write operations : ");
		stringBuilder.append(FileContent.getTotalWrites());
		stringBuilder.append(" | Number of read operations : ");
		stringBuilder.append(FileContent.getTotalReads());
		stringBuilder.append(" | Time: ");
		stringBuilder.append((stop - start));
		stringBuilder.append(" | Nodes created: ");
		stringBuilder.append(tree.filesCreated);
		stringBuilder.append("\n");
		
		fileWriter.write(stringBuilder.toString());
		fileWriter.flush();
		fileWriter.close();
	}
}
