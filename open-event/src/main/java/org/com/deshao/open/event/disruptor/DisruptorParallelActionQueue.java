package org.com.deshao.open.event.disruptor;

import java.util.LinkedList;
import java.util.Queue;

import org.com.deshao.open.event.disruptor.DisruptorParallelActionExecutor.ActionEvent;
import org.com.deshao.open.event.parallel.action.AbstractActionQueue;
import org.com.deshao.open.event.parallel.action.Action;

import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.dsl.Disruptor;

class DisruptorParallelActionQueue extends AbstractActionQueue{
	private Disruptor<ActionEvent> disruptor ;
	private EventTranslatorOneArg<ActionEvent,Action> translator;
	
	public DisruptorParallelActionQueue(Queue<Action> queue, Disruptor<ActionEvent> disruptor) {
		super(queue);
		this.disruptor = disruptor;
	}

	public DisruptorParallelActionQueue(Disruptor<ActionEvent> ringBuffer,EventTranslatorOneArg<ActionEvent,Action> translator) {
		super(new LinkedList<Action>());
		this.disruptor = ringBuffer;
		this.translator = translator;
	}
	
	public DisruptorParallelActionQueue(Queue<Action> queue) {
		super(queue);
	}

	@Override
	public void doExecute(Runnable runnable) {
		Action action = (Action) runnable;
		disruptor.publishEvent(translator, action);
	}
}
