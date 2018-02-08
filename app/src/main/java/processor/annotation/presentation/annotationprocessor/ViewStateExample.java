package processor.annotation.presentation.annotationprocessor;

import processor.annotation.presentation.annotation.ViewState;
import processor.annotation.presentation.core.BaseView;

@ViewState
public interface ViewStateExample extends BaseView {

    void withArguments(String test);

    void withoutArguments();

    void withoutTwoArguments(int one, Long two);
}
