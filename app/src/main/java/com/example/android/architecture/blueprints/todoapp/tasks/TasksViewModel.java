/*
 * Copyright 2016, The Android Open Source Project
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

package com.example.android.architecture.blueprints.todoapp.tasks;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.android.architecture.blueprints.todoapp.Event;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailActivity;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.view.ActionMode;
import androidx.arch.core.util.Function;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;


/**
 * Exposes the data to be used in the task list screen.
 * <p>
 * {@link BaseObservable} implements a listener registration mechanism which is notified when a
 * property changes. This is done by assigning a {@link Bindable} annotation to the property's
 * getter method.
 */
public class TasksViewModel extends ViewModel {

    private final MutableLiveData<List<Task>> mItems = new MutableLiveData<>();

    private final MutableLiveData<Boolean> mDataLoading = new MutableLiveData<>();

    private final MutableLiveData<Integer> mCurrentFilteringLabel = new MutableLiveData<>();

    private final MutableLiveData<Integer> mNoTasksLabel = new MutableLiveData<>();

    private final MutableLiveData<Integer> mNoTaskIconRes = new MutableLiveData<>();

    private final MutableLiveData<Boolean> mTasksAddViewVisible = new MutableLiveData<>();

    private final MutableLiveData<Event<Integer>> mSnackbarText = new MutableLiveData<>();

    private TasksFilterType mCurrentFiltering = TasksFilterType.ALL_TASKS;

    private final TasksRepository mTasksRepository;

    // Not used at the moment
    private final MutableLiveData<Boolean> mIsDataLoadingError = new MutableLiveData<>();

    private final MutableLiveData<Event<String>> mOpenTaskEvent = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mOpenSelectTaskActionMode = new MutableLiveData<>();

    private final MutableLiveData<Event<Object>> mNewTaskEvent = new MutableLiveData<>();

    private final MutableLiveData<List<String>> selectedTaskIds = new MutableLiveData<>();

    private final MutableLiveData<Boolean> multiSelectMode = new MutableLiveData<>();

    // This LiveData depends on another so we can use a transformation.
    public final LiveData<Boolean> empty = Transformations.map(mItems,
            new Function<List<Task>, Boolean>() {
                @Override
                public Boolean apply(List<Task> input) {
                    return input.isEmpty();

                }
            });

    public TasksViewModel(TasksRepository repository) {
        mTasksRepository = repository;

        // Set initial state
        setFiltering(TasksFilterType.ALL_TASKS);
    }

    public void start() {
        loadTasks(false);
        closeSelectTaskActionMode();
    }

    public void loadTasks(boolean forceUpdate) {
        loadTasks(forceUpdate, true);
    }

    /**
     * Sets the current task filtering type.
     *
     * @param requestType Can be {@link TasksFilterType#ALL_TASKS},
     *                    {@link TasksFilterType#COMPLETED_TASKS}, or
     *                    {@link TasksFilterType#ACTIVE_TASKS}
     */
    public void setFiltering(TasksFilterType requestType) {
        mCurrentFiltering = requestType;

        // Depending on the filter type, set the filtering label, icon drawables, etc.
        switch (requestType) {
            case ALL_TASKS:
                mCurrentFilteringLabel.setValue(R.string.label_all);
                mNoTasksLabel.setValue(R.string.no_tasks_all);
                mNoTaskIconRes.setValue(R.drawable.ic_assignment_turned_in_24dp);
                mTasksAddViewVisible.setValue(true);
                break;
            case ACTIVE_TASKS:
                mCurrentFilteringLabel.setValue(R.string.label_active);
                mNoTasksLabel.setValue(R.string.no_tasks_active);
                mNoTaskIconRes.setValue(R.drawable.ic_check_circle_24dp);
                mTasksAddViewVisible.setValue(false);
                break;
            case COMPLETED_TASKS:
                mCurrentFilteringLabel.setValue(R.string.label_completed);
                mNoTasksLabel.setValue(R.string.no_tasks_completed);
                mNoTaskIconRes.setValue(R.drawable.ic_verified_user_24dp);
                mTasksAddViewVisible.setValue(false);
                break;
        }

    }

    public void clearCompletedTasks() {
        mTasksRepository.clearCompletedTasks();
        mSnackbarText.setValue(new Event<>(R.string.completed_tasks_cleared));
        loadTasks(false, false);
    }

    public void completeTask(Task task, boolean completed) {
        // Notify repository
        if (completed) {
            mTasksRepository.completeTask(task);
            showSnackbarMessage(R.string.task_marked_complete);
        } else {
            mTasksRepository.activateTask(task);
            showSnackbarMessage(R.string.task_marked_active);
        }
    }

    // LiveData getters

    public LiveData<Boolean> getTasksAddViewVisible() {
        return mTasksAddViewVisible;
    }

    public LiveData<Boolean> isDataLoading() {
        return mDataLoading;
    }

    public MutableLiveData<Integer> getCurrentFilteringLabel() {
        return mCurrentFilteringLabel;
    }

    public MutableLiveData<Integer> getNoTasksLabel() {
        return mNoTasksLabel;
    }

    public MutableLiveData<Integer> getNoTaskIconRes() {
        return mNoTaskIconRes;
    }

    public LiveData<Event<Integer>> getSnackbarMessage() {
        return mSnackbarText;
    }

    public LiveData<Event<String>> getOpenTaskEvent() {
        return mOpenTaskEvent;
    }

    public LiveData<Boolean> getOpenSelectTaskActionMode() {
        return mOpenSelectTaskActionMode;
    }

    public LiveData<Event<Object>> getNewTaskEvent() {
        return mNewTaskEvent;
    }

    public LiveData<List<Task>> getItems() {
        return mItems;
    }

    public MutableLiveData<List<String>> getSelectedTaskIds() {
        return selectedTaskIds;
    }

    public MutableLiveData<Boolean> getMultiSelectMode() {
        return multiSelectMode;
    }


    /**
     * Called by the Data Binding library and the FAB's click listener.
     */
    public void addNewTask() {
        mNewTaskEvent.setValue(new Event<>(new Object()));
    }

    /**
     * Called by the {@link TasksAdapter}.
     */
    void openTask(String taskId) {
        mOpenTaskEvent.setValue(new Event<>(taskId));

    }

    void handleActivityResult(int requestCode, int resultCode) {
        if (AddEditTaskActivity.REQUEST_CODE == requestCode) {
            switch (resultCode) {
                case TaskDetailActivity.EDIT_RESULT_OK:
                    mSnackbarText.setValue(new Event<>(R.string.successfully_saved_task_message));
                    break;
                case AddEditTaskActivity.ADD_EDIT_RESULT_OK:
                    mSnackbarText.setValue(new Event<>(R.string.successfully_added_task_message));
                    break;
                case TaskDetailActivity.DELETE_RESULT_OK:
                    mSnackbarText.setValue(new Event<>(R.string.successfully_deleted_task_message));
                    break;
            }
        }
    }

    private void showSnackbarMessage(Integer message) {
        mSnackbarText.setValue(new Event<>(message));
    }

    public void setmSnackbarText(Integer message){
        showSnackbarMessage(message);
    }

    /**
     * @param forceUpdate   Pass in true to refresh the data in the {@link TasksDataSource}
     * @param showLoadingUI Pass in true to display a loading icon in the UI
     */
    private void loadTasks(boolean forceUpdate, final boolean showLoadingUI) {
        if (showLoadingUI) {
            mDataLoading.setValue(true);
        }
        if (forceUpdate) {
            mTasksRepository.refreshTasks();
        }

        mTasksRepository.getTasks(new TasksDataSource.LoadTasksCallback() {
            @Override
            public void onTasksLoaded(List<Task> tasks) {
                List<Task> tasksToShow = new ArrayList<>();

                // We filter the tasks based on the requestType
                for (Task task : tasks) {
                    switch (mCurrentFiltering) {
                        case ALL_TASKS:
                            tasksToShow.add(task);
                            break;
                        case ACTIVE_TASKS:
                            if (task.isActive()) {
                                tasksToShow.add(task);
                            }
                            break;
                        case COMPLETED_TASKS:
                            if (task.isCompleted()) {
                                tasksToShow.add(task);
                            }
                            break;
                        default:
                            tasksToShow.add(task);
                            break;
                    }
                }
                if (showLoadingUI) {
                    mDataLoading.setValue(false);
                }
                mIsDataLoadingError.setValue(false);

                List<Task> itemsValue = new ArrayList<>(tasksToShow);
                mItems.setValue(itemsValue);
            }

            @Override
            public void onDataNotAvailable() {
                mIsDataLoadingError.setValue(true);
            }
        });
    }

    void openItemSelectedOptions(String taskId) {
        //mOpenTaskEvent.setValue(new Event<>(taskId));
    }

    public void clearSelectedTasks(final List<String> selectedTaskIds, final boolean selected) {
        mTasksRepository.clearSelectedTasks(selectedTaskIds,selected);
        //mSnackbarText.setValue(new Event<>(R.string.completed_tasks_cleared));
        loadTasks(false, false);
    }

    public void clearSelectedTasks(final boolean selected){
        clearSelectedTasks(selectedTaskIds.getValue(),selected);
        selectedTaskIds.getValue().clear();
    }

    void openSelectTaskActionMode() {
        mOpenSelectTaskActionMode.setValue(true);
    }

    void closeSelectTaskActionMode() {
        mOpenSelectTaskActionMode.setValue(false);
    }

    void updateSelectedTaskIds(List<String> selectedTaskIdsValue){
        selectedTaskIds.setValue(selectedTaskIdsValue);
    }

    void setMultiSelectMode(boolean multiSelectMode1){
        multiSelectMode.setValue(multiSelectMode1);
    }

    public void deleteSelectedTasks() {
        mTasksRepository.deleteSelectedTasks(selectedTaskIds.getValue());
        selectedTaskIds.getValue().clear();
        Log.d("TAG","selectedTaskIds_size="+selectedTaskIds.getValue().size());
        mSnackbarText.setValue(new Event<>(R.string.selected_tasks_deleted));
        loadTasks(false, true);
    }

}
