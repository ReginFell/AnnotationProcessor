package processor.annotation.presentation.core.command

import processor.annotation.presentation.core.BaseView

interface Command<in V : BaseView> {

    operator fun invoke(view: V)

}
