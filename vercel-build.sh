#!/bin/bash
# Vercel build script to inject backend URL into the static frontend

if [ -n "$BACKEND_URL" ]; then
  echo "Injecting BACKEND_URL into config..."
  echo "const CONFIG = { API_URL: '$BACKEND_URL' };" > web/config.js
else
  echo "No BACKEND_URL provided. Using default."
  echo "const CONFIG = { API_URL: '' };" > web/config.js
fi
