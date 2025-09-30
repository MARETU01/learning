package com.learning.algorithm.leetcode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * @author luoding
 * @date 2025-09-18 17:35:48
 * @description 3408.设计任务管理器
 */
public class 设计任务管理器 {
    /**
     * Your TaskManager object will be instantiated and called as such:
     * TaskManager obj = new TaskManager(tasks);
     * obj.add(userId,taskId,priority);
     * obj.edit(taskId,newPriority);
     * obj.rmv(taskId);
     * int param_4 = obj.execTop();
     */
    class TaskManager {
        class Task implements Comparable<Task> {
            int userId, taskId, priority;
            public Task(int userId, int taskId, int priority) {
                this.userId = userId;
                this.taskId = taskId;
                this.priority = priority;
            }
            @Override
            public int compareTo(Task other) {
                if (this.priority != other.priority) {
                    return other.priority - this.priority;
                }
                return other.taskId - this.taskId;
            }
        }

        private Map<Integer, Task> map = new HashMap<>();
        private TreeSet<Task> set = new TreeSet<>();

        public TaskManager(List<List<Integer>> tasks) {
            for (List<Integer> task : tasks) {
                Task newTask = new Task(task.get(0), task.get(1), task.get(2));
                map.put(newTask.taskId, newTask);
                set.add(newTask);
            }
        }

        public void add(int userId, int taskId, int priority) {
            Task newTask = new Task(userId, taskId, priority);
            map.put(taskId, newTask);
            set.add(newTask);
        }

        public void edit(int taskId, int newPriority) {
            Task task = map.get(taskId);
            set.remove(task);
            task.priority = newPriority;
            set.add(task);
        }

        public void rmv(int taskId) {
            set.remove(map.get(taskId));
            map.remove(taskId);
        }

        public int execTop() {
            Task task = set.pollFirst();
            if (task == null) {
                return -1;
            }
            map.remove(task.taskId);
            return task.userId;
        }
    }
}