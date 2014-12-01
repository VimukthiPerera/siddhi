/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org)
 * All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.core.query.processor.filter;

import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventIterator;
import org.wso2.siddhi.core.event.stream.converter.EventManager;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.query.api.definition.Attribute;


public class FilterProcessor implements Processor {

    protected Processor next;
    private ExpressionExecutor conditionExecutor;
    private StreamEventIterator iterator = new StreamEventIterator();
    private EventManager eventManager;

    public FilterProcessor(ExpressionExecutor conditionExecutor) {
        if (Attribute.Type.BOOL.equals(conditionExecutor.getReturnType())) {
            this.conditionExecutor = conditionExecutor;
        } else {
            throw new OperationNotSupportedException("Return type of " + conditionExecutor.toString() + " should be of type BOOL. " +
                    "Actual type: " + conditionExecutor.getReturnType().toString());
        }
    }

    public FilterProcessor cloneProcessor() {
        return new FilterProcessor(conditionExecutor.cloneExecutor());
    }

    @Override
    public void process(StreamEvent event) {
        iterator.assignEvent(event);
        try {
            while (iterator.hasNext()) {
                StreamEvent streamEvent = iterator.next();
                if (!(Boolean) conditionExecutor.execute(streamEvent)) {
                    iterator.remove();
                    eventManager.returnEvent(streamEvent);
                }
            }
            if (iterator.getFirst() != null) {
                this.next.process(iterator.getFirst());
            }
        } finally {
            iterator.clear();
        }
    }

    @Override
    public Processor getNextProcessor() {
        return next;
    }

    @Override
    public void setNextProcessor(Processor processor) {
        next = processor;
    }

    @Override
    public void setToLast(Processor processor) {
        if (next == null) {
            this.next = processor;
        } else {
            this.next.setNextProcessor(processor);
        }
    }

    public void setEventManager(EventManager eventManager) {
        this.eventManager = eventManager;
    }
}