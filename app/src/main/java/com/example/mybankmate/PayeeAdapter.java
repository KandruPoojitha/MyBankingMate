package com.example.mybankmate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PayeeAdapter extends RecyclerView.Adapter<PayeeAdapter.PayeeViewHolder> {

    private final ArrayList<Payee> payeeList;
    private final PayeeClickListener clickListener;

    public PayeeAdapter(ArrayList<Payee> payeeList, PayeeClickListener clickListener) {
        this.payeeList = payeeList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public PayeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payee, parent, false);
        return new PayeeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PayeeViewHolder holder, int position) {
        Payee payee = payeeList.get(position);
        holder.payeeName.setText(payee.getName());
        holder.payeeAccount.setText(payee.getAccountId());
        holder.itemView.setOnClickListener(v -> clickListener.onPayeeClick(payee));
    }

    @Override
    public int getItemCount() {
        return payeeList.size();
    }

    static class PayeeViewHolder extends RecyclerView.ViewHolder {
        TextView payeeName, payeeAccount;

        PayeeViewHolder(@NonNull View itemView) {
            super(itemView);
            payeeName = itemView.findViewById(R.id.payee_name);
            payeeAccount = itemView.findViewById(R.id.payee_account_id);
        }
    }

    public interface PayeeClickListener {
        void onPayeeClick(Payee payee);
    }
}
