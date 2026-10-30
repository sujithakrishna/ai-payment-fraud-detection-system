from pathlib import Path
import joblib
import pandas as pd

from sklearn.model_selection import train_test_split
from sklearn.metrics import (
    confusion_matrix,
    precision_score,
    recall_score,
    f1_score
)

BASE_DIR = Path(__file__).resolve().parent

df = pd.read_csv(BASE_DIR / "synthetic_transactions.csv")

X = df.drop(columns=["is_fraud"])
y = df["is_fraud"]

X_train, X_test, y_train, y_test = train_test_split(
    X,
    y,
    test_size=0.20,
    random_state=42,
    stratify=y
)

artifact = joblib.load(
    BASE_DIR / "transaction_fraud_model.pkl"
)

model = artifact["model"]

probabilities = model.predict_proba(X_test)[:, 1]
fraud_class_index = list(model.classes_).index(1)

probabilities = model.predict_proba(X_test)[:, fraud_class_index]

print("\n========== THRESHOLD EVALUATION ==========")
print("Test records:", len(y_test))
print("Actual fraud records:", int(y_test.sum()))

for threshold in [0.30, 0.40, 0.50, 0.60, 0.70]:

    predictions = (probabilities >= threshold).astype(int)

    tn, fp, fn, tp = confusion_matrix(
        y_test,
        predictions,
        labels=[0, 1]
    ).ravel()

    precision = precision_score(
        y_test, predictions, zero_division=0
    )

    recall = recall_score(
        y_test, predictions, zero_division=0
    )

    f1 = f1_score(
        y_test, predictions, zero_division=0
    )

    fpr = fp / (fp + tn) if (fp + tn) else 0

    print(f"\nTHRESHOLD: {threshold:.2f}")
    print(f"True Positives: {tp}")
    print(f"False Positives: {fp}")
    print(f"True Negatives: {tn}")
    print(f"False Negatives: {fn}")
    print(f"Precision: {precision:.4f}")
    print(f"Recall: {recall:.4f}")
    print(f"F1-score: {f1:.4f}")
    print(f"False Positive Rate: {fpr:.4f}")
