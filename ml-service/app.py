
from pathlib import Path

import joblib
import pandas as pd

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field


# --------------------------------------------------
# Load trained model
# --------------------------------------------------

BASE_DIR = Path(__file__).resolve().parent
MODEL_PATH = BASE_DIR / "transaction_fraud_model.pkl"

if not MODEL_PATH.exists():
    raise FileNotFoundError(
        f"Trained model not found: {MODEL_PATH}"
    )

saved_artifact = joblib.load(MODEL_PATH)

model = saved_artifact["model"]
FEATURES = saved_artifact["features"]

# Locate the fraud probability class
fraud_class_index = list(model.classes_).index(1)


# --------------------------------------------------
# FastAPI application
# --------------------------------------------------

app = FastAPI(
    title="AI Payment Fraud Detection API",
    description=(
        "Random Forest prediction service "
        "for simulated payment transactions."
    ),
    version="1.0.0"
)


# --------------------------------------------------
# Request schema
# --------------------------------------------------

class TransactionRequest(BaseModel):

    amount: float = Field(gt=0)

    transaction_type: str = Field(min_length=1)

    payment_method: str = Field(min_length=1)

    sender_txn_count_10m: int = Field(ge=0)

    receiver_txn_count_10m: int = Field(ge=0)

    minutes_since_last_txn: float = Field(gt=0)

    location_changed: int = Field(ge=0, le=1)

    transaction_hour: int = Field(ge=0, le=23)


# --------------------------------------------------
# Health endpoint
# --------------------------------------------------

@app.get("/health")
def health_check():

    return {
        "status": "UP",
        "model": "Random Forest",
        "training_type": "Synthetic"
    }


# --------------------------------------------------
# Prediction endpoint
# --------------------------------------------------

@app.post("/predict")
def predict_fraud(request: TransactionRequest):

    try:

        transaction_data = request.model_dump()

        input_df = pd.DataFrame(
            [transaction_data],
            columns=FEATURES
        )

        fraud_probability = model.predict_proba(
            input_df
        )[0][fraud_class_index]

        prediction = (
            "FRAUD"
            if fraud_probability >= 0.60
            else "LEGITIMATE"
        )

        return {
            "prediction": prediction,
            "fraud_probability": round(
                float(fraud_probability), 4
            ),
            "model": "Random Forest",
            "training_type": "Synthetic",
            "threshold": 0.60
        }

    except Exception as error:

        raise HTTPException(
            status_code=500,
            detail="Prediction failed."
        ) from error
