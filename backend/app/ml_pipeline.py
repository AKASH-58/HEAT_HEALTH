import os
import json
import logging
from typing import Dict, Any, Tuple, Optional
import numpy as np
import pandas as pd
import joblib
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.metrics import f1_score, precision_score, recall_score, confusion_matrix

from app.heat_risk_engine import BaselineHeatRiskEngine

logger = logging.getLogger(__name__)

MODEL_DIR = os.path.join(os.path.dirname(__file__), "models")
MODEL_PATH = os.path.join(MODEL_DIR, "heat_risk_model.joblib")
METADATA_PATH = os.path.join(MODEL_DIR, "model_metadata.json")

class MLPipeline:
    """
    Experimental Machine Learning Training Pipeline for Research & Demonstration.

    IMPORTANT DISCLAIMER:
    This pipeline generates synthetic training samples derived from rule-based heat-risk formulas.
    Its evaluation metrics reflect formula approximation capabilities and DO NOT demonstrate
    clinical validity or real-world health prediction accuracy.
    """

    FEATURE_NAMES = ["temperature", "relative_humidity", "wind_speed", "apparent_temperature"]
    CLASS_LABELS = ["Low", "Moderate", "High", "Very High"]

    @classmethod
    def generate_training_data(cls, n_samples: int = 2000, seed: int = 42) -> Tuple[pd.DataFrame, pd.Series]:
        np.random.seed(seed)

        temps = np.random.uniform(15.0, 48.0, n_samples)
        humidities = np.random.uniform(10.0, 100.0, n_samples)
        winds = np.random.uniform(0.0, 45.0, n_samples)

        apparent_temps = []
        labels = []

        for t, rh, w in zip(temps, humidities, winds):
            hi = BaselineHeatRiskEngine.calculate_heat_index_celsius(t, rh)
            app_t = hi - (w * 0.1) if w > 10 else hi
            apparent_temps.append(round(app_t, 1))

            if app_t < 27.0:
                labels.append("Low")
            elif app_t < 32.0:
                labels.append("Moderate")
            elif app_t < 41.0:
                labels.append("High")
            else:
                labels.append("Very High")

        df = pd.DataFrame({
            "temperature": temps,
            "relative_humidity": humidities,
            "wind_speed": winds,
            "apparent_temperature": apparent_temps
        })
        y = pd.Series(labels)
        return df, y

    @classmethod
    def train_and_save_model(cls) -> Dict[str, Any]:
        """
        Trains model on synthetic data and saves model artifact with explicit disclaimer metadata.
        """
        os.makedirs(MODEL_DIR, exist_ok=True)
        X, y = cls.generate_training_data(n_samples=3000, seed=42)

        X_train, X_test, y_train, y_test = train_test_split(
            X, y, test_size=0.2, random_state=42, stratify=y
        )

        clf = RandomForestClassifier(n_estimators=100, max_depth=8, random_state=42)
        clf.fit(X_train, y_train)

        y_pred = clf.predict(X_test)

        macro_f1 = float(f1_score(y_test, y_pred, average="macro"))
        precision = float(precision_score(y_test, y_pred, average="macro"))
        recall = float(recall_score(y_test, y_pred, average="macro"))
        cm = confusion_matrix(y_test, y_pred, labels=cls.CLASS_LABELS).tolist()

        metrics = {
            "macro_f1": round(macro_f1, 4),
            "precision": round(precision, 4),
            "recall": round(recall, 4),
            "confusion_matrix": cm,
            "class_labels": cls.CLASS_LABELS
        }

        joblib.dump(clf, MODEL_PATH)

        metadata = {
            "model_version": "1.0.0-rf-experimental",
            "model_type": "RandomForestClassifier",
            "features": cls.FEATURE_NAMES,
            "training_samples": len(X_train),
            "test_samples": len(X_test),
            "dataset_provenance": "Synthetic rule-derived labels (Formula Approximation Only)",
            "clinical_validation": False,
            "metrics": metrics,
            "disclaimer": "Metrics represent approximation of rule-based thresholds and do not constitute clinical validation."
        }

        with open(METADATA_PATH, "w") as f:
            json.dump(metadata, f, indent=2)

        logger.info(f"Experimental ML Model trained and saved. Macro-F1: {macro_f1:.4f}")
        return metadata


class MLRiskPredictor:
    """
    Experimental ML Risk Predictor.
    Defaults to the Baseline Rule Engine for validated predictions.
    """

    def __init__(self):
        self.model = None
        self.metadata = None
        self.loaded = False
        self._load_model()

    def _load_model(self):
        if os.path.exists(MODEL_PATH) and os.path.exists(METADATA_PATH):
            try:
                self.model = joblib.load(MODEL_PATH)
                with open(METADATA_PATH, "r") as f:
                    self.metadata = json.load(f)
                self.loaded = True
                logger.info("Experimental ML model successfully loaded.")
            except Exception as e:
                logger.error(f"Failed to load ML model: {e}")
                self.loaded = False

    def predict(
        self,
        temperature: float,
        relative_humidity: float,
        wind_speed: Optional[float] = None,
        apparent_temperature: Optional[float] = None,
        use_ml: bool = False
    ) -> Dict[str, Any]:
        """
        Evaluates heat risk using Baseline Rule Engine by default.
        Uses ML classifier only when explicitly requested (use_ml=True).
        """
        if not use_ml or not self.loaded or self.model is None:
            return BaselineHeatRiskEngine.evaluate_risk(
                temperature, relative_humidity, wind_speed, apparent_temperature
            )

        try:
            effective_wind = wind_speed if wind_speed is not None else 0.0
            app_t = apparent_temperature if apparent_temperature is not None else temperature
            input_features = pd.DataFrame([{
                "temperature": temperature,
                "relative_humidity": relative_humidity,
                "wind_speed": effective_wind,
                "apparent_temperature": app_t
            }])

            predicted_level = str(self.model.predict(input_features)[0])
            probabilities = self.model.predict_proba(input_features)[0]
            confidence = float(np.max(probabilities))

            baseline_res = BaselineHeatRiskEngine.evaluate_risk(
                temperature, relative_humidity, wind_speed, apparent_temperature
            )

            model_ver = self.metadata.get("model_version", "1.0.0-rf-experimental") if self.metadata else "1.0.0-rf-experimental"

            return {
                "level": predicted_level,
                "score": baseline_res["score"],
                "score_interpretation": (
                    f"Experimental ML Classification ({predicted_level}) with {confidence * 100:.1f}% confidence. "
                    f"[Experimental Model - Not Clinically Validated]. {baseline_res['score_interpretation']}"
                ),
                "method": f"Experimental ML Model v{model_ver} (Unvalidated)",
                "factors": baseline_res["factors"],
                "recommendations": baseline_res["recommendations"],
                "rules_version": baseline_res["rules_version"],
                "model_version": model_ver
            }
        except Exception as e:
            logger.error(f"Error during ML prediction, falling back to baseline: {e}")
            return BaselineHeatRiskEngine.evaluate_risk(
                temperature, relative_humidity, wind_speed, apparent_temperature
            )
