import testsmell.AbstractSmell;
import testsmell.ResultsWriter;
import testsmell.SmellyElement;
import testsmell.TestFile;
import testsmell.TestSmellDetector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args == null) {
            System.out.println("Please provide the file containing the paths to the collection of test files");
            return;
        }
        if(!args[0].isEmpty()){
            File inputFile = new File(args[0]);
            if(!inputFile.exists() || inputFile.isDirectory()) {
                System.out.println("Please provide a valid file containing the paths to the collection of test files");
                return;
            }
        }

        TestSmellDetector testSmellDetector = new TestSmellDetector();

        /*
          Read the input file and build the TestFile objects
         */
        BufferedReader in = new BufferedReader(new FileReader(args[0]));
        String str;

        String[] lineItem;
        TestFile testFile;
        List<TestFile> testFiles = new ArrayList<>();
        while ((str = in.readLine()) != null) {
            // use comma as separator
            lineItem = str.split(",");

            //check if the test file has an associated production file
            if(lineItem.length ==2){
                testFile = new TestFile(lineItem[0], lineItem[1], "");
            }
            else{
                testFile = new TestFile(lineItem[0], lineItem[1], lineItem[2]);
            }

            testFiles.add(testFile);
        }

        /*
          Initialize the output file - Create the output file and add the column names
         */
        ResultsWriter resultsWriter = ResultsWriter.createResultsWriter();
        List<String> columnNames;
        List<String> columnValues;
        columnNames = new ArrayList<>();
        columnNames.add("App");
        columnNames.add("TestClass");
        columnNames.add("TestMethod");
        columnNames.add("TestFilePath");
        columnNames.add("ProductionFilePath");
        columnNames.add("RelativeTestFilePath");
        columnNames.add("RelativeProductionFilePath");
        columnNames.addAll(testSmellDetector.getTestSmellNames());
        resultsWriter.writeColumnName(columnNames);

        /*
          Iterate through all test files to detect smells and then write the output
        */
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        for (TestFile file : testFiles) {
            // detect smells at class level
            System.out.println(dateFormat.format(new Date()) + " Processing at class level: " + file.getTestFilePath());
            Map<String, String> classSmellResults = new LinkedHashMap<>();
            for (String testSmellNames : testSmellDetector.getTestSmellNames()) {
            	classSmellResults.put(testSmellNames, "");
            }
            file.clearSmells();
            testSmellDetector.detectSmellsClassLevel(file);
            columnValues = new ArrayList<>();
            columnValues.add(file.getApp());
            columnValues.add(file.getTestFileName());
            columnValues.add("");
            columnValues.add(file.getTestFilePath());
            columnValues.add(file.getProductionFilePath());
            columnValues.add(file.getRelativeTestFilePath());
            columnValues.add(file.getRelativeProductionFilePath());
            for (AbstractSmell smell : file.getTestSmells()) {
                try {
                	classSmellResults.put(smell.getSmellName(), String.valueOf(smell.getHasSmell()));
                } catch (NullPointerException e){
                	classSmellResults.put(smell.getSmellName(), "");
                }
            }
            for (String smellValue : classSmellResults.values()) {
            	columnValues.add(smellValue);
            }
            resultsWriter.writeLine(columnValues);
            
            // detect smells at method level
            System.out.println(dateFormat.format(new Date()) + " Processing at method level: " + file.getTestFilePath());
            file.clearSmells();
            testSmellDetector.detectSmellsMethodLevel(file);
            Map<String, Map<String, String>> allMethodsResult = new LinkedHashMap<>(); // Map of test path + test method name to a map of smell name and its value
            for (AbstractSmell smell : file.getTestSmells()) {
            	for (SmellyElement smellyElement : smell.getSmellyElements()) {
                    System.out.println(dateFormat.format(new Date()) + " Processing at method level: " + file.getTestFileNameWithoutExtension() + "." + smellyElement.getElementName());
                    // Create (empty) results on demand for each method which has at least one smell
                    Map<String, String> methodSmellResults;
                    if (! allMethodsResult.containsKey(smellyElement.getElementName())) {
                        methodSmellResults = new LinkedHashMap<>();
                        for (String testSmellNames : testSmellDetector.getTestSmellNames()) {
                        	methodSmellResults.put(testSmellNames, "");
                        }
                        allMethodsResult.put(smellyElement.getElementName(), methodSmellResults);
                    } else {
                    	methodSmellResults = allMethodsResult.get(smellyElement.getElementName());
                    }
                    try {
                    	methodSmellResults.put(smell.getSmellName(), String.valueOf(smellyElement.getHasSmell()));
                    } catch (NullPointerException e){
                    	methodSmellResults.put(smell.getSmellName(), "");
                    }
            	}
            }

            for (Map.Entry<String, Map<String, String>> entry : allMethodsResult.entrySet()) {
            	Map<String, String> methodSmellResults = new LinkedHashMap<>();
                for (String testSmellNames : testSmellDetector.getTestSmellNames()) {
                	methodSmellResults.put(testSmellNames, "");
                }
            	columnValues = new ArrayList<>();
            	columnValues.add(file.getApp());
            	columnValues.add(file.getTestFileName());
            	columnValues.add(entry.getKey());
            	columnValues.add(file.getTestFilePath());
            	columnValues.add(file.getProductionFilePath());
            	columnValues.add(file.getRelativeTestFilePath());
            	columnValues.add(file.getRelativeProductionFilePath());
            	for (AbstractSmell smell : file.getTestSmells()) {
                   	methodSmellResults.put(smell.getSmellName(), String.valueOf(entry.getValue().get(smell.getSmellName())));
                }
                for (String smellValue : methodSmellResults.values()) {
                	columnValues.add(smellValue);
                }
                resultsWriter.writeLine(columnValues);
            }
        }
        System.out.println("end");
    }


}
