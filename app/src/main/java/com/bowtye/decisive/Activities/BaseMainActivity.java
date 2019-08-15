package com.bowtye.decisive.Activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.transition.Slide;
import android.transition.Transition;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bowtye.decisive.Adapters.MainAdapter;
import com.bowtye.decisive.BuildConfig;
import com.bowtye.decisive.Models.ProjectWithDetails;
import com.bowtye.decisive.R;
import com.bowtye.decisive.ViewModels.MainViewModel;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static com.bowtye.decisive.Helpers.ExtraLabels.EXTRA_EDIT_PROJECT;
import static com.bowtye.decisive.Helpers.ExtraLabels.EXTRA_NEW_PROJECT;
import static com.bowtye.decisive.Helpers.ExtraLabels.EXTRA_PROJECT_ID;

public class BaseMainActivity extends AppCompatActivity
        implements MainAdapter.ProjectItemClickListener {

    public static final int ADD_PROJECT_REQUEST_CODE = 3423;


    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.fab)
    FloatingActionButton mFab;
    @BindView(R.id.rv_main)
    RecyclerView mRecyclerView;
    @BindView(R.id.toolbar_layout)
    CollapsingToolbarLayout mToolbarLayout;

    private RecyclerView.LayoutManager mLayoutManager;
    private MainAdapter mAdapter;
    private MainViewModel mViewModel;

    private List<ProjectWithDetails> mProjects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        ButterKnife.bind(this);

        prepareViewModel();
        prepareViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_clear) {
            mViewModel.clearProjects();
            return true;
        } else if(id == R.id.action_delete_option){
            mViewModel.clearOptions();
            return true;
        } else if (id == R.id.action_delete_requirement){
            mViewModel.clearRequirements();
            return true;
        } else if(id == R.id.action_insert_dummy){
            mViewModel.insertDummyProject();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onProjectItemClicked(int position) {
        Intent intent = new Intent(getApplicationContext(), ProjectDetails.class);
        intent.putExtra(EXTRA_PROJECT_ID, mProjects.get(position).getProject().getId());

        getWindow().setExitTransition(new Slide(Gravity.START));
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }

    @Override
    public void onProjectDeleteMenuClicked(int position) {
        mViewModel.deleteProject(mProjects.get(position));
    }

    @Override
    public void onProjectEditMenuCLicked(int position) {
        startAddProjectActivity(position);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if ((requestCode == ADD_PROJECT_REQUEST_CODE) && (resultCode == RESULT_OK)) {
            if (data != null && data.hasExtra(EXTRA_NEW_PROJECT)) {
                ProjectWithDetails p = data.getParcelableExtra(EXTRA_NEW_PROJECT);
                mViewModel.insertProjectWithDetails(p);
                //TODO: Add calculate ratings for newly added requirements
                Timber.d("Project: %s inserted into the database", (p != null) ? p.getProject().getName() : "NULL");
            }
        }
    }

    private void prepareViews() {
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(true);
        mToolbarLayout.setTitle("Projects");

        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MainAdapter(null, this);
        mRecyclerView.setAdapter(mAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                LinearLayoutManager.VERTICAL);
        dividerItemDecoration.setDrawable(Objects.requireNonNull(getDrawable(R.drawable.divider)));
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        mFab.setOnClickListener(view -> {
            startAddProjectActivity(-1);

        });

        Timber.d("Number of projects: %d", ((mProjects != null) ? mProjects.size() : 0));
    }

    void prepareViewModel() {
        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mViewModel.getProjects().observe(this, projectWithDetails -> {
            mProjects = projectWithDetails;
            Timber.d("Updating Livedata");
            mAdapter.setProjects(projectWithDetails);
            mAdapter.notifyDataSetChanged();
        });
    }

    void startAddProjectActivity(int position) {
        Intent intent = new Intent(getApplicationContext(), AddProjectActivity.class);
        if (position >= 0) {
            intent.putExtra(EXTRA_EDIT_PROJECT, mProjects.get(position));
        }
        Transition transition = new Slide(Gravity.TOP);

        getWindow().setExitTransition(transition);
        startActivityForResult(intent, ADD_PROJECT_REQUEST_CODE,
                ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }


}