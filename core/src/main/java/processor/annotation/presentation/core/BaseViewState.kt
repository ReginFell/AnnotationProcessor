package processor.annotation.presentation.core


import processor.annotation.presentation.core.command.Command
import processor.annotation.presentation.core.command.Processor
import java.util.Collections.emptyList

abstract class BaseViewState<V : BaseView> : BaseView {

    private val commands: MutableList<Command<V>> = emptyList()

    private var delegate: V? = null

    fun attachView(view: V) {
        this.delegate = view
        for (command in commands) {
            dispatch(command)
        }
        commands.clear()
    }

    fun detachView() {
        this.delegate = null
    }

    protected fun dispatch(command: Command<V>) {
        val delegate = delegate
        if (delegate != null) {
            command.invoke(delegate)
        } else {
            commands.add(command)
        }
    }

    protected fun <T> processUnsafe(processor: Processor<T, V>): T {
        val delegate = delegate
        return if (delegate != null) {
            processor.process(delegate)
        } else {
            throw RuntimeException("This method shouldn't be called when view detached")
        }
    }
}
