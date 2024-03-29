package com.bowtye.decisive.ui.ratings;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bowtye.decisive.utils.ExtraLabels;
import com.bowtye.decisive.utils.RatingUtils;
import com.bowtye.decisive.models.Option;
import com.bowtye.decisive.models.ProjectWithDetails;
import com.bowtye.decisive.models.Requirement;
import com.bowtye.decisive.R;
import com.bowtye.decisive.ui.common.VerticalSpaceItemDecoration;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static com.bowtye.decisive.utils.RatingUtils.POINTS_TOWARD_TOTAL;
import static com.bowtye.decisive.utils.RatingUtils.RATINGS;

public class RatingsActivity extends AppCompatActivity implements RatingUtils.RatingResultAsyncCallback {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.toolbar_layout)
    CollapsingToolbarLayout mToolbarLayout;
    @BindView(R.id.rv_ratings)
    RecyclerView mRecyclerView;
    @BindView(R.id.rb_option_rating)
    RatingBar mOptionRatingBar;
    @BindView(R.id.tv_rating)
    TextView mRatingTextView;

    private Option mOption;
    private List<Requirement> mRequirements;
    private RatingsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ratings);

        ButterKnife.bind(this);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(ExtraLabels.EXTRA_OPTION)) {
                mOption = intent.getParcelableExtra(ExtraLabels.EXTRA_OPTION);
            }
            if (intent.hasExtra(ExtraLabels.EXTRA_PROJECT)) {
                ProjectWithDetails project = intent.getParcelableExtra(ExtraLabels.EXTRA_PROJECT);
                if (project != null) {
                    mRequirements = project.getRequirementList();
                }
            }
        }

        prepareViews();
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finishAfterTransition();
        }

        return super.onOptionsItemSelected(item);
    }

    private void prepareViews() {
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Timber.d("Rating: %f", mOption.getRating());

        mToolbarLayout.setTitle(mOption.getName());

        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(24));
        mAdapter = new RatingsAdapter(mRequirements, mOption);
        mRecyclerView.setAdapter(mAdapter);

        new RatingUtils.CalculateRatingsAsyncTask(mRequirements, mOption.getRequirementValues(), this).execute();
    }

    @Override
    public void updateUIWithRatingResults(List<Float> ratings, int code) {

        if (code == RATINGS) {
            mAdapter.setRatings(ratings);
            mOption.setRating(RatingUtils.calculateOptionRating(
                    ratings, mRequirements));

            mRatingTextView.setText(String.format(Locale.getDefault(), "%.2f", mOption.getRating()));
            mOptionRatingBar.setRating(mOption.getRating());

            new RatingUtils.CalculatePointsTowardTotalsAsyncTask(mRequirements, ratings, this).execute();
        } else if (code == POINTS_TOWARD_TOTAL) {
            mAdapter.setPointsTowardTotal(ratings);
        }
        mAdapter.notifyDataSetChanged();
    }


}
