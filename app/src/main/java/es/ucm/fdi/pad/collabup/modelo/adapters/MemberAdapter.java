package es.ucm.fdi.pad.collabup.modelo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.bumptech.glide.Glide;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.modelo.Usuario;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    private List<Usuario> members;

    public MemberAdapter(List<Usuario> members) {
        this.members = members;
    }

    public static class MemberViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivMemberImage;
        public TextView tvMemberName;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMemberImage = itemView.findViewById(R.id.ivMemberImage);
            tvMemberName = itemView.findViewById(R.id.tvMemberName);
        }
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member_circle, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {


        Usuario member = members.get(position);


        holder.tvMemberName.setText(member.getNombre());
        String urlFoto = member.getUrlFoto();

        Glide.with(holder.itemView.getContext()) //utiliza el contexto de la vista, en caso de no tener foto, utiliza como placeholder la foto por defecto
                .load(urlFoto)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(holder.ivMemberImage);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public void updateMembers(List<Usuario> newMembers) {
        this.members = newMembers;
        notifyDataSetChanged();
    }
}
