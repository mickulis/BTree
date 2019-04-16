
/**
 * 	Autor: Michał Kulis
 *
 * 	Zakres projektu:
 *
 * 		Zadanie 1: dodawanie i wyszukiwanie elementów
 *
 *		Zadanie 2: usuwanie elementów
 *
 *		Zadanie 3: węzły przechowywane w oddzielnych plikach
 *			każde nowe drzewo przechowywane w nowym katalogu
 *			możliwość zmiany parametrów (t, liczba elementów, typ danych (random/ascending), random seed)
 *			liczba odczytów i zapisów przechowywana w pliku z logami
 *
 */


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class BTree
{
	
	static boolean printlogs = false;
	int filesCreated = 1;
	int filesDeleted = 0;
	
	private int rank;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	private String rootPath;
	private String lastFilename = " ";
	private String directory;
	private long filenamemod = 0;
	
	
	
	public BTree(int rank) throws IOException
	{
		if(rank < 2)
			this.rank = 2;
		else
			this.rank = rank;
		directory = dateFormat.format(new Date());
		new File(directory).mkdir();
		rootPath = directory + "/root.node";
		
		new File(rootPath).createNewFile();
	}
	
	public BTree(int rank, String directory) throws IOException
	{
		if(rank < 2)
			this.rank = 2;
		else
			this.rank = rank;
		this.directory = directory;
		new File(directory).mkdir();
		this.rootPath = directory + "/root.node";
		new File(rootPath).createNewFile();
	}
	
	
	
	public void insert(int value) throws IOException
	{
		
		if(printlogs) System.out.println("Inserting " + value);
		
		FileContent root = new FileContent(rootPath);
		int index = findIndex(value, root);
		/**
		 * 	if root is at max capacity, split it
		 */
		if(root.getKeys().size() == (rank * 2 - 1))
		{
			boolean goLeft = index < rank;
			if(printlogs) System.out.println("Full Root: splitting");
			FileContent nextNode = splitRoot(root, goLeft);
			insert(value, nextNode, root);
		}
		else
		{
			if (root.getChildrenPaths().size() == 0)    // root = leaf
			{
				root.insertKey(value);
				root.write();
			}
			else
			{
				FileContent nextNode = new FileContent(root.getChildrenPaths().get(index));
				insert(value, nextNode, root);
			}
		}
	}
	
	private void insert(int value, FileContent currentNode, FileContent parentNode) throws IOException
	{
		int index = findIndex(value, currentNode);
		
		if(currentNode.getKeys().size() == (rank * 2 - 1))
		{
			boolean goLeft = index < rank;
			if(printlogs) System.out.println("Full node encountered: splitting");
			FileContent nextNode = split(currentNode, parentNode, goLeft);
			insert(value, nextNode, currentNode);
		}
		else
		{
			if (currentNode.getChildrenPaths().size() == 0)
			{
				currentNode.insertKey(value);
				currentNode.write();
				parentNode.write();
			}
			else
			{
				parentNode.write();
				FileContent nextNode = new FileContent(currentNode.getChildrenPaths().get(index));
				insert(value, nextNode, currentNode);
			}
		}
	}
	
	private int findIndex(int value, FileContent parentContent)
	{
		int iterator = 0;
		
		while(iterator < parentContent.getKeys().size() && value > parentContent.getKeys().get(iterator))
		{
			if(printlogs) System.out.println("value: " + value + " > " + parentContent.getKeys().get(iterator));
			iterator++;
		}
		if(printlogs)
			if(iterator < parentContent.getKeys().size())
				System.out.println("index of first higher element(" + parentContent.getKeys().get(iterator) + "): " + iterator + "\n");
			else
				System.out.println("no higher element exists(max " + parentContent.getKeys().get(iterator - 1) + ") returning last index+1: " + iterator + "\n");
		return iterator;
	}
	
	
	private FileContent splitRoot(FileContent root, boolean returnLeft) throws IOException
	{
		/**
		 * 	Generate key/children lists for new root and its new children
		 */
		ArrayList<Integer> newRootKey = new ArrayList<>();
		ArrayList<String> newRootChildren = new ArrayList<>();
		
		ArrayList<Integer> newLeftKeys = new ArrayList<>();
		ArrayList<String> newLeftChildren = new ArrayList<>();
		
		/**
		 * 	move first half of old root keys into left child
		 */
		
		for(int i = 0; i < rank - 1; i++)
		{
			newLeftKeys.add(root.removeLowestKey());
		}
		
		/**
		 *  move a key in the middle of old root list into new root
		 */
		newRootKey.add(root.removeLowestKey());
		
		
		/**
		 * 	move first half of old root children into left child
		 */
		if(root.getChildrenPaths().size() > 0)
		{
			for (int i = 0; i < rank; i++)
			{
				newLeftChildren.add(root.getChildrenPaths().remove(0));
			}
		}
		
		/**
		 * 	generate new filenames and add to new root list
		 */
		newRootChildren.add(generateFilename());
		newRootChildren.add(generateFilename());
		
		/**
		 * 	set up new children and new root
		 */
		FileContent left = new FileContent(newLeftKeys, newLeftChildren, newRootChildren.get(0));
		root.setFilePath(newRootChildren.get(1));	// everything not deleted from root is basically the right child
		FileContent newRoot = new FileContent(newRootKey, newRootChildren, rootPath);
		newRoot.write();
		
		if(returnLeft)	// save right, return left
		{
			root.write();
			return left;
		}
		else	// save left, return right
		{
			left.write();
			return root;
		}
		
	}
	
	
	private FileContent split(FileContent child, FileContent parent, boolean returnLeft) throws IOException
	{
		/**
		 * 	Generate key/children lists for new children
		 */
		
		ArrayList<Integer> newLeftKeys = new ArrayList<>();
		ArrayList<String> newLeftChildren = new ArrayList<>();
		
		
		
		
		/**
		 * 	move first half of old child keys into left child
		 */
		for(int i = 0; i < rank - 1; i++)
		{
			newLeftKeys.add(child.removeLowestKey());
		}
		
		/**
		 *  move a key in the middle of old child list into parent
		 */
		int newParentKey = child.removeLowestKey();
		parent.insertKey(newParentKey);
		
		
		/**
		 * 	move first half of old root children into left child
		 */
		if(child.getChildrenPaths().size() > 0)
		{
			for (int i = 0; i < rank; i++)
			{
				newLeftChildren.add(child.getChildrenPaths().remove(0));
			}
		}
		/**
		 * 	generate new filename for left child and add to parent list
		 */
		
		
		String newLeftFilename = generateFilename();
		int movedKeyIndex = parent.getKeys().indexOf(newParentKey);
		parent.getChildrenPaths().add(movedKeyIndex, newLeftFilename);
		parent.write();
		/**
		 * 	write new data into files
		 */
		FileContent left = new FileContent(newLeftKeys, newLeftChildren, newLeftFilename);
		
		
		if(returnLeft)	// save right, return left
		{
			child.write();
			return left;
		}
		else	// save left, return right
		{
			left.write();
			return child;
		}
	}
	
	
	
	private int removeHighest(FileContent currentNode) throws IOException	// deletes highest value from subtree with currentNode as a root and returns it
	{
		if(currentNode.getChildrenPaths().size() == 0)
		{
			int highestKey = currentNode.removeIndexKey(currentNode.getKeys().size() - 1);
			if(printlogs) System.out.println("Next highest key found:" + highestKey);
			currentNode.write();
			return highestKey;
		}
		
		FileContent nextNode = prepareNextNode(currentNode, currentNode.getKeys().size() - 1);
		return removeHighest(nextNode);
	}
	
	
	public boolean delete(int value) throws IOException
	{
		if(printlogs) System.out.println("Deleting " + value);
		
		FileContent root = new FileContent(rootPath);
		
		if(root.getChildrenPaths().size() == 0 || root.getKeys().size() > 1)	// if root has 2 or more keys, or doesn't have children, it behaves as if it was any other node
		{
			if(printlogs) System.out.println("Root doesn't have children or is at size > 1");
			return delete(value, root);
		}
		else	// if root has exactly 1 key, root merge might be necessary
		{
			if(printlogs) System.out.println("Root has children and only 1 key");
			
			int index;
			boolean deletingRootKey = false;
			if(root.getKeys().get(0) == value)
			{
				if(printlogs) System.out.println("Value = root key: checking left node");
				index = 0;
				deletingRootKey = true;
			}
			else
			{
				index = findIndex(value, root);
			}
			
			FileContent nextNode = new FileContent(root.getChildrenPaths().get(index));
			if(nextNode.getKeys().size() > rank - 1)	// if next node is not at minimum capacity move to that node
			{
				if(printlogs) System.out.println("Next node not at min cap");
				if(deletingRootKey)
				{
					if(printlogs) System.out.println("Deleting root key, looking for highest value in left subtree");
					root.removeKey(value);
					root.insertKey(removeHighest(nextNode));
					root.write();
					return true;
				}
				return delete(value, nextNode);
			}
			else
			{
				if(printlogs) System.out.println("Next node at min cap");
				if(index == 0)
				{
					FileContent rightBrother = new FileContent(root.getChildrenPaths().get(1));
					
					if(rightBrother.getKeys().size() > rank - 1)
					{
						if(printlogs) System.out.println("Brother not at min cap: rotate");
						rotateLeft(0, root, nextNode, rightBrother);
						return delete(value, nextNode);
					}
					else
					{
						if(printlogs) System.out.println("Brother at min cap: merging");
						root = rootMerge(root, nextNode, rightBrother);
						return delete(value, root);
					}
				}
				else
				{
					FileContent leftBrother = new FileContent(root.getChildrenPaths().get(0));
					
					if(leftBrother.getKeys().size() > rank - 1)
					{
						if(printlogs) System.out.println("Brother not at min cap: rotate");
						rotateRight(0, root, leftBrother, nextNode);
						return delete(value, nextNode);
						
					}
					else
					{
						if(printlogs) System.out.println("Brother at min cap: merging");
						root = rootMerge(root, leftBrother, nextNode);
						return delete(value, root);
						
					}
				}
			}
		}
	}
	
	private boolean delete(int value, FileContent currentNode) throws IOException
	{
		int index = findIndex(value, currentNode);
		
		
		if(currentNode.getKeys().size() > index && currentNode.getKeys().get(index) == value)
		{
			if(printlogs) System.out.println(currentNode.getFilePath() + " Key found: deleting");
			
			if(currentNode.getChildrenPaths().size() == 0)
			{
				if(printlogs) System.out.println("Node is a leaf: done");
				currentNode.removeKey(value);
				currentNode.write();
			}
			else
			{
				if(printlogs) System.out.println("Node is not a leaf: finding highest key lower than deleted to replace");
				FileContent nextNode = prepareNextNode(currentNode, index);
				currentNode.removeKey(value);
				currentNode.insertKey(removeHighest(nextNode));
				currentNode.write();
			}
			
			return true;
		}
		if(currentNode.getChildrenPaths().size() == 0)
		{
			if(printlogs) System.out.println("Key not found: no keys deleted");
			currentNode.write();
			return false;
		}
		
		/**
		 * 	current node has children - check next node's capacity
		 */
		
		FileContent nextNode = prepareNextNode(currentNode, index);
		currentNode.write();
		return delete(value, nextNode);
	}
	
	private FileContent prepareNextNode(FileContent currentNode, int index) throws IOException
	{
		FileContent nextNode = new FileContent(currentNode.getChildrenPaths().get(index));
		if(printlogs) System.out.println("Preparing next node:" + nextNode.getFilePath());
		
		/**
		 * 	if next node is at minimum capacity
		 */
		if (nextNode.getKeys().size() == rank - 1)
		{
			if(printlogs) System.out.println("Node at minimum capacity");
			
			
			/**
			 * 	if left brother exists
			 */
			if (index > 0)
			{
				if(printlogs) System.out.println("Left brother exists");
				FileContent leftBrother = new FileContent(currentNode.getChildrenPaths().get(index - 1));
				if (leftBrother.getKeys().size() > rank - 1)
				{
					if(printlogs) System.out.println("Left brother not at minimum capacity: rotating right");
					nextNode = rotateRight(index - 1, currentNode, leftBrother, nextNode);
				}
				else
				{
					if(printlogs) System.out.println("Left brother at minimum capacity: merging");
					nextNode = merge(index - 1, currentNode, leftBrother, nextNode);	// index of a key between two children is equal to right child's index - 1
				}
			}
			/**
			 * 	if left brother does not exist
			 */
			else
			{
				if(printlogs) System.out.println("Left brother doesn't exist: reading right brother");
				FileContent rightBrother = new FileContent(currentNode.getChildrenPaths().get(index + 1));
				if (rightBrother.getKeys().size() > rank - 1)
				{
					if(printlogs) System.out.println("Right brother not at minimum capacity: rotating left");
					//rotate left
					nextNode = rotateLeft(index, currentNode, nextNode, rightBrother);
				}
				else
				{
					if(printlogs) System.out.println("Right brother at minimum capacity: merging");
					nextNode = merge(index, currentNode, nextNode, rightBrother);	// index points at leftmost key
				}
			}
		}
		return nextNode;
	}
	
	
	private FileContent rootMerge(FileContent root, FileContent leftChild, FileContent rightChild) throws IOException
	{
		if(printlogs) System.out.println("Commencing root merge");
		
		leftChild.insertKey(root.getKeys().get(0));
		for(int key: rightChild.getKeys())
			leftChild.insertKey(key);
		
		
		while(rightChild.getChildrenPaths().size() > 0)
		{
			leftChild.getChildrenPaths().add(rightChild.getChildrenPaths().remove(0));
		}
		if(printlogs) System.out.println("Deleting file " + leftChild.getFilePath());
		leftChild.delete();
		if(printlogs) System.out.println("Deleting file " + rightChild.getFilePath());
		rightChild.delete();
		leftChild.setFilePath(rootPath);
		if(printlogs) System.out.println("Root merge completed");
		return leftChild;
	}
	
	private FileContent merge(int index, FileContent parentNode, FileContent leftChild, FileContent rightChild) throws IOException
	{
		if(printlogs) System.out.println("Commencing merge");
		
		leftChild.insertKey(parentNode.removeIndexKey(index));
		parentNode.getChildrenPaths().remove(index + 1);
		for(int key: rightChild.getKeys())
			leftChild.insertKey(key);
		
		
		while(rightChild.getChildrenPaths().size() > 0)
		{
			leftChild.getChildrenPaths().add(rightChild.getChildrenPaths().remove(0));
		}
		if(printlogs) System.out.println("Deleting file " + rightChild.getFilePath());
		rightChild.delete();
		parentNode.write();
		if(printlogs) System.out.println("Merge completed");
		return leftChild;
	}
	
	private FileContent rotateLeft(int index, FileContent parentNode, FileContent leftChild, FileContent rightChild) throws IOException
	{
		int promotedKey = rightChild.removeLowestKey();
		int demotedKey = parentNode.removeIndexKey(index);
		
		leftChild.insertKey(demotedKey);	// insert demoted key into left node
		parentNode.insertKey(promotedKey);	// insert promoted key into parent node
		
		if(rightChild.getChildrenPaths().size() > 0)
		{
			String passedChildPath = rightChild.getChildrenPaths().remove(0);
			leftChild.getChildrenPaths().add(passedChildPath);    // insert as a last child
		}
		
		parentNode.write();
		rightChild.write();
		return leftChild;
	}
	
	private FileContent rotateRight(int index, FileContent parentNode, FileContent leftChild, FileContent rightChild) throws IOException
	{
		int promotedKey = leftChild.removeHighestKey();
		int demotedKey = parentNode.removeIndexKey(index);
		
		rightChild.insertKey(demotedKey);	// insert demoted key into left node
		parentNode.insertKey(promotedKey);	// insert promoted key into parent node
		
		
		if(leftChild.getChildrenPaths().size() > 0)
		{
			String passedChildPath = leftChild.getChildrenPaths().remove(leftChild.getChildrenPaths().size() - 1);
			rightChild.getChildrenPaths().add(0, passedChildPath);    // insert as a first child
		}
		parentNode.write();
		leftChild.write();
		return rightChild;
	}
	
	
	public boolean contains(int value) throws IOException
	{
		FileContent root = new FileContent(rootPath);
		int index = findIndex(value, root);
		if(index < root.getKeys().size() && root.getKeys().get(index) == value)	// value found
			return true;
		
		if(root.getChildrenPaths().size() == 0)	// value not found and no children
			return false;
		
		FileContent child = new FileContent(root.getChildrenPaths().get(index));
		return contains(value, child);
		
	}
	
	private boolean contains(int value, FileContent currentNode) throws IOException
	{
		int index = findIndex(value, currentNode);
		if(index < currentNode.getKeys().size() && currentNode.getKeys().get(index) == value)	// value found
			return true;
		
		if(currentNode.getChildrenPaths().size() == 0)	// value not found and no children
			return false;
		
		FileContent child = new FileContent(currentNode.getChildrenPaths().get(index));
		return contains(value, child);
		
	}
	
	
	
	String generateFilename()
	{
		Date date = new Date();
		String filename = dateFormat.format(date);
		if(filename.compareTo(lastFilename) == 0)
		{
			filenamemod++;
		}
		else
		{
			lastFilename = filename;
			filenamemod = 0;
		}
		filename = directory + "/" + filename + "v" + filenamemod + ".node";
		if(printlogs) System.out.println("new filename: " + filename + "\n");
		filesCreated++;
		return filename;
	}
	
	
}
