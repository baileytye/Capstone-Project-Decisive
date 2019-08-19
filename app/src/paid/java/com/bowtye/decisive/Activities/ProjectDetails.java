package com.bowtye.decisive.Activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.transition.Slide;
import android.transition.Transition;
import android.view.Gravity;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.bowtye.decisive.ViewModels.DetailsViewModel;
import com.google.firebase.auth.FirebaseAuth;

import timber.log.Timber;

import static com.bowtye.decisive.Activities.MainActivity.EXTRA_FIREBASE_ID;
import static com.bowtye.decisive.Helpers.ExtraLabels.EXTRA_DELETE_OPTION;
import static com.bowtye.decisive.Helpers.ExtraLabels.EXTRA_OPTION_ID;
import static com.bowtye.decisive.Helpers.ExtraLabels.EXTRA_PROJECT;

public class ProjectDetails extends BaseProjectDetails{

    private String mFirebaseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        if(intent != null){
            if(intent.hasExtra(EXTRA_FIREBASE_ID)){
                mFirebaseId = intent.getStringExtra(EXTRA_FIREBASE_ID);
            }
        }

        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == EDIT_OPTION_REQUEST_CODE) {
            if (data != null && data.hasExtra(EXTRA_DELETE_OPTION)) {
                switch (resultCode) {
                    case RESULT_DELETED:
                        mViewModel.deleteOptionFirebase(mProject.getOptionList().get(mItemSelected), mItemSelected);
                        mItemDeleted = true;
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    void prepareViewModel() {
        mViewModel = ViewModelProviders.of(this).get(DetailsViewModel.class);
        mViewModel.getProjectFirebase(mProjectId, mFirebaseId).observe(this, projectWithDetails -> {
            Timber.d("Livedata Updated");
            mProject = projectWithDetails;
            mAdapter.setProject(mProject);
            if (mItemAdded) {
                mAdapter.notifyItemInserted(mAdapter.getItemCount() - 1);
                mItemAdded = false;
            } else if (mItemDeleted) {
                mAdapter.notifyItemRemoved(mItemSelected);
                mItemDeleted = false;
            } else {
                mAdapter.notifyDataSetChanged();
            }

            setEmptyMessageVisibility();

            if(mProject != null) {
                mToolbarLayout.setTitle(mProject.getProject().getName());

                if (mProject.getOptionList() != null && mProject.getRequirementList() != null) {
                    Timber.d("Number of requirements loaded: %d",
                            ((this.mProject.getRequirementList() != null) ? this.mProject.getRequirementList().size() : 0));
                    Timber.d("Number of options loaded: %d",
                            ((this.mProject.getOptionList() != null) ? this.mProject.getOptionList().size() : 0));
                }
            }
        });
    }


    @Override
    public void onOptionItemClicked(int position) {
        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            super.onOptionItemClicked(position);
            return;
        }
        Intent intent = new Intent(getApplicationContext(), OptionDetails.class);

        intent.putExtra(EXTRA_OPTION_ID, position);
        intent.putExtra(EXTRA_PROJECT, mProject);

        mItemSelected = position;

        Transition transition = new Slide(Gravity.START);

        getWindow().setExitTransition(transition);
        startActivityForResult(intent, EDIT_OPTION_REQUEST_CODE,
                ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }
}