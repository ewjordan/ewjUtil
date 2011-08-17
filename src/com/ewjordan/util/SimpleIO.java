package com.ewjordan.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A class with static methods to perform simple loading
 * and saving of basic CSV files.
 * <BR><BR>
 * No special handling for embedded strings or anything
 * like that, just "plain old" lists of doubles, and title
 * strings are expected to be "safe" (i.e. no commas,
 * newlines, quoting, etc.).
 * <BR><BR>
 * Uses the "output" directory in this project as the save/load
 * directory, unless you toggle the {@link #USE_ABSOLUTE_PATHS} flag
 * to true, in which case you must specify a full path for each file.
 * 
 * @author eric
 */
public class SimpleIO {
	/** If true, you must use full paths when using SimpleIO. */
	static public boolean USE_ABSOLUTE_PATHS = false;
	
	/** Save a Serializable object to a file. */
	static public void serializeObject(Object obj, String toFilename) {
		String outputPath = getOutputPath(toFilename);
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		
		try {
			fos = new FileOutputStream(outputPath);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(obj);
			oos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/** Load a Serializable object from a file.  It must be cast to its type after loading. */
	static public Object unserializeObject(String fromFilename) {
		String inputPath = getOutputPath(fromFilename);
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		
		try {
			fis = new FileInputStream(inputPath);
			ois = new ObjectInputStream(fis);
			return ois.readObject();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	static public void printCSVLine(Object ...ds) {
		for (int i=0; i<ds.length; ++i) {
			System.out.print(ds[i].toString());
			if (i < ds.length-1) System.out.print(",");
		}
		System.out.println();
	}

	static public void printCSVLine(double ...ds) {
		for (int i=0; i<ds.length; ++i) {
			System.out.print(ds[i]);
			if (i < ds.length-1) System.out.print(",");
		}
		System.out.println();
	}
	
	static public List<List<String>> splitStringsFromFile(final String filename, final String delimiter) {
		List<String> strings = stringsFromFile(filename);
		List<List<String>> splitStrings = new ArrayList<List<String>>(strings.size());
		for (int i=0; i<strings.size(); ++i) {
			String[] split = strings.get(i).split(delimiter);
			List<String> curr = new ArrayList<String>(split.length);
			splitStrings.add(curr);
			for (String s:split) {
				curr.add(s);
			}
		}
		return splitStrings;
	}
	
	/** Load a list of strings from a CSV file. Each string is one row. */
	static public List<String> stringsFromFile(final String filename) {
		String fullPath = getOutputPath(filename);
		ArrayList<String> res = new ArrayList<String>();
		BufferedReader in = null;
		try {
	        in = new BufferedReader(new FileReader(fullPath));
	        String str;
	        while ((str = in.readLine()) != null) {
	    		res.add(str);
	        }
	    } catch (FileNotFoundException e) {
	    	System.err.println("File not found: "+fullPath);
	    } catch (IOException e) {
	    	e.printStackTrace();
	    } finally {
	    	try{
	    		if (in != null) in.close();
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    	}
	    }
	    return res;
	}
	
	
	/** Returns a list of rows loaded from a CSV file, parsed into DoubleList form (one DoubleList for each row). */
	static public List<DoubleList> doubleListColumnListFromFile(final String filename, int expectedSize) {
		String fullPath = getOutputPath(filename);
		ArrayList<DoubleList> res = new ArrayList<DoubleList>();
		BufferedReader in = null;
		try {
	        in = new BufferedReader(new FileReader(fullPath));
	        String str;
	        boolean inited = false;
	        int nCols = -1;
	        int count = 0;
	        while ((str = in.readLine()) != null) {
	        	++count;
	        	if (count % 1000 == 0) System.out.println(count);
	        	try {
	        		String[] split = str.split(",");
	        		if (split.length < 2 && split[0].contains("\t")) {
	        			split = str.split("\t");
	        		}
	        		if (!inited) {
	        			nCols = split.length;
	        			for (int i=0; i<nCols; ++i) {
	        				res.add(new DoubleList(expectedSize));
	        			}
	        			System.out.println("Created "+nCols+" columns.");
	        			inited = true;
	        		}
	        		for (int j=0; j<split.length; ++j) {
	        			double f = Double.parseDouble(split[j]);
	        			res.get(j).add(f);
	        		}
	        	} catch (NumberFormatException e) {
	        		//skip header line
	        		System.out.println("Line skipped");
//	        		e.printStackTrace();
	        	}
	        }
	    } catch (FileNotFoundException e) {
	    	System.err.println("File not found: "+fullPath);
	    } catch (IOException e) {
	    	e.printStackTrace();
	    } finally {
	    	try{
	    		if (in != null) in.close();
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    	}
	    }
	    
	    return res;
	}
	
	
	/** Returns a list of columns loaded from a CSV file, parsed into List<Double> form (one for each column). */
	static public List<List<Double>> doubleColumnListFromFile(final String filename) {
		List<List<Double>> list = doubleRowListFromFile(filename);
		return transposeMatrix(list);
	}
	
	/** Returns a list of rows loaded from a CSV file. */
	static public List<List<Double>> doubleRowListFromFile(final String filename) {
		String fullPath = getOutputPath(filename);
		ArrayList<List<Double>> res = new ArrayList<List<Double>>();
		BufferedReader in = null;
		try {
	        in = new BufferedReader(new FileReader(fullPath));
	        String str;
	        while ((str = in.readLine()) != null) {
	    		ArrayList<Double> row = new ArrayList<Double>();
	        	try {
	        		String[] split = str.split(",");
	        		for (String num:split) {
	        			double f = Double.parseDouble(num);
	        			row.add(f);
	        		}
	        		res.add(row);
	        	} catch (NumberFormatException e) {
	        		//skip header line
	        	}
	        }
	    } catch (FileNotFoundException e) {
	    	System.err.println("File not found: "+fullPath);
	    } catch (IOException e) {
	    	e.printStackTrace();
	    } finally {
	    	try{
	    		if (in != null) in.close();
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    	}
	    }
	    
	    return res;
	}
	
	/** Return an array of doubles from a newline-separated list in a file. */
	static public double[] doublesFromFile(final String filename) {
		String fullPath = getOutputPath(filename);
		ArrayList<Double> doubles = new ArrayList<Double>();
		BufferedReader in = null;
		try {
	        in = new BufferedReader(new FileReader(fullPath));
	        String str;
	        
	        while ((str = in.readLine()) != null) {
	        	try {
	        		double f = Double.parseDouble(str);
	        		doubles.add(f);
	        	} catch (NumberFormatException e) {
	        		//skip header line
	        	}
	        }
	    } catch (FileNotFoundException e) {
	    	System.err.println("File not found: "+fullPath);
	    } catch (IOException e) {
	    	e.printStackTrace();
	    } finally {
	    	try{
	    		if (in != null) in.close();
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    	}
	    }
	    
	    double[] res = new double[doubles.size()];
	    for (int i=0; i<res.length; ++i) {
	    	res[i] = doubles.get(i);
	    }
	    return res;
	}
	
	/** 
	 * Get the output path corresponding to the passed filename.
	 * Does not append a path if you pass an absolute path (which
	 * starts with '/').
	 * <BR><BR>
	 * May be used to access files on the output path, as well (this is more
	 * of a utility function to access the project working path than
	 * specifically for output).
	 * @param fileName
	 * @return Full path to fileName
	 */
	static public String getOutputPath(String fileName) {
		if (USE_ABSOLUTE_PATHS) return fileName; //using absolute paths, do not append anything
		if (fileName.startsWith("/")) return fileName; //path is already specified
		return System.getProperty("user.dir")+"/output/"+fileName;
	}
	
	/** Save a double array to a file as a newline separated list. */
	static public void saveToFile(final double[] doubles, final String filename) {
		saveToFile(doubles, filename, "Values");
	}
	
	/** Save a double array to a file as a newline separated list with the given column title. */
	static public void saveToFile(final double[] doubles, final String _filename, final String columnTitle) {
		BufferedWriter out = null;
		String filename = getOutputPath(_filename);
		try {
			out = new BufferedWriter(new FileWriter(filename),8192*4);
			out.write(columnTitle);
			out.newLine();
			for (int i=0; i<doubles.length; ++i) {
				out.write(String.format("%.9f", doubles[i]));
				out.newLine();
			}
			System.out.println("Saved double array to "+filename);
		} catch (Exception e) {
	    	e.printStackTrace();
	    	System.err.println("Could not save file: "+filename);
	    } finally {
	    	try{
	    		if (out != null) {
	    			out.flush();
	    			out.close();
	    		}
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    	}
	    }
	}
	
	/** Save a List<Double> to a CSV file as a column with a title row. */
	static public void saveToFile(List<Double> doubles, final String _filename, final String columnTitle) {
		BufferedWriter out = null;
		String filename = getOutputPath(_filename);
		try {
			out = new BufferedWriter(new FileWriter(filename),8192*4);
			out.write(columnTitle);
			out.newLine();
			for (int i=0; i<doubles.size(); ++i) {
				out.write(String.format("%.5f", doubles.get(i)));
				out.newLine();
			}
			System.out.println("Saved double list to "+filename);
		} catch (Exception e) {
	    	e.printStackTrace();
	    	System.err.println("Could not save file: "+filename);
	    } finally {
	    	try{
	    		if (out != null) {
	    			out.flush();
	    			out.close();
	    		}
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    	}
	    }
	}
	
	/** Save strings to a file as a newline separated list. */
	static public void saveToFile(String[] strings, final String _filename) {
		List<String> slist = Arrays.asList(strings);
		saveToFile(slist, _filename);
	}
	
	/** Save strings to a file as a newline separated list. */
	static public void saveToFile(List<String> strings, final String _filename) {
		BufferedWriter out = null;
		String filename = getOutputPath(_filename);
		try {
			out = new BufferedWriter(new FileWriter(filename),8192*4);
//			out.newLine();
			for (int i=0; i<strings.size(); ++i) {
				out.write(strings.get(i));
				out.newLine();
			}
			System.out.println("Saved string list to "+filename);
		} catch (Exception e) {
	    	e.printStackTrace();
	    	System.err.println("Could not save file: "+filename);
	    } finally {
	    	try{
	    		if (out != null) {
	    			out.flush();
	    			out.close();
	    		}
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    	}
	    }
	}
	
	/** Append a list of strings to the end of a file, separated by newlines. */
	static public void appendToFile(List<String> strings, final String _filename) {
		BufferedWriter out = null;
		String filename = getOutputPath(_filename);
		try {
			out = new BufferedWriter(new FileWriter(filename,true),8192*4);
//			out.newLine();
			for (int i=0; i<strings.size(); ++i) {
				out.write(strings.get(i));
				out.newLine();
			}
			System.out.println("Appended string list to "+filename);
		} catch (Exception e) {
	    	e.printStackTrace();
	    	System.err.println("Could not save file: "+filename);
	    } finally {
	    	try{
	    		if (out != null) {
	    			out.flush();
	    			out.close();
	    		}
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    	}
	    }
	}
	
	/** Append a string to the end of a file, followed by a newline. */
	static public void appendToFile(String string, final String _filename) {
		BufferedWriter out = null;
		String filename = getOutputPath(_filename);
		try {
			out = new BufferedWriter(new FileWriter(filename,true),8192*4);
//			out.newLine();
			out.write(string);
			out.newLine();
			System.out.println("Appended string to "+filename);
		} catch (Exception e) {
	    	e.printStackTrace();
	    	System.err.println("Could not save file: "+filename);
	    } finally {
	    	try{
	    		if (out != null) {
	    			out.flush();
	    			out.close();
	    		}
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    	}
	    }
	}
	
	/** Save a string to a text file. */
	static public void saveToFile(String string, final String _filename) {
		BufferedWriter out = null;
		String filename = getOutputPath(_filename);
		try {
			out = new BufferedWriter(new FileWriter(filename),8192*4);
//			out.newLine();
			out.write(string);
			System.out.println("Saved string to "+filename);
		} catch (Exception e) {
	    	e.printStackTrace();
	    	System.err.println("Could not save file: "+filename);
	    } finally {
	    	try{
	    		if (out != null) {
	    			out.flush();
	    			out.close();
	    		}
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    	}
	    }
	}
	
	/** Save a matrix to a file with column titles (or without if columnTitles is null) */
	static public void saveToFile(double[][] doubles, final String _filename, final String[] columnTitles) {
		BufferedWriter out = null;
		String filename = getOutputPath(_filename);
		try {
			out = new BufferedWriter(new FileWriter(filename),8192*4);
			if (columnTitles != null) {
				for (int i=0; i<columnTitles.length; ++i) {
					out.write(columnTitles[i]);
					if (i != columnTitles.length-1) out.write(",");
				}
				out.newLine();
			}
			int cols = doubles.length;
			int rows = (cols>0)?doubles[0].length:0;
			for (int i=0; i<rows; ++i) {//lines
				for (int j=0; j<cols; ++j) {
					out.write(String.format("%.9f", doubles[j][i]));
					if (j != cols-1) out.write(",");
				}
				out.newLine();
			}
			System.out.println("Saved double matrix to "+filename);
		} catch (Exception e) {
	    	e.printStackTrace();
	    	System.err.println("Could not save file: "+filename);
	    } finally {
	    	try{
	    		if (out != null) {
	    			out.flush();
	    			out.close();
	    		}
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    	}
	    }
	}
	
	/**
	 * Write 2d double matrix (passed as a list of columns)
	 * to a file in CSV format with the given column titles.
	 * <BR><BR>
	 * If you have a list of rows instead of columns, use {@link #transposeMatrix(List)}
	 * to convert it to column-list format before writing it to a file. 
	 * @param doubles
	 * @param _filename
	 * @param columnTitles
	 */
	static public void saveToFile(List<List<Double>> doubles, final String _filename, final String[] columnTitles) {
		BufferedWriter out = null;
		String filename = getOutputPath(_filename);
		try {
			out = new BufferedWriter(new FileWriter(filename),8192*4);
			if (columnTitles != null) {
				for (int i=0; i<columnTitles.length; ++i) {
					out.write(columnTitles[i]);
					if (i != columnTitles.length-1) out.write(",");
				}
				out.newLine();
			}
			int cols = doubles.size();
			int rows = (cols>0)?doubles.get(0).size():0;
			for (int i=0; i<rows; ++i) {//lines
				for (int j=0; j<cols; ++j) {
					out.write(String.format("%.9f", doubles.get(j).get(i)));
					if (j != cols-1) out.write(",");
				}
				out.newLine();
			}
			System.out.println("Saved double matrix to "+filename);
		} catch (Exception e) {
	    	e.printStackTrace();
	    	System.err.println("Could not save file: "+filename);
	    } finally {
	    	try{
	    		if (out != null) {
	    			out.flush();
	    			out.close();
	    		}
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    	}
	    }
	}
	
	/** Create a string from a matrix. */
	static public String saveToString(List<List<Double>> doubles, final String[] columnTitles) {
		StringBuilder sb = new StringBuilder();
			if (columnTitles != null) {
				for (int i=0; i<columnTitles.length; ++i) {
					sb.append(columnTitles[i]);
					if (i != columnTitles.length-1) sb.append(",");
				}
				sb.append("\n");
			}
			int cols = doubles.size();
			int rows = (cols>0)?doubles.get(0).size():0;
			for (int i=0; i<rows; ++i) {//lines
				for (int j=0; j<cols; ++j) {
					sb.append(String.format("%.9f", doubles.get(j).get(i)));
					if (j != cols-1) sb.append(",");
				}
				sb.append("\n");
			}
			return sb.toString();
	}
	
	/**
	 * Write 2d double matrix (passed as a list of columns)
	 * to a file in CSV format with the given column titles.
	 * <BR><BR>
	 * If you have a list of rows instead of columns, use {@link #transposeMatrix(List)}
	 * to convert it to column-list format before writing it to a file. 
	 * @param doubles
	 * @param _filename
	 * @param columnTitles
	 */
	static public void saveDoubleListsToFile(List<DoubleList> doubles, final String _filename, final String[] columnTitles, int decimalPlaces) {
		BufferedWriter out = null;
		String filename = getOutputPath(_filename);
		try {
			out = new BufferedWriter(new FileWriter(filename),8192*4);
			if (columnTitles != null) {
				for (int i=0; i<columnTitles.length; ++i) {
					out.write(columnTitles[i]);
					if (i != columnTitles.length-1) out.write(",");
				}
				out.newLine();
			}
			int cols = doubles.size();
			int rows = (cols>0)?doubles.get(0).size():0;
			for (int i=0; i<rows; ++i) {//lines
				for (int j=0; j<cols; ++j) {
					out.write(String.format("%."+decimalPlaces+"f", doubles.get(j).get(i)));
					if (j != cols-1) out.write(",");
				}
				out.newLine();
			}
			System.out.println("Saved double matrix to "+filename);
		} catch (Exception e) {
	    	e.printStackTrace();
	    	System.err.println("Could not save file: "+filename);
	    } finally {
	    	try{
	    		if (out != null) {
	    			out.flush();
	    			out.close();
	    		}
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    	}
	    }
	}
	
	/** Save an integer matrix to a file, with column titles (or without if columnTitles is null) */
	static public void saveIntsToFile(List<List<Integer>> ints, final String _filename, final String[] columnTitles) {
		BufferedWriter out = null;
		String filename = getOutputPath(_filename);
		try {
			out = new BufferedWriter(new FileWriter(filename),8192*4);
			if (columnTitles != null) {
				for (int i=0; i<columnTitles.length; ++i) {
					out.write(columnTitles[i]);
					if (i != columnTitles.length-1) out.write(",");
				}
				out.newLine();
			}
			int cols = ints.size();
			int rows = (cols>0)?ints.get(0).size():0;
			for (int i=0; i<rows; ++i) {//lines
				for (int j=0; j<cols; ++j) {
					out.write(ints.get(j).get(i));
					if (j != cols-1) out.write(",");
				}
				out.newLine();
			}
			System.out.println("Saved int matrix to "+filename);
		} catch (Exception e) {
	    	e.printStackTrace();
	    	System.err.println("Could not save file: "+filename);
	    } finally {
	    	try{
	    		if (out != null) {
	    			out.flush();
	    			out.close();
	    		}
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    	}
	    }
	}
	
	/** 
	 * Return the transpose of a matrix specified as a list of lists of doubles.
	 * Does not alter the original matrix, instead returns a copy. 
	 */
	public static List<List<Double>> transposeMatrix(List<List<Double>> in) {
		double inCols = in.size();
		double inRows = (inCols > 0)?in.get(0).size():0;
		
		List<List<Double>> out = new ArrayList<List<Double>>();
		for (int i=0; i<inRows; ++i) {
			ArrayList<Double> newCol = new ArrayList<Double>();
			for (int j=0; j<inCols; ++j) {
				newCol.add(in.get(j).get(i));
			}
			out.add(newCol);
		}
		return out;
	}
	
	/** Test utility methods. */
	static public void main(String[] args) {
		// 1d double array save/load heavily tested in other files 
		
		// Test 2d double array saving and transposition
		List<List<Double>> list2 = new ArrayList<List<Double>>();
		String[] names = new String[15];
		for (int i=0; i<15; ++i) {
			names[i] = "column "+i;
			list2.add(new ArrayList<Double>());
			for (int j = 0; j<100; ++j) {
				double myDouble = j * (double)Math.random();
				list2.get(i).add(myDouble);
			}
		}
		saveToFile(list2, "test2darray.csv", names);
		List<List<Double>> list3 = transposeMatrix(list2);
		saveToFile(list3, "test2darrayT.csv", null);
		
		// Test 2d double array loading
		/*
		List<List<Double>> list = doubleRowListFromFile("test2darray.csv");
		System.out.println("Row list:");
		/*/
		List<List<Double>> list = doubleColumnListFromFile("test2darray.csv");
		System.out.println("Column list:");
		//*/
		for (List<Double> doubles:list) {
			for (Double f:doubles) {
				System.out.print(f+",");
			}
			System.out.println("");
		}
	}

	public static void printList(List<?> list) {
		int index = 0;
		for (Object o:list) {
			System.out.println("" + index++ + " : " + o);
		}
	}
	
}
