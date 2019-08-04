package com.bowtye.decisive.Adapters;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bowtye.decisive.Models.Option;
import com.bowtye.decisive.Models.Project;
import com.bowtye.decisive.Models.ProjectWithDetails;
import com.bowtye.decisive.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewHolder> {

    private int mProjectCount;
    private List<ProjectWithDetails> mProjects;
    final private ProjectItemClickListener mClickListener;

    public MainAdapter(List<ProjectWithDetails> projects, ProjectItemClickListener clickListener) {
        mClickListener = clickListener;
        mProjects = projects;
        if (mProjects != null) {
            mProjectCount = mProjects.size();
        } else {
            mProjectCount = 0;
        }
    }

    public void setProjects(List<ProjectWithDetails> projects) {
        mProjects = projects;
        if (mProjects != null) {
            mProjectCount = mProjects.size();
        } else {
            mProjectCount = 0;
        }
    }


    @NonNull
    @Override
    public MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_project, parent, false);
        return new MainViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MainViewHolder holder, int position) {
        holder.bind(mProjects.get(position));
    }

    @Override
    public int getItemCount() {
        return mProjectCount;
    }

    class MainViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener {

        @BindView(R.id.iv_project_card_header)
        ImageView mProjectImageView;
        @BindView(R.id.tv_project_card_title)
        TextView mProjectTitle;
        @BindView(R.id.tv_project_choices)
        TextView mChoicesTextView;
        @BindView(R.id.tv_project_requirement_count)
        TextView mRequirementsTextView;

        MainViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onClick(View view) {
            Timber.d("Project clicked: %d", getAdapterPosition());
            mClickListener.onProjectItemClicked(getAdapterPosition());
        }

        void bind(ProjectWithDetails p) {

            String image = checkForImages(p);

            if(image.equals("")) {
                mProjectImageView.setVisibility(View.GONE);
            } else {
                mProjectImageView.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load(image)
                        .fit()
                        .centerCrop()
                        .into(mProjectImageView);
            }
            mProjectTitle.setText(p.getProject().getName());
            mChoicesTextView.setText((p.getOptionList() == null) ? "0" : String.valueOf(p.getOptionList().size()));
            mRequirementsTextView.setText((p.getRequirementList()== null) ? "0" : String.valueOf(p.getRequirementList().size()));
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            contextMenu.setHeaderTitle(mProjects.get(getAdapterPosition()).getProject().getName());
            MenuItem deleteActionItem = contextMenu.add("Delete");
            deleteActionItem.setOnMenuItemClickListener(menuItem -> {
                mClickListener.onProjectDeleteMenuClicked(getAdapterPosition());
                return true;
            });
            MenuItem editActionItem = contextMenu.add("Edit");
            editActionItem.setOnMenuItemClickListener(menuItem -> {
                mClickListener.onProjectEditMenuCLicked(getAdapterPosition());
                return true;
            });

        }

        private String checkForImages(ProjectWithDetails project){
            for(Option option: project.getOptionList()){
                if(!option.getImagePath().equals("")){
                    return option.getImagePath();
                }
            }
            return "";
        }

    }

    public interface ProjectItemClickListener {
        void onProjectItemClicked(int position);
        void onProjectDeleteMenuClicked(int position);
        void onProjectEditMenuCLicked(int position);
    }
}
