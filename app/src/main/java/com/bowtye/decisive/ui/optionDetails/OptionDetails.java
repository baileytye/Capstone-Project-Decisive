package com.bowtye.decisive.ui.optionDetails;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.transition.Slide;
import android.transition.Transition;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bowtye.decisive.ui.ratings.RatingsActivity;
import com.bowtye.decisive.ui.common.RequirementsAdapter;
import com.bowtye.decisive.utils.ExtraLabels;
import com.bowtye.decisive.utils.RatingUtils;
import com.bowtye.decisive.models.Option;
import com.bowtye.decisive.models.ProjectWithDetails;
import com.bowtye.decisive.models.Requirement;
import com.bowtye.decisive.R;
import com.bowtye.decisive.ui.common.VerticalSpaceItemDecoration;
import com.bowtye.decisive.ui.addOption.AddOption;
import com.bowtye.decisive.utils.RequestCode;
import com.bowtye.decisive.utils.StringUtils;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class OptionDetails extends AppCompatActivity implements RatingUtils.CalculateRatingOfOptionAsyncTask.OptionResultAsyncCallback {

    public static final int RESULT_DELETED = 10;
    private static final String OPTION_ID = "optionDetails";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.toolbar_layout)
    CollapsingToolbarLayout mToolbarLayout;
    @BindView(R.id.tv_rating)
    TextView mRatingTextView;
    @BindView(R.id.rb_option_rating)
    RatingBar mRatingBar;
    @BindView(R.id.text_input_et_notes)
    EditText mNotesTextInputEditText;
    @BindView(R.id.rv_requirements)
    RecyclerView mRecyclerView;
    @BindView(R.id.tv_price)
    TextView mPriceTextView;
    @BindView(R.id.iv_option_image)
    ImageView mOptionImageView;
    @BindView(R.id.layout_rating_with_number)
    View mRatingWithNumberView;

    private int mOptionId;
    Option mOption;
    ProjectWithDetails mProject;
    List<Requirement> mRequirements;
    RequirementsAdapter mAdapter;
    OptionDetailsViewModel mViewModel;
    boolean mIsTemplate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option_details);

        ButterKnife.bind(this);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(ExtraLabels.EXTRA_OPTION_ID)) {
                mOptionId = intent.getIntExtra(ExtraLabels.EXTRA_OPTION_ID, -1);
            }
            if (intent.hasExtra(ExtraLabels.EXTRA_PROJECT)) {
                mProject = intent.getParcelableExtra(ExtraLabels.EXTRA_PROJECT);
                if (mProject != null) {
                    mRequirements = mProject.getRequirementList();
                }
            }
            if (intent.hasExtra(ExtraLabels.EXTRA_IS_TEMPLATE)) {
                mIsTemplate = intent.getBooleanExtra(ExtraLabels.EXTRA_IS_TEMPLATE, false);
            }
        }
        prepareViews();
        prepareViewModel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.option_details, menu);

        if (mIsTemplate) {
            menu.findItem(R.id.action_edit).setVisible(false);
            menu.findItem(R.id.action_delete).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finishAfterTransition();
                return true;
            case R.id.action_delete:
                Intent out = new Intent();
                out.putExtra(ExtraLabels.EXTRA_DELETE_OPTION, mOption);
                setResult(RESULT_DELETED, out);
                finishAfterTransition();
                return true;
            case R.id.action_edit:
                Intent intent = new Intent(getApplicationContext(), AddOption.class);
                intent.putExtra(ExtraLabels.EXTRA_PROJECT, mProject);
                intent.putExtra(ExtraLabels.EXTRA_OPTION, mOption);

                Transition transition = new Slide(Gravity.TOP);

                getWindow().setExitTransition(transition);
                startActivityForResult(intent, RequestCode.EDIT_OPTION_REQUEST_CODE,
                        ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestCode.EDIT_OPTION_REQUEST_CODE) {
            if (data != null && data.hasExtra(ExtraLabels.EXTRA_OPTION)) {
                if (resultCode == RESULT_OK) {
                    Option prevOption = mOption;

                    mOption = data.getParcelableExtra(ExtraLabels.EXTRA_OPTION);

                    if (mOption != null) {
                        if(!mOption.getImagePath().equals(prevOption.getImagePath())){
                            mViewModel.deleteImage(prevOption, this);
                        }
                    }
                    new RatingUtils.CalculateRatingOfOptionAsyncTask(this, mRequirements).execute(mOption);
                }
            }
        }
    }

    private void prepareViews() {
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setInitialPrefetchItemCount(mRequirements.size());
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new RequirementsAdapter(mRequirements, null, true);
        mRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration((int)
                getResources().getDimension(R.dimen.recycler_item_separation)));
        mRecyclerView.setAdapter(mAdapter);

        mRatingWithNumberView.setOnClickListener(view -> {
            Intent intent = new Intent(this.getApplicationContext(), RatingsActivity.class);
            intent.putExtra(ExtraLabels.EXTRA_OPTION, mOption);
            intent.putExtra(ExtraLabels.EXTRA_PROJECT, mProject);
            startActivity(intent);
        });

        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500); // half second between each showcase view

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, OPTION_ID);

        sequence.setConfig(config);

        sequence.addSequenceItem( new MaterialShowcaseView.Builder(this)
                .setDismissOnTouch(true)
                .setMaskColour(getResources().getColor(R.color.colorPrimaryDark))
                .setShapePadding(32)
                .setTitleText(R.string.showcase_title_detailed_ratings)
                .setTarget(mRatingWithNumberView)
                .setDismissText(R.string.showcase_got_it)
                .setContentText(R.string.showcase_message_detailed_ratings)
                .build());

        sequence.start();
    }

    private void fillData() {
        mToolbarLayout.setTitle(mOption.getName());

        if(mProject.getProject().getHasPrice()) {
            mPriceTextView.setText(getResources().getString(R.string.concatenation_price_value, StringUtils.convertToTwoDecimals(mOption.getPrice())));
        } else {
            mPriceTextView.setVisibility(View.INVISIBLE);
        }
        mRatingTextView.setText(String.format(Locale.getDefault(), "%.2f", mOption.getRating()));
        mRatingBar.setRating(mOption.getRating());
        mNotesTextInputEditText.setText(mOption.getNotes());

        if (mOption.getImagePath().equals("")) {
            mOptionImageView.setVisibility(View.GONE);
        } else {
            mOptionImageView.setVisibility(View.VISIBLE);
            Picasso.get()
                    .load(mOption.getImagePath())
                    .fit()
                    .centerCrop()
                    .into(mOptionImageView);
        }
    }

    private void prepareViewModel() {
        if (mIsTemplate) {
            mOption = mProject.getOptionList().get(mOptionId);
            mAdapter.setRequirementValues(mOption.getRequirementValues());
            mAdapter.notifyDataSetChanged();
            fillData();
        } else {
            mViewModel = ViewModelProviders.of(this).get(OptionDetailsViewModel.class);
            mViewModel.getOption(mOptionId).observe(this, option -> {
                Timber.d("Option livedata updated");
                if (option != null) {
                    mOption = option;
                    mAdapter.setRequirementValues(mOption.getRequirementValues());
                    mAdapter.notifyDataSetChanged();
                    fillData();
                }
            });
        }
    }

    @Override
    public void updateOptionAfterCalculatingRatings(Option option) {
        mViewModel.updateOption(mOption, mOptionId);
    }
}
