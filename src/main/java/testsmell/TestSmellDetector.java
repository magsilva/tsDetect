package testsmell;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.apache.commons.lang3.StringUtils;
import testsmell.smell.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TestSmellDetector {

	private List<AbstractSmell> availableTestSmells;
	
    private List<AbstractSmell> testSmellsClassLevel;
    
    private List<AbstractSmell> testSmellsMethodLevel;

    public TestSmellDetector() {
    	initializeSmells();
    }

    private void initializeSmells() {
    	availableTestSmells = new ArrayList<>();
    	availableTestSmells.add(new AssertionRoulette());
    	availableTestSmells.add(new ConditionalTestLogic());
    	availableTestSmells.add(new ConstructorInitialization());
    	availableTestSmells.add(new DefaultTest());
    	availableTestSmells.add(new DependentTest());
        availableTestSmells.add(new DuplicateAssert());
        availableTestSmells.add(new EagerTest());
        availableTestSmells.add(new EmptyTest());
        availableTestSmells.add(new ExceptionCatchingThrowing());
        availableTestSmells.add(new GeneralFixture());
        availableTestSmells.add(new IgnoredTest());
        availableTestSmells.add(new LazyTest());
        availableTestSmells.add(new MagicNumberTest());
        availableTestSmells.add(new MysteryGuest());
        availableTestSmells.add(new PrintStatement());
        availableTestSmells.add(new RedundantAssertion());
        availableTestSmells.add(new ResourceOptimism());
        availableTestSmells.add(new SensitiveEquality());
        availableTestSmells.add(new SleepyTest());
        availableTestSmells.add(new UnknownTest());
        availableTestSmells.add(new VerboseTest());
        // TODO: add after importing and reviewing the test smells
        // availableTestSmells.add(new CatchingUnexpectedExceptions());
        // testSmellsClassLevel.add(new DuplicateTestCode());
        // testSmellsClassLevel.add(new LonelyTest());
        // testSmellsClassLevel.add(new TheSecretCatcher());
    }
    
    private void initializeClassLevelSmells() {
        testSmellsClassLevel = new ArrayList<>();
        testSmellsClassLevel.add(new AssertionRoulette());
        testSmellsClassLevel.add(new ConditionalTestLogic());
        testSmellsClassLevel.add(new ConstructorInitialization());
        testSmellsClassLevel.add(new DefaultTest());
        testSmellsClassLevel.add(new DependentTest());
        testSmellsClassLevel.add(new DuplicateAssert());
        testSmellsClassLevel.add(new EagerTest());
        testSmellsClassLevel.add(new EmptyTest());
        testSmellsClassLevel.add(new ExceptionCatchingThrowing());
        testSmellsClassLevel.add(new GeneralFixture());
        testSmellsClassLevel.add(new IgnoredTest());
        testSmellsClassLevel.add(new LazyTest());
        testSmellsClassLevel.add(new MagicNumberTest());
        testSmellsClassLevel.add(new MysteryGuest());
        testSmellsClassLevel.add(new PrintStatement());
        testSmellsClassLevel.add(new RedundantAssertion());
        testSmellsClassLevel.add(new ResourceOptimism());
        testSmellsClassLevel.add(new SensitiveEquality());
        testSmellsClassLevel.add(new SleepyTest());
        testSmellsClassLevel.add(new UnknownTest());
        testSmellsClassLevel.add(new VerboseTest());
        // TODO: add after importing and reviewing the test smells
        // testSmellsClassLevel.add(new CatchingUnexpectedExceptions());
        // testSmellsClassLevel.add(new DuplicateTestCode());
        // testSmellsClassLevel.add(new LonelyTest());
        // testSmellsClassLevel.add(new TheSecretCatcher());
    }

    private void initializeMethodLevelSmells() {
        testSmellsMethodLevel = new ArrayList<>();
        testSmellsMethodLevel.add(new AssertionRoulette());
        testSmellsMethodLevel.add(new ConditionalTestLogic());
        testSmellsMethodLevel.add(new DuplicateAssert());
        testSmellsMethodLevel.add(new EagerTest());
        testSmellsMethodLevel.add(new EmptyTest());
        testSmellsMethodLevel.add(new GeneralFixture());
        testSmellsMethodLevel.add(new IgnoredTest());
        testSmellsMethodLevel.add(new LazyTest());
        testSmellsMethodLevel.add(new ResourceOptimism());
        testSmellsMethodLevel.add(new MagicNumberTest());
        testSmellsMethodLevel.add(new MysteryGuest());
        testSmellsMethodLevel.add(new PrintStatement());
        testSmellsMethodLevel.add(new RedundantAssertion());
        testSmellsMethodLevel.add(new SensitiveEquality());
        testSmellsMethodLevel.add(new SleepyTest());
        testSmellsMethodLevel.add(new VerboseTest());
        testSmellsMethodLevel.add(new UnknownTest());
        // TODO: add after importing and reviewing the test smells
        // testSmellsMethodLevel.add(new CatchingUnexpectedExceptions());
        // testSmellsMethodLevel.add(new DuplicateTestCode());
        // testSmellsMethodLevel.add(new LonelyTest());
        // testSmellsMethodLevel.add(new TheSecretCatcher());
    }
    
    public void setTestSmells(List<AbstractSmell> testSmells) {
        this.testSmellsClassLevel = testSmells;
    }

    public void testSmellsMethodLevel(List<AbstractSmell> testSmells) {
        this.testSmellsMethodLevel = testSmells;
    }

    /**
     * Provides the names of the smells that tsDetect supports.
     *
     * @return list of smell names
     */
    public List<String> getTestSmellNames() {
        return availableTestSmells.stream().map(AbstractSmell::getSmellName).collect(Collectors.toList());
    }
    
    /**
     * Provides the names of the smells that are being checked for in the code at class level.
     *
     * @return list of smell names
     */
    public List<String> getTestSmellNamesClassLevel() {
        return testSmellsClassLevel.stream().map(AbstractSmell::getSmellName).collect(Collectors.toList());
    }

    /**
     * Provides the names of the smells that are being checked for in the code at method level.
     *
     * @return list of smell names
     */
    public List<String> getTestSmellNamesMethodLevel() {
        return testSmellsMethodLevel.stream().map(AbstractSmell::getSmellName).collect(Collectors.toList());
    }

    /**
     * Loads the java source code file into an AST and then analyzes it for the existence of the different types of test smells at class level.
     */
    public TestFile detectSmells(TestFile testFile) throws IOException {
    	return detectSmellsClassLevel(testFile);
    }

    /**
     * Loads the java source code file into an AST and then analyzes it for the existence of the different types of test smells at class level.
     */
    public TestFile detectSmellsClassLevel(TestFile testFile) throws IOException {
        initializeClassLevelSmells();
    	return detectSmells(testFile, testSmellsClassLevel);
    }
    
    /**
     * Loads the java source code file into an AST and then analyzes it for the existence of the different types of test smells at class level.
     */
    public TestFile detectSmellsMethodLevel(TestFile testFile) throws IOException {
        initializeMethodLevelSmells();
    	return detectSmells(testFile, testSmellsMethodLevel);
    }
    
    /**
     * Loads the java source code file into an AST and then analyzes it for the existence of the different types of test smells
     */
    public TestFile detectSmells(TestFile testFile, List<AbstractSmell> testSmells) throws IOException {
        CompilationUnit testFileCompilationUnit = null;
        CompilationUnit productionFileCompilationUnit = null;
        FileInputStream testFileInputStream;
        FileInputStream productionFileInputStream;

        if(! StringUtils.isEmpty(testFile.getTestFilePath())) {
            testFileInputStream = new FileInputStream(testFile.getTestFilePath());
            testFileCompilationUnit = JavaParser.parse(testFileInputStream);
        }

        if(! StringUtils.isEmpty(testFile.getProductionFilePath())){
            productionFileInputStream = new FileInputStream(testFile.getProductionFilePath());
            productionFileCompilationUnit = JavaParser.parse(productionFileInputStream);
        }

        for (AbstractSmell smell : testSmells) {
            try {
                smell.runAnalysis(
                	testFileCompilationUnit,
                	productionFileCompilationUnit,
                	testFile.getTestFileNameWithoutExtension(),
                	testFile.getProductionFileNameWithoutExtension()
                );
            } catch (FileNotFoundException e) {
                testFile.addSmell(null);
                continue;
            }
            testFile.addSmell(smell);
        }
        
        return testFile;

    }


}
