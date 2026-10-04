// ==========================================
// VERCEL / PRODUCTION CONFIGURATION
// ==========================================
// When deploying the frontend to Vercel, it cannot run the Java backend.
// You must deploy the Java backend separately (e.g., to Render using the Dockerfile).
// Once deployed, paste your Java backend's public API URL here.

const CONFIG = {
    // Empty means use relative /api path (perfect for unified deployment)
    API_URL: ""
};
