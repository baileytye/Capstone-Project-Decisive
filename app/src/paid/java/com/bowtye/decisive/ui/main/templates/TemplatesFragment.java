package com.bowtye.decisive.ui.main.templates;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.bowtye.decisive.R;
import com.bowtye.decisive.ui.login.LoginActivity;
import com.bowtye.decisive.utils.PicassoMenuLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import timber.log.Timber;

import static com.bowtye.decisive.ui.main.home.HomeFragment.EXTRA_SIGN_OUT;

public class TemplatesFragment extends BaseTemplatesFragment {

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        if (FirebaseAuth.getInstance().getCurrentUser() != null && !FirebaseAuth.getInstance().getCurrentUser().isAnonymous()) {
            Timber.d("Setting user image");
            PicassoMenuLoader menuLoader = new PicassoMenuLoader(menu.getItem(0), getActivity());
            Picasso.get().load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl()).into(menuLoader);
        } else {
            MenuItem item = menu.findItem(R.id.action_sign_out);
            item.setTitle(R.string.menu_sign_in_title);

            item = menu.findItem(R.id.action_profile);
            item.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        String userName;
        int id = item.getItemId();

        if (id == R.id.action_sign_out) {
            Intent intent = new Intent(this.getActivity(), LoginActivity.class);
            intent.putExtra(EXTRA_SIGN_OUT, true);
            Objects.requireNonNull(getActivity()).startActivity(intent);
            getActivity().finish();
            return true;
        } else if (id == R.id.action_profile) {
            if (FirebaseAuth.getInstance().getCurrentUser() != null && !FirebaseAuth.getInstance().getCurrentUser().isAnonymous()) {
                userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                Toast.makeText(this.getContext(), getString(R.string.concatenation_username, userName), Toast.LENGTH_SHORT).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
