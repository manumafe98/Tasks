/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.tasks.systems;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.registry.In;
import org.terasology.tasks.Quest;
import org.terasology.tasks.Status;
import org.terasology.tasks.Task;
import org.terasology.tasks.TaskGraph;
import org.terasology.tasks.TimeConstraintTask;
import org.terasology.tasks.events.StartTaskEvent;
import org.terasology.tasks.events.TaskCompletedEvent;

/**
 * TODO Type description
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class TimedTaskSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    @In
    private Time time;

    private final Map<TimeConstraintTask, Quest> questRefs = new LinkedHashMap<>();

    @ReceiveEvent
    public void onStartTask(StartTaskEvent event, EntityRef entity) {
        if (event.getTask() instanceof TimeConstraintTask) {
            TimeConstraintTask task = (TimeConstraintTask) event.getTask();
            questRefs.put(task, event.getQuest());
            task.startTimer(time.getGameTime());
        }
    }

    @ReceiveEvent
    public void onCompletedTask(TaskCompletedEvent event, EntityRef entity) {
        Task task = event.getTask();
        Iterator<Entry<TimeConstraintTask, Quest>> it = questRefs.entrySet().iterator();
        while (it.hasNext()) {
            Entry<TimeConstraintTask, Quest> entry = it.next();
            TaskGraph taskGraph = entry.getValue().getTaskGraph();
            if (taskGraph.getDependencies(task).contains(entry.getKey())) {
                it.remove();
            }
        }
    }

    @Override
    public void update(float delta) {
        Iterator<Entry<TimeConstraintTask, Quest>> it = questRefs.entrySet().iterator();
        while (it.hasNext()) {
            Entry<TimeConstraintTask, Quest> entry = it.next();
            TimeConstraintTask task = entry.getKey();

            TaskGraph taskGraph = entry.getValue().getTaskGraph();
            Status prevStatus = taskGraph.getTaskStatus(task);

            if (prevStatus == Status.SUCCEEDED) {
                task.setTime(time.getGameTime());
            }

            Status status = taskGraph.getTaskStatus(task);
            if (status != prevStatus && status.isComplete()) {
                Quest quest = entry.getValue();
                EntityRef entity = quest.getEntity();
                entity.send(new TaskCompletedEvent(quest, task, status.isSuccess()));
                it.remove();
            }
        }
    }
}
