package testsmell.smell;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import testsmell.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EagerTest extends AbstractSmell {

    private static final String TEST_FILE = "Test";
    private static final String PRODUCTION_FILE = "Production";
    private String productionClassName;
    private List<SmellyElement> smellyElementList;

    public EagerTest() {
        smellyElementList = new ArrayList<>();
    }

    /**
     * Checks of 'Eager Test' smell
     */
    @Override
    public String getSmellName() {
        return "Eager Test";
    }

    /**
     * Returns true if any of the elements has a smell
     */
    @Override
    public boolean getHasSmell() {
        return smellyElementList.stream().filter(x -> x.getHasSmell()).count() >= 1;
    }

    /**
     * Analyze the test file for test methods that exhibit the 'Eager Test' smell
     *
     * @param testFilePath       The absolute path of the test file
     * @param productionFilePath the absolute path of the production file (that corresponds to the test file)
     */
    @Override
    public void runAnalysis(String testFilePath, String productionFilePath) throws FileNotFoundException {
        FileInputStream testFileInputStream = null;
        FileInputStream productionFileInputStream = null;
        try {
            testFileInputStream = new FileInputStream(testFilePath);
            productionFileInputStream = new FileInputStream(productionFilePath);
        } catch (FileNotFoundException e) {
            throw e;
        }

        CompilationUnit compilationUnit;
        EagerTest.ClassVisitor classVisitor;

        compilationUnit = JavaParser.parse(productionFileInputStream);
        classVisitor = new EagerTest.ClassVisitor(PRODUCTION_FILE);
        classVisitor.visit(compilationUnit, null);

        compilationUnit = JavaParser.parse(testFileInputStream);
        classVisitor = new EagerTest.ClassVisitor(TEST_FILE);
        classVisitor.visit(compilationUnit, null);

    }

    /**
     * Returns the set of analyzed elements (i.e. test methods)
     */
    @Override
    public List<SmellyElement> getSmellyElements() {
        return smellyElementList;
    }


    /**
     * Visitor class
     */
    private class ClassVisitor extends VoidVisitorAdapter<Void> {
        private MethodDeclaration currentMethod = null;
        TestMethod testMethod;
        private int eagerCount = 0;
        private List<String> productionVariables = new ArrayList<>();
        private List<String> calledMethods = new ArrayList<>();
        private String fileType;

        public ClassVisitor(String type) {
            fileType = type;
        }

        @Override
        public void visit(ClassOrInterfaceDeclaration n, Void arg) {
            if (Objects.equals(fileType, PRODUCTION_FILE)) {
                productionClassName = n.getNameAsString();
            }
            super.visit(n, arg);
        }

        /**
         * The purpose of this method is to 'visit' all test methods.
         */
        @Override
        public void visit(MethodDeclaration n, Void arg) {
            // ensure that this method is only executed for the test file
            if (Objects.equals(fileType, TEST_FILE)) {

                //only analyze methods that either have a @test annotation (Junit 4) or the method name starts with 'test'
                if (n.getAnnotationByName("Test").isPresent() || n.getNameAsString().toLowerCase().startsWith("test")) {
                    currentMethod = n;
                    testMethod = new TestMethod(currentMethod.getNameAsString());
                    testMethod.setHasSmell(false); //default value is false (i.e. no smell)
                    super.visit(n, arg);

                    testMethod.setHasSmell(eagerCount > 1); //the method has a smell if there is more than 1 call to production methods
                    System.out.println(n.getNameAsString() + ";" + eagerCount);

                    smellyElementList.add(testMethod);

                    //reset values for next method
                    currentMethod = null;
                    eagerCount = 0;
                    productionVariables = new ArrayList<>();
                    calledMethods = new ArrayList<>();
                }

            }
        }


        /**
         * The purpose of this method is to identify the production class methods that are called from the test method
         * When the parser encounters a method call, the code will check the 'scope' of the called method.
         * A match is made if the scope is either:
         * equal to the name of the production class (as in the case of a static method) or
         * if the scope is a variable that has been declared to be of type of the production class (i.e. contained in the 'productionVariables' list).
         */
        @Override
        public void visit(MethodCallExpr n, Void arg) {
            super.visit(n, arg);
            if (currentMethod != null) {
                if (n.getScope().isPresent()) {
                    if (n.getScope().get() instanceof NameExpr) {
                        //checks if the scope of the method being called is either of production class (e.g. static method)
                        //or
                        ///if the scope matches a variable which, in turn, is of type of the production class
                        if (((NameExpr) n.getScope().get()).getNameAsString().equals(productionClassName) ||
                                productionVariables.contains(((NameExpr) n.getScope().get()).getNameAsString())) {
                            if (!calledMethods.contains(n.getNameAsString())) {
                                eagerCount++;
                                calledMethods.add(n.getNameAsString());
                            }

                        }
                    }
                }
            }
        }

        /**
         * The purpose of this method is to capture the names of all variables, declared in the method body, that are of type of the production class.
         * The variable is captured as and when the code statement is parsed/evaluated by the parser
         */
        @Override
        public void visit(VariableDeclarationExpr n, Void arg) {
            if (currentMethod != null) {
                for (int i = 0; i < n.getVariables().size(); i++) {
                    if (productionClassName.equals(n.getVariable(i).getType().asString())) {
                        productionVariables.add(n.getVariable(i).getNameAsString());
                    }
                }
            }
            super.visit(n, arg);
        }
    }
}