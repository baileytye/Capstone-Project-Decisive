package com.bowtye.decisive.ui.addOption;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bowtye.decisive.models.Option;
import com.bowtye.decisive.models.Requirement;
import com.bowtye.decisive.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;
import timber.log.Timber;

public class AddOptionAdapter extends RecyclerView.Adapter<AddOptionAdapter.AddOptionRequirementViewHolder> {

    private List<Requirement> mRequirements;
    private Option mOption;
    private Boolean mIsEdit;

    private ItemChangedCallback mCallback;

    AddOptionAdapter(List<Requirement> requirements, Option option, Boolean isEdit, ItemChangedCallback callback) {
        mRequirements = requirements;
        mOption = option;
        mIsEdit = isEdit;
        mCallback = callback;
    }

    @NonNull
    @Override
    public AddOptionRequirementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_requirement_value_editable, parent, false);
        return new AddOptionRequirementViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AddOptionRequirementViewHolder holder, int position) {
        holder.bind(mRequirements.get(position), mOption.getRequirementValues().get(position));
    }

    @Override
    public int getItemCount() {
        return mRequirements.size();
    }

    public Option getOption() {
        return mOption;
    }

    public class AddOptionRequirementViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.sp_averages)
        Spinner spAverages;
        @BindView(R.id.tv_averages_requirement_name)
        TextView tvAveragesRequirementName;
        @BindView(R.id.frame_averages)
        FrameLayout frameAverages;
        @BindView(R.id.cb_value)
        CheckBox cbValue;
        @BindView(R.id.et_value)
        TextInputEditText etValue;
        @BindView(R.id.til_value)
        TextInputLayout tilValue;
        @BindView(R.id.rb_value)
        MaterialRatingBar rbValue;
        @BindView(R.id.tv_rating_requirement_name)
        TextView tvRatingRequirementName;
        @BindView(R.id.frame_star_rating)
        FrameLayout frameStarRating;
        @BindView(R.id.tv_unit)
        TextView unitTextView;

        AddOptionRequirementViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(Requirement requirement, Double value) {
            Requirement.Type type = requirement.getType();
            setVisibleType(type);
            switch (type) {
                case starRating:
                    tvRatingRequirementName.setText(requirement.getName());
                    rbValue.setRating(value.floatValue());
                    rbValue.setOnRatingChangeListener((ratingBar, rating) -> {
                        mOption.getRequirementValues().set(getAdapterPosition(), (double) rating);
                        mCallback.requirementItemChanged();
                    });
                    unitTextView.setVisibility(View.GONE);
                    break;
                case checkbox:
                    cbValue.setChecked(value == 1);
                    cbValue.setText(requirement.getName());
                    cbValue.setOnCheckedChangeListener((compoundButton, b) -> {
                        mOption.getRequirementValues().set(getAdapterPosition(), (b) ? 1.0 : 0.0);
                        mCallback.requirementItemChanged();
                    });
                    unitTextView.setVisibility(View.GONE);
                    break;
                case averaging:
                    spAverages.setSelection(Requirement.getAveragingIndex(value, itemView.getContext()), false);
                    tvAveragesRequirementName.setText(requirement.getName());
                    final boolean[] check = {false};
                    spAverages.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            mOption.getRequirementValues().set(getAdapterPosition(),
                                    Requirement.getAveragingValue(adapterView.getSelectedItem().toString(), itemView.getContext()));
                            if(check[0]) {
                                mCallback.requirementItemChanged();
                            } else {
                                check[0] = true;
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
                    unitTextView.setVisibility(View.GONE);
                    break;
                case number:
                    if (mIsEdit || value != 0) {
                        etValue.setText(String.valueOf(value));
                    }
                    tilValue.setHint(requirement.getName());
                    etValue.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            mOption.getRequirementValues().set(getAdapterPosition(),
                                    (Objects.requireNonNull(etValue.getText()).toString().equals(""))
                                            ? 0.0
                                            : Double.parseDouble(etValue.getText().toString())
                            );
                            mCallback.requirementItemChanged();
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                        }
                    });
                    if(requirement.getUnit() == null || requirement.getUnit().equals("")){
                        etValue.setPaddingRelative(etValue.getPaddingStart(), etValue.getPaddingTop(),
                                (int)itemView.getResources().getDimension(R.dimen.text_input_edit_text_padding),
                                etValue.getPaddingBottom());
                    }
                    unitTextView.setText(requirement.getUnit());
                    break;
            }
        }

        void setVisibleType(Requirement.Type type) {
            Timber.d("Type: %s", type.toString());
            switch (type) {
                case starRating:
                    cbValue.setVisibility(View.INVISIBLE);
                    tilValue.setVisibility(View.INVISIBLE);
                    frameAverages.setVisibility(View.INVISIBLE);
                    frameStarRating.setVisibility(View.VISIBLE);
                    return;
                case averaging:
                    cbValue.setVisibility(View.INVISIBLE);
                    tilValue.setVisibility(View.INVISIBLE);
                    frameAverages.setVisibility(View.VISIBLE);
                    frameStarRating.setVisibility(View.INVISIBLE);
                    return;
                case checkbox:
                    cbValue.setVisibility(View.VISIBLE);
                    tilValue.setVisibility(View.INVISIBLE);
                    frameAverages.setVisibility(View.INVISIBLE);
                    frameStarRating.setVisibility(View.INVISIBLE);
                    return;
                default:
                    cbValue.setVisibility(View.INVISIBLE);
                    tilValue.setVisibility(View.VISIBLE);
                    frameAverages.setVisibility(View.INVISIBLE);
                    frameStarRating.setVisibility(View.INVISIBLE);
            }
        }
    }

    public interface ItemChangedCallback {
        void requirementItemChanged();
    }
}
