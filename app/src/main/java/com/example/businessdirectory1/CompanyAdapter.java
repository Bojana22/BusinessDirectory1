package com.example.businessdirectory1;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CompanyAdapter extends RecyclerView.Adapter<CompanyAdapter.ViewHolder> {

    private List<Company> companies;

    public CompanyAdapter(List<Company> companies) {
        this.companies = companies;
    }

    // Поправено: Користиме поефикасно освежување на листата
    public void updateList(List<Company> newList) {
        this.companies = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Провери дали layout-от се вика точно item_company.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_company, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Company company = companies.get(position);

        holder.name.setText(company.getName());
        holder.address.setText(company.getAddress());
        holder.phone.setText(company.getPhone());
        holder.website.setText(company.getWebsite());
        holder.logo.setImageResource(company.getIconResId());

        // --- ЛОГИКА ЗА КЛИК: ПРАЌАЊЕ ДО DETAILS ---
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), DetailsActivity.class);

            // Најдобар начин: Го праќаме целиот објект како "company_data"
            // Напомена: Провери дали во Company.java стои "implements Serializable"
            intent.putExtra("company_data", company);

            // Ги оставаме и поединечните за секој случај, ако така ти е полесно во Details
            intent.putExtra("name", company.getName());
            intent.putExtra("address", company.getAddress());
            intent.putExtra("phone", company.getPhone());
            intent.putExtra("website", company.getWebsite());
            intent.putExtra("email", company.getEmail());
            intent.putExtra("lat", company.getLatitude());
            intent.putExtra("lon", company.getLongitude());
            intent.putExtra("logo", company.getIconResId());

            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return companies.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, address, phone, website;
        ImageView logo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.companyName);
            address = itemView.findViewById(R.id.companyAddress);
            phone = itemView.findViewById(R.id.companyPhone);
            website = itemView.findViewById(R.id.companyWebsite);
            logo = itemView.findViewById(R.id.companyLogo);
        }
    }
}