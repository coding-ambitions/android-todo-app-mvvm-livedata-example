/*
 *  Copyright 2017 Google Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp.tasks;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.databinding.TaskItemBinding;

import java.util.ArrayList;
import java.util.List;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LifecycleOwner;


public class TasksAdapter extends BaseAdapter {

    private final TasksViewModel mTasksViewModel;

    private List<Task> mTasks;
    private List<String> selectedtaskids = new ArrayList<>();

    private LifecycleOwner mLifecycleOwner;

    //private boolean multiSelectMode = false;

    public TasksAdapter(List<Task> tasks,
            TasksViewModel tasksViewModel, LifecycleOwner activity) {
        mTasksViewModel = tasksViewModel;
        setList(tasks);
        mLifecycleOwner = activity;

    }

    public void replaceData(List<Task> tasks) {
        setList(tasks);
    }

    @Override
    public int getCount() {
        return mTasks != null ? mTasks.size() : 0;
    }

    @Override
    public Task getItem(int position) {
        return mTasks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, final View view, final ViewGroup viewGroup) {
        TaskItemBinding binding;
        if (view == null) {
            // Inflate
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

            // Create the binding
            binding = TaskItemBinding.inflate(inflater, viewGroup, false);
        } else {
            // Recycling view
            binding = DataBindingUtil.getBinding(view);
            final Task currentTask = mTasks.get(position);
            view.setBackgroundColor(currentTask.isSelected() ? Color.CYAN : Color.TRANSPARENT);
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Log.d("TAG","onLongClick");

                   //resetMultiselectMode();

                    if(!mTasksViewModel.getMultiSelectMode().getValue()){
                        mTasksViewModel.getMultiSelectMode().setValue(true);
                        selectItem(currentTask,view);
                    }

                    return true;
                }
            });
        }

        TaskItemUserActionsListener userActionsListener = new TaskItemUserActionsListener() {
            @Override
            public void onCompleteChanged(Task task, View v) {
                boolean checked = ((CheckBox)v).isChecked();
                mTasksViewModel.completeTask(task, checked);
            }

            @Override
            public void onTaskClicked(Task task,View view1) {
                //resetMultiselectMode();
                if(mTasksViewModel.getMultiSelectMode().getValue()){
                    //select item here also
                    selectItem(task,view1);
                }else{
                    mTasksViewModel.openTask(task.getId());
                }

            }
        };

        binding.setTask(mTasks.get(position));
        binding.setLifecycleOwner(mLifecycleOwner);

        binding.setListener(userActionsListener);

        binding.executePendingBindings();
        return binding.getRoot();
    }

    private void setList(List<Task> tasks) {
        mTasks = tasks;
        notifyDataSetChanged();
    }

    private void selectItem(Task currentTask,View view){
        if(currentTask.isSelected()){
            selectedtaskids.remove(currentTask.getId());
        }else{
            selectedtaskids.add(currentTask.getId());
        }

        mTasksViewModel.updateSelectedTaskIds(selectedtaskids);

        currentTask.setSelected(!currentTask.isSelected());
        if(view!=null){
            view.setBackgroundColor(currentTask.isSelected() ? Color.CYAN : Color.TRANSPARENT);
        }

        mTasksViewModel.openSelectTaskActionMode();
    }


}
