package processor.annotation.presentation.processor

import com.google.common.truth.Truth
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourcesSubjectFactory
import org.junit.Test


class ViewStateProcessorTest {

    private val input: String = """package processor.test;

import processor.annotation.presentation.annotation.ViewState;
import processor.annotation.presentation.core.BaseView;

@ViewState
public interface View extends BaseView {

    void withArguments(String test);

    void withoutArguments();

    void withoutTwoArguments(int one, Long two);
}
"""

    private val output: String = """// This file is auto-generated and should not be edited.
package processor.test;

import java.lang.Long;
import java.lang.String;
import processor.annotation.presentation.core.BaseViewState;

public class ViewState extends BaseViewState<View> implements View {
  public void withArguments(String arg0) {
    dispatch(view -> view.withArguments(arg0));}

  public void withoutArguments() {
    dispatch(view -> view.withoutArguments());}

  public void withoutTwoArguments(int arg0, Long arg1) {
    dispatch(view -> view.withoutTwoArguments(arg0, arg1));}
}
"""

    @Test
    fun test() {
        Truth.assert_()
                .about(JavaSourcesSubjectFactory.javaSources())
                .that(listOf(JavaFileObjects.forSourceString("processor/annotation/presentation/processor/View", input)))
                .processedWith(ViewStateProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(JavaFileObjects.forSourceString("processor/annotation/presentation/processor/ViewState", output))
    }
}