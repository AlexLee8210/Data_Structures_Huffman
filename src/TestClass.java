import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Scanner;

public class TestClass {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
//		BitInputStream in = new BitInputStream(new File("smallTxt.txt"));
//		
//		Byte b = 0010;
//		System.out.println(b);
		
//		HuffmanTree ht = new HuffmanTree();
		
		BitOutputStream out = new BitOutputStream(System.out);
		out.writeBits(IHuffConstants.BITS_PER_WORD + 1, 100011);
		out.close();
//		OutputStream os = System.out;
//		os.write(65);
//		os.close();
//		System.out.println(123);
	}

}
