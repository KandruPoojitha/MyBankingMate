package com.example.mybankmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class MoveMoneyFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_move_money, container, false);

        LinearLayout interacTransferLayout = view.findViewById(R.id.interac_transfer);
        LinearLayout transferAccountsLayout = view.findViewById(R.id.transfer_accounts);
        LinearLayout billPaymentsLayout = view.findViewById(R.id.bill_payments);

        interacTransferLayout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SendMoneyActivity.class);
            startActivity(intent);
        });

        transferAccountsLayout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), BetweenMyAccountsActivity.class);
            startActivity(intent);
        });

        billPaymentsLayout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PayBillActivity.class);
            startActivity(intent);
        });

        return view;
    }
}
