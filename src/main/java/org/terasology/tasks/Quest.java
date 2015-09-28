/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.tasks;

import java.util.List;
import java.util.stream.Collectors;

import org.terasology.entitySystem.entity.EntityRef;

public interface Quest {

    /**
     * @return the id/name of the quest, not the one that people see though.
     */
    String getShortName();

    /**
     * @return human-readable description and explanation of the quest
     */
    String getDescription();

    List<Task> getAllTasks();

    @SuppressWarnings("unchecked")
    default <T extends Task> List<T> getTasks(Class<T> type) {
        return (List<T>) getAllTasks().stream().filter(type::isInstance).collect(Collectors.toList());
    }

    /**
     * @return the status of the quest as a whole
     */
    Status getStatus();


    /**
     * @return the target entity for this quest
     */
    EntityRef getEntity();
}
