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

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.architecture.blueprints.todoapp.Event;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.ScrollChildSwipeRefreshLayout;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.databinding.TasksFragBinding;
import com.example.android.architecture.blueprints.todoapp.util.SnackbarUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.ActionMode;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

/**
 * Display a grid of {@link Task}s. User can choose to view all, active or completed tasks.
 */
public class TasksFragment extends Fragment {

    private static final String TAG = "TasksFragment";
    private TasksViewModel mTasksViewModel;

    private TasksFragBinding mTasksFragBinding;

    private TasksAdapter mListAdapter;
    private Menu menu;

    public TasksFragment() {
        // Requires empty public constructor
    }

    public static TasksFragment newInstance() {
        return new TasksFragment();
    }

    @Override
    public void onResume() {
        Log.d(TAG,"onResume");
        super.onResume();
        mTasksViewModel.start();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mTasksFragBinding = TasksFragBinding.inflate(inflater, container, false);

        mTasksViewModel = TasksActivity.obtainViewModel(getActivity());

        mTasksFragBinding.setViewmodel(mTasksViewModel);
        mTasksFragBinding.setLifecycleOwner(getActivity());

        setHasOptionsMenu(true);

        return mTasksFragBinding.getRoot();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_clear:
                mTasksViewModel.clearCompletedTasks();
                break;
            case R.id.menu_filter:
                showFilteringPopUpMenu();
                break;
            case R.id.menu_refresh:
                mTasksViewModel.loadTasks(true);
                break;
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.tasks_fragment_menu, menu);
        this.menu = menu;



       /* new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(menu.size()>0){
                    for(int i=0;i<menu.size();i++){

                        Log.d("TAG","getItem(0).getActionView()="+menu.getItem(0).getActionView());
                        Log.d("TAG","getItem(0).getItemId()="+menu.getItem(0).getItemId());

                        TextView itemTextView = menu.getItem(0).getActionView().findViewById(menu.getItem(0).getItemId());
                        itemTextView.setTextColor(getResources().getColor(R.color.white));


                       // ((TextView) menu.getItem(i).getActionView()).setTextColor(getResources().getColor(R.color.white));
                    }
                }
            }
        },2000);*/


       /* Log.d("TAG","menu.size()="+menu.size());
        if(menu.size()>0){
            for(int i=0;i<menu.size();i++){
                TextView itemTextView = menu.getItem(i).getActionView().findViewById(menu.getItem(i).getItemId());
                itemTextView.setTextColor(getResources().getColor(R.color.white));
                Log.d("TAG","menu-txt="+itemTextView.getText());
                //((TextView) menu.getItem(i).getActionView()).setTextColor(getResources().getColor(R.color.white));
            }
        }
*/
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull final Menu menu) {
        super.onPrepareOptionsMenu(menu);
       /* new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(menu.size()>0){
                    for(int i=0;i<menu.size();i++){

                        ((TextView) menu.getItem(i).getActionView()).setTextColor(getResources().getColor(R.color.white));
                    }
                }
            }
        },2000);*/
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupSnackbar();

        setupFab();

        setupListAdapter();

        setupRefreshLayout();

        mTasksViewModel.getOpenSelectTaskActionMode().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                setActionMode(aBoolean);
            }
        });

        mTasksViewModel.setMultiSelectMode(false);

        mTasksViewModel.getSelectedTaskIds().observe(getViewLifecycleOwner(), new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> strings) {
                if(mActionMode!=null){
                    if(strings.size()>0){
                        mActionMode.setTitle(strings.size()+" Selected");
                    }else{
                        mActionMode.setTitle("");
                    }

                }
            }
        });
    }

    private void setupSnackbar() {
        mTasksViewModel.getSnackbarMessage().observe(getViewLifecycleOwner(), new Observer<Event<Integer>>() {
            @Override
            public void onChanged(Event<Integer> event) {
                Integer msg = event.getContentIfNotHandled();
                if (msg != null) {
                    SnackbarUtils.showSnackbar(getView(), getString(msg));
                }
            }
        });
    }

    private void showFilteringPopUpMenu() {


      /*  new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(menu.size()>0){
                    for(int i=0;i<menu.size();i++){
                        TextView itemTextView = menu.getItem(i).getActionView().findViewById(menu.getItem(i).getItemId());
                        itemTextView.setTextColor(getResources().getColor(R.color.white));
                        //((TextView) menu.getItem(i).getActionView()).setTextColor(getResources().getColor(R.color.white));
                    }
                }
            }
        },2000);*/


        PopupMenu popup = new PopupMenu(getContext(), getActivity().findViewById(R.id.menu_filter));
        popup.getMenuInflater().inflate(R.menu.filter_tasks, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.active:
                        mTasksViewModel.setFiltering(TasksFilterType.ACTIVE_TASKS);
                        break;
                    case R.id.completed:
                        mTasksViewModel.setFiltering(TasksFilterType.COMPLETED_TASKS);
                        break;
                    default:
                        mTasksViewModel.setFiltering(TasksFilterType.ALL_TASKS);
                        break;
                }
                mTasksViewModel.loadTasks(false);
                return true;
            }
        });

        popup.show();
    }

    private void setupFab() {
        FloatingActionButton fab = getActivity().findViewById(R.id.fab_add_task);

        fab.setImageResource(R.drawable.ic_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTasksViewModel.addNewTask();
            }
        });
    }

    private void setupListAdapter() {
        ListView listView =  mTasksFragBinding.tasksList;

        mListAdapter = new TasksAdapter(
                new ArrayList<Task>(0),
                mTasksViewModel,
                getActivity()
        );
        listView.setAdapter(mListAdapter);
    }

    private void setupRefreshLayout() {
        ListView listView =  mTasksFragBinding.tasksList;
        final ScrollChildSwipeRefreshLayout swipeRefreshLayout = mTasksFragBinding.refreshLayout;
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                ContextCompat.getColor(getActivity(), R.color.colorAccent),
                ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)
        );
        // Set the scrolling view in the custom SwipeRefreshLayout.
        swipeRefreshLayout.setScrollUpChild(listView);
    }

    private android.view.ActionMode mActionMode;
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.task_select_action, menu);
            mode.setTitle("");
            return true;
        }
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.option_1:
                    mTasksViewModel.setmSnackbarText(R.string.option1_selected);
                    //Toast.makeText(getActivity(), "Option 1 selected", Toast.LENGTH_SHORT).show();
                    mode.finish();
                    return true;
                case R.id.option_2:
                    mTasksViewModel.setmSnackbarText(R.string.option1_selected);
                    // Toast.makeText(getActivity(), "Option 2 selected", Toast.LENGTH_SHORT).show();
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            mTasksViewModel.clearSelectedTasks(false);
            mTasksViewModel.closeSelectTaskActionMode();
            mTasksViewModel.setMultiSelectMode(false);
        }
    };

    void setActionMode(boolean openSelectTaskActionMode){
        Log.d(TAG,"setActionMode="+openSelectTaskActionMode);
        if(openSelectTaskActionMode){
            if (mActionMode != null) {
                return;
            }
            mActionMode = getActivity().startActionMode(mActionModeCallback);
        }else{
            if(mActionMode!=null){
                mActionMode.finish();
                mActionMode = null;
            }

        }

    }


}
