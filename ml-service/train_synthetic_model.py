
import pandas as pd
import joblib

from pathlib import Path

from sklearn.compose import ColumnTransformer
from sklearn.preprocessing import OneHotEncoder
from sklearn.pipeline import Pipeline
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier

from sklearn.metrics import (
    classification_report,
    confusion_matrix,
    roc_auc_score,
    average_precision_score
)

# --------------------------------------------------
# Paths
# --------------------------------------------------

BASE_DIR = Path(__file__).resolve().parent

DATASET_PATH = BASE_DIR / "synthetic_transactions.csv"

MODEL_PATH = BASE_DIR / "transaction_fraud_model.pkl"

# --------------------------------------------------
# Load dataset
# --------------------------------------------------

print("Loading synthetic transaction dataset...")

df = pd.read_csv(DATASET_PATH)

X = df.drop(columns=["is_fraud"])
y = df["is_fraud"]

# --------------------------------------------------
# Feature definitions
# --------------------------------------------------

numeric_features = [
    "amount",
    "sender_txn_count_10m",
    "receiver_txn_count_10m",
    "minutes_since_last_txn",
    "location_changed",
    "transaction_hour"
]

categorical_features = [
    "transaction_type",
    "payment_method"
]

# --------------------------------------------------
# Preprocessing
# --------------------------------------------------

preprocessor = ColumnTransformer(
    transformers=[
        (
            "categorical",
            OneHotEncoder(handle_unknown="ignore"),
            categorical_features
        ),
        (
            "numeric",
            "passthrough",
            numeric_features
        )
    ]
)

# --------------------------------------------------
# Random Forest classifier
# --------------------------------------------------

classifier = RandomForestClassifier(
    n_estimators=200,
    max_depth=12,
    min_samples_split=5,
    min_samples_leaf=2,
    class_weight="balanced",
    random_state=42,
    n_jobs=-1
)

# Complete ML pipeline
model = Pipeline(
    steps=[
        ("preprocessor", preprocessor),
        ("classifier", classifier)
    ]
)

# --------------------------------------------------
# Train-test split
# --------------------------------------------------

X_train, X_test, y_train, y_test = train_test_split(
    X,
    y,
    test_size=0.20,
    random_state=42,
    stratify=y
)

print("\nTraining records:", len(X_train))
print("Testing records:", len(X_test))

print("Fraud records in training:", int(y_train.sum()))
print("Fraud records in testing:", int(y_test.sum()))

# --------------------------------------------------
# Train model
# --------------------------------------------------

print("\nTraining Random Forest model...")

model.fit(X_train, y_train)

# --------------------------------------------------
# Predictions
# --------------------------------------------------

y_pred = model.predict(X_test)

y_prob = model.predict_proba(X_test)[:, 1]

# --------------------------------------------------
# Evaluation
# --------------------------------------------------

print("\n========== CLASSIFICATION REPORT ==========")

print(
    classification_report(
        y_test,
        y_pred,
        target_names=["Legitimate", "Fraud"],
        zero_division=0
    )
)

print("\n========== CONFUSION MATRIX ==========")

print(
    confusion_matrix(y_test, y_pred)
)

print(
    "\nROC-AUC:",
    round(roc_auc_score(y_test, y_prob), 4)
)

print(
    "Average Precision:",
    round(average_precision_score(y_test, y_prob), 4)
)

# --------------------------------------------------
# Feature importance
# --------------------------------------------------

feature_names = (
    model.named_steps["preprocessor"]
    .get_feature_names_out()
)

importances = (
    model.named_steps["classifier"]
    .feature_importances_
)

importance_df = pd.DataFrame({
    "feature": feature_names,
    "importance": importances
}).sort_values(
    by="importance",
    ascending=False
)

print("\n========== TOP 10 IMPORTANT FEATURES ==========")

print(importance_df.head(10).to_string(index=False))

# --------------------------------------------------
# Save model
# --------------------------------------------------

joblib.dump(
    {
        "model": model,
        "features": list(X.columns),
        "numeric_features": numeric_features,
        "categorical_features": categorical_features,
        "training_type": "synthetic"
    },
    MODEL_PATH
)

print("\nModel saved successfully:")
print(MODEL_PATH)
