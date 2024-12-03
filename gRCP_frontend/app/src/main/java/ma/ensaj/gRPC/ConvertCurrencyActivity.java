package ma.ensaj.gRPC;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import ma.ensaj.gRPC.R;
import ma.project.stubs.Bank;
import ma.project.stubs.BankServiceGrpc;

public class ConvertCurrencyActivity extends AppCompatActivity {

    private TextInputEditText etAmount;
    private AutoCompleteTextView etCurrencyFrom, etCurrencyTo;
    private MaterialButton btnConvert;
    private TextView tvResult;
    private ImageButton btnSwapCurrencies;

    private ManagedChannel channel;
    private BankServiceGrpc.BankServiceBlockingStub stub;

    // List of supported currencies
    private static final String[] CURRENCIES = {
            "USD", "EUR", "GBP", "JPY", "CAD",
            "AUD", "CHF", "CNY", "INR", "BRL"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_convert_currency);

        // Initialize views
        etAmount = findViewById(R.id.input_from_amount);
        etCurrencyFrom = findViewById(R.id.input_from_currency);
        etCurrencyTo = findViewById(R.id.input_to_currency);
        btnConvert = findViewById(R.id.btn_convert);
        tvResult = findViewById(R.id.text_result);
        btnSwapCurrencies = findViewById(R.id.btn_swap_currencies);

        // Setup currency dropdowns
        setupCurrencyDropdowns();

        // Setup gRPC channel
        setupGrpcChannel();

        // Convert button listener
        btnConvert.setOnClickListener(v -> convertCurrency());

        // Swap currencies button listener
        btnSwapCurrencies.setOnClickListener(v -> swapCurrencies());
    }

    private void setupCurrencyDropdowns() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                CURRENCIES
        );

        etCurrencyFrom.setAdapter(adapter);
        etCurrencyTo.setAdapter(adapter);
    }

    private void swapCurrencies() {
        String from = etCurrencyFrom.getText().toString();
        String to = etCurrencyTo.getText().toString();

        etCurrencyFrom.setText(to);
        etCurrencyTo.setText(from);
    }

    private void setupGrpcChannel() {
        try {
            channel = ManagedChannelBuilder.forAddress("192.168.1.158", 5555)
                    .usePlaintext()
                    .keepAliveTimeout(30, TimeUnit.SECONDS)
                    .keepAliveTime(30, TimeUnit.SECONDS)
                    .build();

            stub = BankServiceGrpc.newBlockingStub(channel);

        } catch (Exception e) {
            Log.e("gRPC Setup Error", "Error setting up gRPC channel: " + e.getMessage(), e);
            Toast.makeText(this, "Failed to set up gRPC channel", Toast.LENGTH_SHORT).show();
        }
    }

    private void convertCurrency() {
        String amountStr = etAmount.getText().toString();
        String currencyFrom = etCurrencyFrom.getText().toString();
        String currencyTo = etCurrencyTo.getText().toString();

        // Validation
        if (amountStr.isEmpty() || currencyFrom.isEmpty() || currencyTo.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create gRPC request
        Bank.ConvertCurrencyRequest request = Bank.ConvertCurrencyRequest.newBuilder()
                .setAmount(amount)
                .setCurrencyFrom(currencyFrom)
                .setCurrencyTo(currencyTo)
                .build();

        // Perform conversion in background thread
        new Thread(() -> {
            try {
                Bank.ConvertCurrencyResponse response = stub.convert(request);

                runOnUiThread(() -> {
                    String resultText = String.format("%s %s = %s %s",
                            amountStr, currencyFrom,
                            String.format("%.2f", response.getResult()), currencyTo);
                    tvResult.setText(resultText);
                });

            } catch (StatusRuntimeException e) {
                Log.e("gRPC Error", "Error calling gRPC: " + e.getMessage(), e);
                runOnUiThread(() -> Toast.makeText(this, "Conversion failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                Log.e("gRPC Error", "Unexpected error: " + e.getMessage(), e);
                runOnUiThread(() -> Toast.makeText(this, "An error occurred", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
        }
    }
}