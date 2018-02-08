package processor.annotation.presentation.core.command

import processor.annotation.presentation.core.BaseView

interface Processor<out T, in V : BaseView> {

    fun process(view: V): T

}
