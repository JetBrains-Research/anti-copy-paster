package org.jetbrains.research.anticopypaster.utils;

import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.*;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static com.intellij.openapi.roots.ui.configuration.ClasspathEditor.getName;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MetricsGathererTest {
    private PsiFile psiFile;
    private TestFixtureBuilder<IdeaProjectTestFixture> projectBuilder;
    private JavaCodeInsightTestFixture myFixture;
    @Before
    public void setUp() {
        projectBuilder = IdeaTestFixtureFactory.getFixtureFactory().createFixtureBuilder(getName());

        myFixture = JavaTestFixtureFactory.getFixtureFactory()
                .createCodeInsightFixture(projectBuilder.getFixture());
    }

//    @Test
//    public void testGatherer(){
//        TestFixtureBuilder<IdeaProjectTestFixture> projectBuilder =
//                IdeaTestFixtureFactory.getFixtureFactory().createFixtureBuilder(getName());
//        MetricsGatherer metricsGatherer = new MetricsGatherer();
//        assertTrue(true);
//    }
}
