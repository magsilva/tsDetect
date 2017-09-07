package testsmell;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.apache.commons.lang3.StringUtils;
import testsmell.smell.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TestSmellDetector {

    private List<AbstractSmell> testSmells;

    /**
     * Instantiates the various test smell analyzer classes and loads the objects into an List
     */
    private TestSmellDetector() {

        testSmells = new ArrayList<>();
        testSmells.add(new AssertionRoulette());
        testSmells.add(new ConditionalTestLogic());
        testSmells.add(new ConstructorInitialization());
        testSmells.add(new DefaultTest());
        testSmells.add(new EmptyTest());
        testSmells.add(new ExceptionCatchingThrowing());
        testSmells.add(new GeneralFixture());
        testSmells.add(new MysteryGuest());
        testSmells.add(new PrintStatement());
        testSmells.add(new RedundantAssertion());
        testSmells.add(new SensitiveEquality());
        testSmells.add(new VerboseTest());
        testSmells.add(new WaitAndSee());
        testSmells.add(new EagerTest());
    }

    /**
     * Factory method that provides a new instance of the TestSmellDetector
     *
     * @return new TestSmellDetector instance
     */
    public static TestSmellDetector createTestSmellDetector() {
        return new TestSmellDetector();
    }

    /**
     * Provides the names of the smells that are being checked for in the code
     *
     * @return list of smell names
     */
    public List<String> getTestSmellNames() {
        return testSmells.stream().map(AbstractSmell::getSmellName).collect(Collectors.toList());
    }

    /**
     * Loads the java source code file into an AST and then analyzes it for the existence of the different types of test smells
     */
    public TestFile detectSmells(TestFile testFile) throws IOException {
        for (AbstractSmell smell : testSmells) {
            try {
                smell.runAnalysis(testFile.getTestFilePath(), testFile.getProductionFilePath());
            }
            catch (FileNotFoundException e) {
                testFile.addSmell(null);
                continue;
            }
            testFile.addSmell(smell);
        }

        return testFile;

    }


}
