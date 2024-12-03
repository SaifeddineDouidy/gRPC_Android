package ma.ensaj.protobufapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import ma.project.stubs.Bank;
import ma.project.stubs.BankServiceGrpc;

public class ConvertCurrencyActivity extends AppCompatActivity {

    private EditText etAmount, etCurrencyFrom, etCurrencyTo;
    private TextView tvResult;
    private Button btnConvert;

    private ManagedChannel channel;
    private BankServiceGrpc.BankServiceBlockingStub stub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_convert_currency);

        // Initialize UI elements
        etAmount = findViewById(R.id.et_amount);
        etCurrencyFrom = findViewById(R.id.et_currency_from);
        etCurrencyTo = findViewById(R.id.et_currency_to);
        tvResult = findViewById(R.id.tv_result);
        btnConvert = findViewById(R.id.btn_convert);

        // Set up gRPC channel
        setupGrpcChannel();

        // Button click listener
        btnConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                convertCurrency();
            }
        });
    }

    private void setupGrpcChannel() {
        try {
            // Use 10.0.2.2 for Android emulator to access the host machine
            channel = ManagedChannelBuilder.forAddress("192.168.1.158", 5555)
                    .usePlaintext() // This ensures HTTP/2 without TLS
                    .keepAliveTimeout(30, TimeUnit.SECONDS)
                    .keepAliveTime(30, TimeUnit.SECONDS)
                    .build();

            // Create a blocking stub for synchronous calls
            stub = BankServiceGrpc.newBlockingStub(channel);

        } catch (Exception e) {
            Log.e("gRPC Setup Error", "Error setting up gRPC channel: " + e.getMessage(), e);
            Toast.makeText(this, "Failed to set up gRPC channel", Toast.LENGTH_SHORT).show();
        }
    }

    private void convertCurrency() {
        // Get input values
        String amountStr = etAmount.getText().toString();
        String currencyFrom = etCurrencyFrom.getText().toString();
        String currencyTo = etCurrencyTo.getText().toString();

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

        new Thread(() -> {
            try {
                Bank.ConvertCurrencyResponse response = stub.convert(request);

                runOnUiThread(() -> tvResult.setText("Converted Amount: " + response.getResult()));

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
