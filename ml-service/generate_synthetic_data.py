
import numpy as np
import pandas as pd

from pathlib import Path

# --------------------------------------------------
# Configuration
# --------------------------------------------------

BASE_DIR = Path(__file__).resolve().parent
OUTPUT_PATH = BASE_DIR / "synthetic_transactions.csv"

TOTAL_TRANSACTIONS = 20000
FRAUD_RATE = 0.025

rng = np.random.default_rng(42)

# Generate simulated target labels.
# 0 = legitimate, 1 = simulated fraud
is_fraud = rng.binomial(
    1,
    FRAUD_RATE,
    TOTAL_TRANSACTIONS
)

# --------------------------------------------------
# Generate transaction features
# --------------------------------------------------

amount = np.where(
    is_fraud == 1,
    rng.lognormal(mean=8.0, sigma=1.0, size=TOTAL_TRANSACTIONS),
    rng.lognormal(mean=7.0, sigma=1.0, size=TOTAL_TRANSACTIONS)
)

amount = np.clip(amount, 10, 200000)
amount = np.round(amount, 2)

transaction_types = np.array([
    "TRANSFER",
    "PAYMENT",
    "WITHDRAWAL",
    "DEPOSIT"
])

payment_methods = np.array([
    "UPI",
    "CARD",
    "BANK_TRANSFER",
    "WALLET"
])

transaction_type = []
payment_method = []

for fraud in is_fraud:

    if fraud == 1:
        transaction_type.append(
            rng.choice(
                transaction_types,
                p=[0.50, 0.20, 0.25, 0.05]
            )
        )

        payment_method.append(
            rng.choice(
                payment_methods,
                p=[0.50, 0.25, 0.15, 0.10]
            )
        )

    else:
        transaction_type.append(
            rng.choice(
                transaction_types,
                p=[0.35, 0.35, 0.15, 0.15]
            )
        )

        payment_method.append(
            rng.choice(
                payment_methods,
                p=[0.40, 0.25, 0.20, 0.15]
            )
        )

# Recent sender transaction frequency
sender_txn_count_10m = np.where(
    is_fraud == 1,
    rng.poisson(4.0, TOTAL_TRANSACTIONS),
    rng.poisson(1.2, TOTAL_TRANSACTIONS)
)

# Recent receiver transaction frequency
receiver_txn_count_10m = np.where(
    is_fraud == 1,
    rng.poisson(2.5, TOTAL_TRANSACTIONS),
    rng.poisson(0.8, TOTAL_TRANSACTIONS)
)

# Minutes since sender's previous transaction
minutes_since_last_txn = np.where(
    is_fraud == 1,
    rng.exponential(25, TOTAL_TRANSACTIONS),
    rng.exponential(180, TOTAL_TRANSACTIONS)
)

minutes_since_last_txn = np.round(
    np.clip(minutes_since_last_txn, 0.1, 1440),
    2
)

# Location change indicator
location_changed = np.where(
    is_fraud == 1,
    rng.binomial(1, 0.35, TOTAL_TRANSACTIONS),
    rng.binomial(1, 0.04, TOTAL_TRANSACTIONS)
)

# Transaction hour
transaction_hour = rng.integers(
    0,
    24,
    TOTAL_TRANSACTIONS
)

# --------------------------------------------------
# Build dataset
# --------------------------------------------------

df = pd.DataFrame({
    "amount": amount,
    "transaction_type": transaction_type,
    "payment_method": payment_method,
    "sender_txn_count_10m": sender_txn_count_10m,
    "receiver_txn_count_10m": receiver_txn_count_10m,
    "minutes_since_last_txn": minutes_since_last_txn,
    "location_changed": location_changed,
    "transaction_hour": transaction_hour,
    "is_fraud": is_fraud
})

# Shuffle records to avoid ordering by label
df = df.sample(
    frac=1,
    random_state=42
).reset_index(drop=True)

# Save dataset
df.to_csv(
    OUTPUT_PATH,
    index=False
)

# --------------------------------------------------
# Dataset summary
# --------------------------------------------------

print("\n========== SYNTHETIC DATASET CREATED ==========")

print("\nDataset shape:", df.shape)

print("\nFraud distribution:")
print(df["is_fraud"].value_counts())

print("\nFraud percentage:")
print(
    (df["is_fraud"].value_counts(normalize=True) * 100)
    .round(2)
)

print("\nFeature columns:")
print(list(df.columns))

print("\nFirst 5 records:")
print(df.head().to_string(index=False))

print("\nMissing values:")
print(df.isnull().sum())

print("\nDataset saved successfully:")
print(OUTPUT_PATH)
