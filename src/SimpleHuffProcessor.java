/*  Student information for assignment:
 *
 *  On OUR honor, Alexander Lee and Luis Pabon, this programming assignment is OUR own work
 *  and WE have not provided this code to any other student.
 *
 *  Number of slip days used: 0
 *
 *  Alexander Lee
 *  UTEID: al55332
 *  email address: alexlee8210@gmail.com
 *  Grader name: Sai
 *
 *  Luis Pabon
 *  UTEID: lap3865
 *  email address: luisalepabon@utexas.edu
 *
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.TreeMap;

public class SimpleHuffProcessor implements IHuffProcessor {

    private IHuffViewer myViewer;

    private HuffmanTree tree;
    private int preprocessHeaderFormat;
    private int preprocessBitsSaved;
    
    /**
     * Preprocess data so that compression is possible ---
     * count characters/create tree/store state so that
     * a subsequent call to compress will work. The InputStream
     * is <em>not</em> a BitInputStream, so wrap it int one as needed.
     * @param in is the stream which could be subsequently compressed
     * @param headerFormat a constant from IHuffProcessor that determines what kind of
     * header to use, standard count format, standard tree format, or
     * possibly some format added in the future.
     * @return number of bits saved by compression or some other measure
     * Note, to determine the number of
     * bits saved, the number of bits written includes
     * ALL bits that will be written including the
     * magic number, the header format number, the header to
     * reproduce the tree, AND the actual data.
     * @throws IOException if an error occurs while reading from the input file.
     */
    public int preprocessCompress(InputStream in, int headerFormat) throws IOException {
    	TreeMap<Integer, Integer> map = new TreeMap<>();
    	preprocessHeaderFormat = headerFormat;
    	
    	int numOriginalBits = createMap(in, map);
    	tree = new HuffmanTree(map);
    	
    	int headerBits = 0;
    	if (headerFormat == IHuffConstants.STORE_COUNTS) {
			headerBits = IHuffConstants.ALPH_SIZE * IHuffConstants.BITS_PER_INT;
		} else if (headerFormat == IHuffConstants.STORE_TREE) {
			headerBits = IHuffConstants.BITS_PER_INT + tree.getTreeSizeInBits();
    	} else {
			myViewer.showError("This header format is not store tree or store counts");
			return -1;
		}
    	// data + magic number + bits from header + tree header constant
		int newBitsSize = tree.getTotalDataSizeInBits() + IHuffConstants.BITS_PER_INT
				+ headerBits + IHuffConstants.BITS_PER_INT;
		preprocessBitsSaved = numOriginalBits - newBitsSize;
		showString("Bits saved: " + preprocessBitsSaved);
    	return preprocessBitsSaved;
    }

    /**
     * For preprocess.
     * Creates a mapping of the bits read from in to their frequencies.
     * The mappings are stored in map, which is altered as a result of this function.
     * @param in is the stream which could be subsequently compressed
     * @param map the map that contains the mappings of bits to their frequencies
     * @return the total number of bits read from in
     * @throws IOException
     */
	private int createMap(InputStream in, TreeMap<Integer, Integer> map)
			throws IOException {
    	int n = in.read();
    	int numOriginalBits = 0;
    	while (n != -1) {
        	numOriginalBits += IHuffConstants.BITS_PER_WORD;
        	if (map.containsKey(n)) {
        		map.put(n, map.get(n) + 1);
        	} else {
        		map.put(n, 1);
        	}
        	n = in.read();
    	}
		map.put(IHuffConstants.PSEUDO_EOF, 1);
		
		return numOriginalBits;
    }
    
    /**
	 * Compresses input to output, where the same InputStream has
     * previously been pre-processed via <code>preprocessCompress</code>
     * storing state used by this call.
     * <br> pre: <code>preprocessCompress</code> must be called before this method
     * @param in is the stream being compressed (NOT a BitInputStream)
     * @param out is bound to a file/stream to which bits are written
     * for the compressed file (not a BitOutputStream)
     * @param force if this is true create the output file even if it is larger 
     * than the input file.
     * If this is false do not create the output file if it is larger than the input file.
     * @return the number of bits written.
     * @throws IOException if an error occurs while reading from the input file or
     * writing to the output file.
     */
    public int compress(InputStream in, OutputStream out, boolean force) throws IOException {
    	if (!force && preprocessBitsSaved < 0) {
    		myViewer.showError("Compressed file has " + -1 * preprocessBitsSaved
    				+ " more bits than uncompressed file.\n"
    				+ "Select \"force compression\" option to compress.");
    		return -1;
    	}
    	
    	int numBitsWritten = 0;
    	BitOutputStream bitOut = new BitOutputStream(out);
    	
    	// Write header first
    	bitOut.writeBits(IHuffConstants.BITS_PER_INT, IHuffConstants.MAGIC_NUMBER);
    	bitOut.writeBits(IHuffConstants.BITS_PER_INT, preprocessHeaderFormat);
    	numBitsWritten += IHuffConstants.BITS_PER_INT + IHuffConstants.BITS_PER_INT;
    	
    	// header content
    	int headerContentOutput = writeHeader(bitOut);
    	if (headerContentOutput == -1) {
    		return -1;
    	}
    	numBitsWritten += headerContentOutput;
    	
    	// data
    	numBitsWritten += compressData(in, bitOut);
    	bitOut.close();
    	
		showString("Bits written: " + numBitsWritten);
        return numBitsWritten;
    }

    /**
     * Compresses and writes the compressed data to bitOut from in.
     * @param in is the stream being compressed 
     * @param bitOut is bound to a file/stream to which bits are written
     * for the compressed file
     * @return the number of bits written.
     * @throws IOException if an error occurs while reading from the input file or
     * writing to the output file.
     */
	private int compressData(InputStream in, BitOutputStream bitOut) throws IOException {
    	int numBitsWritten = 0;
    	int input = in.read();
		String code;
    	while (input != -1) {
    		code = tree.getCode(input);
    		int numBits = code.length();
			bitOut.writeBits(numBits, Integer.parseInt(code, 2));
			numBitsWritten += numBits;
        	input = in.read();
    	}
    	
    	code = tree.getCode(IHuffConstants.PSEUDO_EOF);
    	bitOut.writeBits(code.length(), Integer.parseInt(code, 2));
    	numBitsWritten += code.length();
    	return numBitsWritten;
    }
    
    /**
     * For compress.
     * Writes the compression header content to bitOut
     * depending on the header from the preprocess.
     * @param bitOut is bound to a file/stream to which bits are written
     * for the compressed file
     * @return the number of bits written
     */
    private int writeHeader(BitOutputStream bitOut) {
    	int numBitsWritten = IHuffConstants.BITS_PER_INT;
    	if (preprocessHeaderFormat == IHuffConstants.STORE_COUNTS) {
    		int[] counts = new int[IHuffConstants.ALPH_SIZE];
    		tree.fillCounts(counts);
    		for(int i = 0; i < IHuffConstants.ALPH_SIZE; i++) {
    		    bitOut.writeBits(IHuffConstants.BITS_PER_INT, counts[i]);
    		}
    		
    		// number of bits written (fewer computations than inside loop)
		    numBitsWritten *= IHuffConstants.ALPH_SIZE;
    	} else if (preprocessHeaderFormat == IHuffConstants.STORE_TREE) {
    		// Size of tree representation
			bitOut.writeBits(IHuffConstants.BITS_PER_INT, tree.getTreeSizeInBits());
			
    		Iterator<TreeNode> treeIterator = tree.iterator();
    		while (treeIterator.hasNext()) {
				numBitsWritten += 1;
    			int val = treeIterator.next().getValue();
    			if (val == -1) {
    				bitOut.writeBits(1, 0);
    			} else {
    				bitOut.writeBits(1, 1);
    				bitOut.writeBits(IHuffConstants.BITS_PER_WORD + 1, val);
    				numBitsWritten += IHuffConstants.BITS_PER_WORD + 1;
    			}
    		}
    	} else {
    		myViewer.showError("This header format is not store tree or store counts");
    		return -1;
    	}
    	
    	return numBitsWritten;
    }
    
    /**
     * Uncompress a previously compressed stream in, writing the
     * uncompressed bits/data to out.
     * @param in is the previously compressed data (not a BitInputStream)
     * @param out is the uncompressed file/stream
     * @return the number of bits written to the uncompressed file/stream
     * @throws IOException if an error occurs while reading from the input file or
     * writing to the output file.
     */
    public int uncompress(InputStream in, OutputStream out) throws IOException {
    	BitOutputStream bitOut = new BitOutputStream(out);
    	BitInputStream bitIn = new BitInputStream(in);
    	
    	int magic = bitIn.readBits(IHuffConstants.BITS_PER_INT); // magic number
		if (magic != IHuffConstants.MAGIC_NUMBER) {
			myViewer.showError("Error reading compressed file. \n" +
		            "File did not start with the huff magic number.");
			bitIn.close();
			bitOut.close();
		    return -1;
		}
		
		HuffmanTree inputTree;
		int format = bitIn.readBits(IHuffConstants.BITS_PER_INT);
		if (format == IHuffConstants.STORE_COUNTS) {
			inputTree = createStoreCountsTree(bitIn);
    	} else if (format == IHuffConstants.STORE_TREE) {
    		inputTree = new HuffmanTree(bitIn);
    	} else {
			myViewer.showError("This header format is not store tree or store counts");
    		bitIn.close();
    		bitOut.close();
    		return -1;
    	}
		
		int numBitsWritten = decode(bitIn, bitOut, inputTree);
		showString("Bits written: " + numBitsWritten);
    	return numBitsWritten;
    }
    
    /**
     * Creates the Huffman tree using store counts header format
     * @param bitIn the header to read in
     * @return the Huffman tree created
     * @throws IOException if an error occurs while reading from the input file or
     * writing to the output file. 
     */
    private HuffmanTree createStoreCountsTree(BitInputStream bitIn) throws IOException {
    	TreeMap<Integer, Integer> map = new TreeMap<>();
		for (int i = 0; i < IHuffConstants.ALPH_SIZE; i++) {
			int frequency = bitIn.readBits(IHuffConstants.BITS_PER_INT);
			if (frequency != 0) {
				map.put(i, frequency);
			}
		}
		map.put(IHuffConstants.PSEUDO_EOF, 1);
		return new HuffmanTree(map);
    }
    
    /**
     * Writes out the decoded data to bitOut from bitIn
     * @param bitIn is the previously compressed data
     * @param bitOut is the uncompressed file/stream
     * @param ht the tree used to decode the input
     * @return the number of bits written
     * @throws IOException if an error occurs while reading from the input file or
     * writing to the output file.
     */
	private int decode(BitInputStream bitIn, BitOutputStream bitOut, HuffmanTree ht) throws IOException {
		int nextBit = bitIn.readBits(1);
		int numBitsWritten = 0;
		TreeNode node = ht.getRoot();
		boolean done = false;
		while (nextBit != -1 && !done) {
			if (nextBit == 0) {
				node = node.getLeft();
			} else {
				node = node.getRight();
			}
			
			// at leaf
			if (node.isLeaf()) {
				int value = node.getValue();
				if (value == IHuffConstants.PSEUDO_EOF) {
					done = true;
				} else {
					bitOut.writeBits(IHuffConstants.BITS_PER_WORD, value);
					numBitsWritten += IHuffConstants.BITS_PER_WORD;
					node = ht.getRoot();
				}
			}
			nextBit = bitIn.readBits(1);
		}

		bitOut.close();
		bitIn.close();
		return numBitsWritten;
	}

	/**
	 * Sets the IHuffViewer to viewer
	 */
    public void setViewer(IHuffViewer viewer) {
        myViewer = viewer;
    }
    
    /*
     * Display a string s
     */
    private void showString(String s){
        if (myViewer != null) {
            myViewer.update(s);
        }
    }

}
