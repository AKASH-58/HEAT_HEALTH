from fastapi import FastAPI

app = FastAPI(
    title="HeatHealth API",
    description="Heat-Health Risk Prediction & Early Warning System API",
    version="1.0.0"
)

@app.get("/")
def read_root():
    return {
        "app": "HeatHealth",
        "message": "Backend is running"
    }

@app.get("/health")
def health_check():
    return {
        "status": "healthy",
        "service": "heathealth-api",
        "ml_model_loaded": False
    }
