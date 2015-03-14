package pl.mjedynak.idea.plugins.builder.writer;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import pl.mjedynak.idea.plugins.builder.gui.helper.GuiHelper;
import pl.mjedynak.idea.plugins.builder.psi.BuilderPsiClassBuilder;
import pl.mjedynak.idea.plugins.builder.psi.PsiHelper;
import pl.mjedynak.idea.plugins.builder.psi.model.PsiFieldsForBuilder;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;


@RunWith(MockitoJUnitRunner.class)
public class BuilderWriterComputableTest {

    private BuilderWriterComputable builderWriterComputable;

    @Mock private PsiHelper psiHelper;
    @Mock private GuiHelper guiHelper;
    @Mock private BuilderPsiClassBuilder builderPsiClassBuilder;
    @Mock private Project project;
    @Mock private PsiClass srcClass;
    @Mock private PsiFieldsForBuilder psiFieldsForBuilder;
    @Mock private PsiClass builderClass;
    @Mock private PsiFile psiFile;
    @Mock private PsiElement psiElement;
    private BuilderContext context;
    private String methodPrefix = "with";

    @Before
    public void setUp() {
        context = new BuilderContext(project, psiFieldsForBuilder, mock(PsiDirectory.class), "anyName", mock(PsiClass.class), methodPrefix);
        builderWriterComputable = new BuilderWriterComputable(builderPsiClassBuilder, context);
        setField(builderWriterComputable, "psiHelper", psiHelper);
        setField(builderWriterComputable, "guiHelper", guiHelper);
    }

    @Test
    public void shouldIncludeCurrentPlaceAsChangePlaceAndCreateBuilderAndNavigateToIt() {
        // given
        given(builderPsiClassBuilder.aBuilder(context)).willReturn(builderPsiClassBuilder);
        given(builderPsiClassBuilder.withFields()).willReturn(builderPsiClassBuilder);
        given(builderPsiClassBuilder.withPrivateConstructor()).willReturn(builderPsiClassBuilder);
        given(builderPsiClassBuilder.withInitializingMethod()).willReturn(builderPsiClassBuilder);
        given(builderPsiClassBuilder.withSetMethods(methodPrefix)).willReturn(builderPsiClassBuilder);
        given(builderPsiClassBuilder.withButMethod()).willReturn(builderPsiClassBuilder);
        given(builderPsiClassBuilder.build()).willReturn(builderClass);
        given(builderClass.getContainingFile()).willReturn(psiFile);
        given(builderClass.getLBrace()).willReturn(psiElement);

        // when
        PsiElement result = builderWriterComputable.compute();

        // then
        verify(guiHelper).includeCurrentPlaceAsChangePlace(project);
        verify(guiHelper).positionCursor(project, psiFile, psiElement);
        assertThat(result, is(instanceOf(PsiClass.class)));
        assertThat((PsiClass) result, is(builderClass));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldInvokeBuilderWriterErrorRunnableWhenExceptionOccurs() {
        // given
        given(builderPsiClassBuilder.aBuilder(context)).willThrow(IncorrectOperationException.class);
        Application application = mock(Application.class);
        given(psiHelper.getApplication()).willReturn(application);

        // when
        builderWriterComputable.compute();

        // then
        verify(application).invokeLater(any(BuilderWriterErrorRunnable.class));
    }
}
