package com.ishland.c2me.base.common.structs;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;

/**
 * A priority queue with fixed number of priorities and allows changing priorities of elements.
 * Not thread-safe.
 *
 * @param <E> the type of elements held in this collection
 */
public class DynamicPriorityQueue<E> {

    private final ReferenceLinkedOpenHashSet<E>[] priorities;
    private final Reference2IntMap<E> priorityMap = new Reference2IntOpenHashMap<>();

    private int currentMinPriority = 0;

    public DynamicPriorityQueue(int priorityCount) {
        //noinspection unchecked
        this.priorities = new ReferenceLinkedOpenHashSet[priorityCount];
        for (int i = 0; i < priorityCount; i++) {
            this.priorities[i] = new ReferenceLinkedOpenHashSet<>();
        }
    }

    public void enqueue(E element, int priority) {
        if (priority < 0 || priority >= priorities.length)
            throw new IllegalArgumentException("Priority out of range");
        if (priorityMap.containsKey(element))
            throw new IllegalArgumentException("Element already in queue");

        priorities[priority].add(element);
        priorityMap.put(element, priority);
        if (priority < currentMinPriority)
            currentMinPriority = priority;
    }

    public void changePriority(E element, int priority) {
        if (priority < 0 || priority >= priorities.length)
            throw new IllegalArgumentException("Priority out of range");
        if (!priorityMap.containsKey(element))
            throw new IllegalArgumentException("Element not in queue");

        int oldPriority = priorityMap.getInt(element);
        if (oldPriority == priority) return; // nothing to do

        priorities[oldPriority].remove(element);
        priorities[priority].add(element);
        priorityMap.put(element, priority);

        if (priority < currentMinPriority) currentMinPriority = priority;
    }

    public E dequeue() {
        while (currentMinPriority < priorities.length) {
            ReferenceLinkedOpenHashSet<E> priority = this.priorities[currentMinPriority];
            if (priority.isEmpty()) {
                currentMinPriority++;
                continue;
            }
            E element = priority.removeFirst();
            priorityMap.removeInt(element);
            return element;
        }
        return null;
    }

    public boolean contains(E element) {
        return priorityMap.containsKey(element);
    }

    public void remove(E element) {
        if (!priorityMap.containsKey(element))
            throw new IllegalArgumentException("Element not in queue");
        int priority = priorityMap.getInt(element);
        priorities[priority].remove(element);
        priorityMap.removeInt(element);
    }

}
